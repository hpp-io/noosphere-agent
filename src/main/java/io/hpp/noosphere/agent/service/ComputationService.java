package io.hpp.noosphere.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.contracts.DelegateeCoordinator;
import io.hpp.noosphere.agent.exception.ContainerException;
import io.hpp.noosphere.agent.service.dto.*;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import io.hpp.noosphere.agent.service.dto.enumeration.ContainerStatus;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.web3j.utils.Numeric;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ComputationService {

    private static final Logger log = LoggerFactory.getLogger(ComputationService.class);

    private final DataStoreService dataStoreService;
    private final ContainerManagerService containerManager;
    private final WebClient webClient;

    public ComputationService(DataStoreService dataStoreService, ContainerManagerService containerManager, WebClient webClient) {
        this.dataStoreService = dataStoreService;
        this.containerManager = containerManager;
        this.webClient = webClient;
    }

    private final String host = System.getenv("RUNTIME") != null && "docker".equals(System.getenv("RUNTIME"))
        ? "host.docker.internal"
        : "localhost";

    /**
     * 컨테이너의 서비스 URL을 가져옵니다
     */
    private String getContainerUrl(String container, boolean isProof) {
        String containerUrl = containerManager.getUrl(container);
        if (containerUrl != null && !containerUrl.isEmpty()) {
            return containerUrl + (isProof ? "/service_output" : "/computation");
        } else {
            int port = containerManager.getPort(container);
            return String.format("http://%s:%d" + (isProof ? "/service_output" : "/computation"), host, port);
        }
    }

    /**
     * 컨테이너별 헤더 설정을 가져옵니다
     */
    private Map<String, String> getHeaders(String container) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String bearer = containerManager.getBearer(container);
        if (bearer != null && !bearer.isEmpty()) {
            headers.put("Authorization", "Bearer " + bearer);
        }

        return headers;
    }

    /**
     * Extracts the 'hex_data' byte array from the input map and decodes it into a UTF-8 string.
     */
    private String decodeInputDataToString(Map<String, Object> data) {
        if (data == null) {
            return "";
        }
        Object hexData = data.get("hex_data");
        if (hexData instanceof byte[]) {
            return new String((byte[]) hexData, StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * 작업을 실행합니다
     */
    private CompletableFuture<List<ContainerResultDTO>> runComputation(
        UUID ComputationId,
        ComputationInputDTO computationInput,
        List<String> containers,
        Optional<OffchainRequestDTO> request,
        Boolean requiresProof,
        byte[] requestId,
        byte[] commitment,
        SubscriptionDTO subscriptionDTO
    ) {
        return CompletableFuture.supplyAsync(() -> {
            // 작업 시작
            request.ifPresent(dataStoreService::setRunning);

            List<ContainerResultDTO> results = new ArrayList<>();

            // 첫 번째 컨테이너 입력 데이터 설정
            ContainerInputDTO inputData = ContainerInputDTO.builder()
                .source(computationInput.getSource())
                .destination(containers.size() == 1 ? computationInput.getDestination() : ComputationLocation.OFF_CHAIN.name())
                .data(computationInput.getData())
                .requiresProof(requiresProof)
                .build();

            log.debug("Initial input.data (decoded): {}", decodeInputDataToString(computationInput.getData()));
            // 컨테이너 체인 실행
            for (int index = 0; index < (requiresProof ? containers.size() + 1 : containers.size()); index++) {
                String container = containers.get(index);
                log.debug("container id: {}, input.data: {}", container, decodeInputDataToString(inputData.getData()));
                String url = getContainerUrl(container, false);
                Map<String, String> headers = getHeaders(container);

                try {
                    // HTTP 요청 실행
                    Map<String, Object> response = webClient
                        .post()
                        .uri(url)
                        .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(inputData.getData() != null ? decodeInputDataToString(inputData.getData()) : Collections.emptyMap())
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofMinutes(3))
                        .block();
                    // 성공 결과 추가
                    results.add(new ContainerOutputDTO(container, response, null));
                    log.info("ComputationId={}, container={}, inputData={}, result=", ComputationId, container, computationInput, response);
                    dataStoreService.trackContainerStatus(container, ContainerStatus.SUCCESS);

                    // 다음 컨테이너를 위한 입력 데이터 준비
                    if (index < containers.size() - 1) {
                        inputData = ContainerInputDTO.builder()
                            .source(ComputationLocation.OFF_CHAIN.toString())
                            .destination(
                                index == containers.size() - 2
                                    ? computationInput.getDestination()
                                    : ComputationLocation.OFF_CHAIN.toString()
                            )
                            .data(response)
                            .requiresProof(requiresProof)
                            .build();
                    }

                    // If proof is required, prepare and call the verifier container
                    if (
                        computationInput.getDestination().equals(ComputationLocation.ON_CHAIN.toString()) &&
                        requiresProof &&
                        index == containers.size() - 1
                    ) {
                        log.info("Proof generation required. Preparing input for verifier.");

                        boolean isOffChain = computationInput.getSource().equals(ComputationLocation.OFF_CHAIN.toString());
                        // This is where you would construct the proof input
                        ProofInputDTO proofInput = buildProofInput(
                            isOffChain ? null : requestId,
                            isOffChain ? null : commitment,
                            computationInput.getData(),
                            response,
                            isOffChain ? subscriptionDTO.toCoordinatorComputeSubscription() : null
                        );

                        String proofurl = getContainerUrl(container, true);
                        // You would then call the verifier container with `proofInput`
                        // For now, we just log it.
                        log.debug("Proof Input DTO: {}", proofInput);

                        String proofResponse = webClient
                            .post()
                            .uri(proofurl)
                            .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(proofInput)
                            .retrieve()
                            .bodyToMono(String.class)
                            .timeout(Duration.ofMinutes(3))
                            .block();
                        // Cast the last result to ContainerOutputDTO to set the proof.
                        ContainerResultDTO lastResult = results.getLast();
                        if (lastResult instanceof ContainerOutputDTO) {
                            ((ContainerOutputDTO) lastResult).setProof(proofResponse);
                            log.info("Proof generated and attached to the result.");
                        }
                    }
                } catch (Exception e) {
                    // 오류 처리
                    ContainerErrorDTO error = new ContainerErrorDTO(container, e.getMessage());
                    results.add(error);

                    log.error(
                        "Container error: ComputationId={}, container={}, inputData={} error={}",
                        ComputationId,
                        container,
                        computationInput.getData(),
                        e.getMessage()
                    );

                    // 실패 상태 추적
                    request.ifPresent(req -> dataStoreService.setFailed(req, results));
                    dataStoreService.trackContainerStatus(container, ContainerStatus.FAILED);

                    return results;
                }
            }
            // 작업 성공
            request.ifPresent(msg -> dataStoreService.setSuccess(msg, results));
            return results;
        });
    }

    /**
     * 체인 프로세서 작업 처리
     */
    public CompletableFuture<List<ContainerResultDTO>> processChainProcessorComputation(
        UUID ComputationId,
        ComputationInputDTO ComputationInput,
        List<String> containers,
        boolean requiresProof,
        byte[] requestId,
        byte[] commitment,
        SubscriptionDTO subscription
    ) {
        return runComputation(
            ComputationId,
            ComputationInput,
            containers,
            Optional.empty(),
            requiresProof,
            requestId,
            commitment,
            subscription
        );
    }

    /**
     * 오프체인 작업 처리
     */
    public CompletableFuture<Void> processOffchainComputation(OffchainRequestDTO request) {
        ComputationInputDTO ComputationInput = ComputationInputDTO.builder()
            .source(ComputationLocation.OFF_CHAIN.name())
            .destination(ComputationLocation.OFF_CHAIN.name())
            .data(request.getData())
            .build();

        return runComputation(
            request.getId(),
            ComputationInput,
            request.getContainers(),
            Optional.of(request),
            request.getRequiresProof(),
            null,
            null,
            null
        ).thenApply(results -> null);
    }

    /**
     * 스트리밍 작업 처리
     */
    public Flux<byte[]> processStreamingComputation(OffchainRequestDTO request) {
        // 스트리밍은 첫 번째 컨테이너만 사용
        String container = request.getContainers().get(0);
        String url = getContainerUrl(container, false);
        Map<String, String> headers = getHeaders(container);

        // 작업 시작
        dataStoreService.setRunning(request);

        List<byte[]> chunks = new ArrayList<>();

        return webClient
            .post()
            .uri(url)
            .headers(httpHeaders -> headers.forEach(httpHeaders::set))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                ComputationInputDTO.builder()
                    .source(ComputationLocation.OFF_CHAIN.name())
                    .destination(ComputationLocation.STREAM.name())
                    .data(request.getData())
                    .build()
            )
            .retrieve()
            .onStatus(
                httpStatus -> httpStatus.isError(),
                response ->
                    response
                        .bodyToMono(String.class)
                        .flatMap(body ->
                            Mono.error(new ContainerException("HTTP Error: " + response.statusCode() + ", Body: " + body, container))
                        )
            )
            .bodyToFlux(byte[].class)
            .timeout(Duration.ofMinutes(1))
            .doOnNext(chunks::add)
            .doOnComplete(() -> {
                // 성공 처리
                try {
                    String finalResult = new String(concatenateChunks(chunks));
                    Map<String, Object> output = Map.of("output", finalResult);
                    List<ContainerResultDTO> results = List.of(new ContainerOutputDTO(container, output, null));

                    dataStoreService.setSuccess(request, results);
                    dataStoreService.trackContainerStatus(container, ContainerStatus.SUCCESS);
                } catch (Exception e) {
                    log.error("Error processing streaming completion", e);
                }
            })
            .doOnError(error -> {
                // 실패 처리
                String errorMessage = error.getMessage();
                log.error("Container streaming error: ComputationId={}, container={}, error={}", request.getId(), container, errorMessage);

                String errorContainer = (error instanceof ContainerException) ? ((ContainerException) error).getContainerId() : container;
                List<ContainerResultDTO> results = List.of(new ContainerErrorDTO(errorContainer, errorMessage));
                dataStoreService.setFailed(request, results);
                dataStoreService.trackContainerStatus(container, ContainerStatus.FAILED);
            });
    }

    /**
     * 서비스 리소스 수집
     */
    public CompletableFuture<Map<String, Map<String, Object>>> collectServiceResources(Optional<String> modelId) {
        ParameterizedTypeReference<Map<String, Object>> mapType = new ParameterizedTypeReference<Map<String, Object>>() {};
        List<ApplicationProperties.NoosphereConfig.NoosphereContainer> configs = containerManager.getConfigs();

        Map<String, CompletableFuture<Map<String, Object>>> futures = new HashMap<>();

        for (ApplicationProperties.NoosphereConfig.NoosphereContainer config : configs) {
            String url = modelId
                .map(id -> String.format("http://%s:%d/service-resources?model_id=%s", host, config.getPort(), id))
                .orElse(String.format("http://%s:%d/service-resources", host, config.getPort()));

            CompletableFuture<Map<String, Object>> future = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(mapType)
                .onErrorReturn(Collections.emptyMap())
                .toFuture();

            futures.put(config.getId(), future);
        }

        // 모든 요청 완료 대기
        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).thenApply(v -> {
            Map<String, Map<String, Object>> results = new HashMap<>();
            futures.forEach((containerId, future) -> {
                try {
                    Map<String, Object> result = future.get();
                    if (result != null && !result.isEmpty()) {
                        results.put(containerId, result);
                    }
                } catch (Exception e) {
                    log.warn("Error fetching data for container {}: {}", containerId, e.getMessage());
                }
            });
            return results;
        });
    }

    private byte[] concatenateChunks(List<byte[]> chunks) {
        int totalLength = chunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, result, offset, chunk.length);
            offset += chunk.length;
        }
        return result;
    }

    /**
     * Helper method to build the input DTO for the proof generation/verification container.
     */
    private ProofInputDTO buildProofInput(
        byte[] requestId,
        byte[] commitment,
        Map<String, Object> Input,
        Map<String, Object> output,
        DelegateeCoordinator.ComputeSubscription subscription
    ) throws JsonProcessingException {
        // Convert byte arrays and strings to hex strings, similar to ethers.hexlify
        String requestIdHex = Numeric.toHexString(requestId);
        String commitmentHex = Numeric.toHexString(commitment);

        String inputString = new ObjectMapper().writeValueAsString(Input);
        String inputHex = Numeric.toHexString(inputString.getBytes(StandardCharsets.UTF_8));

        // Assuming the output map needs to be serialized to a string first
        String outputString = new ObjectMapper().writeValueAsString(output);
        String outputHex = Numeric.toHexString(outputString.getBytes(StandardCharsets.UTF_8));

        String subscriptionString = new ObjectMapper().writeValueAsString(subscription);
        String subscriptionHex = Numeric.toHexString(subscriptionString.getBytes(StandardCharsets.UTF_8));

        return ProofInputDTO.builder()
            .requestId(requestIdHex)
            .commitment(InlineDataDTO.builder().value(commitmentHex).build())
            .input(InlineDataDTO.builder().value(inputHex).build())
            .output(InlineDataDTO.builder().value(outputHex).build())
            .delegatedSubscription(InlineDataDTO.builder().value(subscriptionHex).build())
            .build();
    }
}
