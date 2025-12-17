package io.hpp.noosphere.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.contracts.DelegateeCoordinator;
import io.hpp.noosphere.agent.exception.ContainerException;
import io.hpp.noosphere.agent.service.dto.*;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import io.hpp.noosphere.agent.service.dto.enumeration.ContainerStatus;
import io.hpp.noosphere.agent.service.util.CommonUtil;
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

    /**
     * 컨테이너의 서비스 URL을 가져옵니다 (verifier 주소로 컨테이너를 찾아서)
     */
    private String getProofContainerUrl(String verifierAddress) {
        String proofContainerId = containerManager.getContainerIdByVerifierAddress(verifierAddress);
        if (proofContainerId == null) {
            throw new IllegalStateException("No proof-creator container found for verifier address: " + verifierAddress);
        }
        return getContainerUrl(proofContainerId, true);
    }

    /**
     * 컨테이너의 서비스 URL을 가져옵니다
     */
    private String getContainerUrl(String containerId, boolean isProof) {
        String runtimeEnv = System.getenv("HPP_RUNTIME");
        String endpoint = isProof ? "/api/service_output" : "/computation";

        if ("docker".equals(runtimeEnv)) {
            // DooD mode: Connect via container name and internal port
            String containerName = containerManager.getContainerName(containerId);
            int internalPort = containerManager.getInternalPort(containerId);
            log.debug(
                "Docker container mode detected. Connecting to container Id: {} name: {} port: {}",
                containerId,
                containerName,
                internalPort
            );
            return String.format("http://%s:%d%s", containerName, internalPort, endpoint);
        } else {
            // Local process mode: Connect via localhost and the host-mapped port
            int hostPort = containerManager.getPort(containerId);
            if (hostPort == -1) {
                throw new IllegalStateException("Container port not found or not mapped to host for " + containerId);
            }
            return String.format("http://localhost:%d%s", hostPort, endpoint);
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

            log.debug("Initial input.data (decoded): {}", CommonUtil.decodeInputDataToString(computationInput.getData()));
            // 컨테이너 체인 실행
            for (int index = 0; index < containers.size(); index++) {
                String container = containers.get(index);
                log.debug("container id: {}, input.data: {}", container, CommonUtil.decodeInputDataToString(inputData.getData()));
                String url = getContainerUrl(container, false);
                log.debug("container url: {}", url);
                Map<String, String> headers = getHeaders(container);

                try {
                    // HTTP 요청 실행
                    Map<String, Object> response = webClient
                        .post()
                        .uri(url)
                        .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(
                            inputData.getData() != null ? CommonUtil.decodeInputDataToString(inputData.getData()) : Collections.emptyMap()
                        )
                        .retrieve()
                        .toEntity(String.class) // Receive the response as a String first (including headers)
                        .map(responseEntity -> {
                            String body = responseEntity.getBody();
                            MediaType contentType = responseEntity.getHeaders().getContentType();

                            // If the response is text/plain, wrap it in a Map
                            if (contentType != null && contentType.isCompatibleWith(MediaType.TEXT_PLAIN)) {
                                return Map.<String, Object>of("output", body != null ? body : "");
                            }

                            // If the response is JSON, parse it directly (for future extensibility)
                            try {
                                Map<String, Object> parsedJson = new ObjectMapper()
                                    .readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                                // Wrap the parsed JSON map under the "output" key for consistency
                                return Map.<String, Object>of("output", parsedJson);
                            } catch (Exception e) {
                                // If parsing fails, treat it as text
                                return Map.<String, Object>of("output", body != null ? body : "");
                            }
                        })
                        .timeout(Duration.ofMinutes(4))
                        .block();
                    // 성공 결과 추가
                    Object inputs = inputData.getData();
                    results.add(new ContainerOutputDTO(container, Objects.requireNonNull(response).get("output"), null, inputs));
                    log.info(
                        "ComputationId={}, container={}, inputData={}, result={}",
                        ComputationId,
                        container,
                        computationInput,
                        response
                    );
                    dataStoreService.trackContainerStatus(container, ContainerStatus.SUCCESS);

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

                        // Get the verifier address from subscription and find the corresponding proof-creator container
                        String verifierAddress = subscriptionDTO != null ? subscriptionDTO.getVerifier() : null;
                        if (verifierAddress == null || verifierAddress.isEmpty()) {
                            throw new IllegalStateException(
                                "Verifier address is required for proof generation but not provided in subscription"
                            );
                        }

                        String proofurl = getProofContainerUrl(verifierAddress);
                        log.debug("Proof container URL: {}", proofurl);
                        log.debug("Proof Input DTO: {}", proofInput);

                        String proofResponse = webClient
                            .post()
                            .uri(proofurl)
                            .headers(httpHeaders -> {
                                headers.forEach(httpHeaders::set);
                                httpHeaders.set("Host", "localhost:3000");
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("data", proofInput))
                            .retrieve()
                            .bodyToMono(String.class)
                            .timeout(Duration.ofMinutes(3))
                            .block();

                        log.info(
                            "Proof response received from container. Length: {}, starts with 0x: {}",
                            proofResponse != null ? proofResponse.length() : 0,
                            proofResponse != null && proofResponse.startsWith("0x")
                        );
                        log.debug(
                            "Proof response content (first 100 chars): {}",
                            proofResponse != null && proofResponse.length() > 100 ? proofResponse.substring(0, 100) : proofResponse
                        );

                        // Extract the "proof" field from JSON response (like JavaScript does: responseData.proof)
                        String extractedProof = proofResponse;
                        if (proofResponse != null && proofResponse.trim().startsWith("{")) {
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, Object> responseMap = mapper.readValue(proofResponse, Map.class);
                                if (responseMap.containsKey("proof")) {
                                    extractedProof = (String) responseMap.get("proof");
                                    log.info(
                                        "Extracted 'proof' field from JSON response: {}",
                                        extractedProof != null && extractedProof.length() > 50
                                            ? extractedProof.substring(0, 50) + "..."
                                            : extractedProof
                                    );
                                } else {
                                    log.warn("Response JSON does not contain 'proof' field. Available keys: {}", responseMap.keySet());
                                }
                            } catch (Exception e) {
                                log.error("Failed to parse proof response as JSON: {}", e.getMessage(), e);
                            }
                        }

                        // Cast the last result to ContainerOutputDTO to set the proof.
                        log.debug("Attempting to attach proof to result. Results size: {}", results.size());
                        try {
                            ContainerResultDTO lastResult = results.getLast();
                            log.debug(
                                "Last result type: {}, is ContainerOutputDTO: {}",
                                lastResult.getClass().getSimpleName(),
                                lastResult instanceof ContainerOutputDTO
                            );

                            if (lastResult instanceof ContainerOutputDTO) {
                                ((ContainerOutputDTO) lastResult).setProof(extractedProof);
                                log.info("Proof generated and attached to the result.");
                            } else {
                                log.error("Last result is not ContainerOutputDTO, it is: {}", lastResult.getClass().getName());
                            }
                        } catch (Exception e) {
                            log.error("CRITICAL ERROR: Failed to attach proof to result: {}", e.getMessage(), e);
                            throw e; // Re-throw to see the full error
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
                        CommonUtil.decodeInputDataToString(computationInput.getData()),
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
                    List<ContainerResultDTO> results = List.of(new ContainerOutputDTO(container, output, null, null));

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
            // Use the container's name for inter-container communication, not 'host' variable.
            String containerName = containerManager.getContainerName(config.getId());
            int internalPort = containerManager.getInternalPort(config.getId());

            String url = modelId
                .map(id -> String.format("http://%s:%d/service-resources?model_id=%s", containerName, internalPort, id))
                .orElse(String.format("http://%s:%d/service-resources", containerName, internalPort));

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

        // IMPORTANT: Match JavaScript behavior - use the raw string representation, not JSON
        // JavaScript: ethers.hexlify(ethers.toUtf8Bytes(inputs))
        // If Input is a String, use it directly; otherwise JSON-serialize
        String inputString;
        if (Input.size() == 1 && Input.containsKey("hex_data")) {
            // For hex_data, keep as JSON (this is already structured data)
            inputString = new ObjectMapper().writeValueAsString(Input);
        } else {
            inputString = new ObjectMapper().writeValueAsString(Input);
        }
        String inputHex = Numeric.toHexString(inputString.getBytes(StandardCharsets.UTF_8));

        // Extract the actual output value from the response map
        // response is Map.of("output", actualValue) from line 149/157/160
        Object actualOutput = output.containsKey("output") ? output.get("output") : output;

        // For output: match JavaScript behavior - if it's a string, use it directly
        // JavaScript: ethers.hexlify(ethers.toUtf8Bytes(output))
        String outputString;
        if (actualOutput instanceof String) {
            // Direct string output - matches JavaScript behavior
            outputString = (String) actualOutput;
            log.info(
                "buildProofInput - using raw string output: '{}'",
                outputString.length() > 50 ? outputString.substring(0, 50) + "..." : outputString
            );
        } else {
            // Complex object, use JSON with consistent configuration
            ObjectMapper mapper = new ObjectMapper();
            // Configure for deterministic JSON output
            mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            mapper.configure(com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

            outputString = mapper.writeValueAsString(actualOutput);
            log.info(
                "buildProofInput - JSON-serialized complex output, type: {}, json: {}",
                actualOutput.getClass().getSimpleName(),
                outputString.length() > 100 ? outputString.substring(0, 100) + "..." : outputString
            );
        }
        String outputHex = Numeric.toHexString(outputString.getBytes(StandardCharsets.UTF_8));

        log.info(
            "buildProofInput - outputHex (first 66 chars): {}, full length: {}",
            outputHex.length() > 66 ? outputHex.substring(0, 66) + "..." : outputHex,
            outputHex.length()
        );

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
