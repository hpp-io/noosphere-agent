package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.exception.ContainerException;
import io.hpp.noosphere.agent.service.dto.*;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import io.hpp.noosphere.agent.service.dto.enumeration.ContainerStatus;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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
    private String getContainerUrl(String container) {
        String containerUrl = containerManager.getUrl(container);
        if (containerUrl != null && !containerUrl.isEmpty()) {
            return containerUrl + "/service_output";
        } else {
            int port = containerManager.getPort(container);
            return String.format("http://%s:%d/service_output", host, port);
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
        ComputationInputDTO ComputationInput,
        List<String> containers,
        Optional<OffchainRequestDTO> request,
        Boolean requiresProof
    ) {
        return CompletableFuture.supplyAsync(() -> {
            // 작업 시작
            request.ifPresent(dataStoreService::setRunning);

            List<ContainerResultDTO> results = new ArrayList<>();

            // 첫 번째 컨테이너 입력 데이터 설정
            ContainerInputDTO inputData = ContainerInputDTO.builder()
                .source(ComputationInput.getSource())
                .destination(containers.size() == 1 ? ComputationInput.getDestination() : ComputationLocation.OFF_CHAIN.name())
                .data(ComputationInput.getData())
                .requiresProof(requiresProof)
                .build();

            // 컨테이너 체인 실행
            for (int index = 0; index < containers.size(); index++) {
                String container = containers.get(index);
                String url = getContainerUrl(container);
                Map<String, String> headers = getHeaders(container);

                try {
                    // HTTP 요청 실행
                    Map<String, Object> response = webClient
                        .post()
                        .uri(url)
                        .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(inputData.getData() != null ? inputData.getData() : Collections.emptyMap())
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofMinutes(3))
                        .block();

                    // 성공 결과 추가
                    results.add(new ContainerOutputDTO(container, response));
                    dataStoreService.trackContainerStatus(container, ContainerStatus.SUCCESS);

                    // 다음 컨테이너를 위한 입력 데이터 준비
                    if (index < containers.size() - 1) {
                        inputData = ContainerInputDTO.builder()
                            .source(ComputationLocation.OFF_CHAIN.toString())
                            .destination(
                                index == containers.size() - 2
                                    ? ComputationInput.getDestination()
                                    : ComputationLocation.OFF_CHAIN.toString()
                            )
                            .data(response)
                            .requiresProof(requiresProof)
                            .build();
                    }
                } catch (Exception e) {
                    // 오류 처리
                    ContainerErrorDTO error = new ContainerErrorDTO(container, e.getMessage());
                    results.add(error);

                    log.error("Container error: ComputationId={}, container={}, error={}", ComputationId, container, e.getMessage());

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
        boolean requiresProof
    ) {
        return runComputation(ComputationId, ComputationInput, containers, Optional.empty(), requiresProof);
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
            request.getRequiresProof()
        ).thenApply(results -> null);
    }

    /**
     * 스트리밍 작업 처리
     */
    public Flux<byte[]> processStreamingComputation(OffchainRequestDTO request) {
        // 스트리밍은 첫 번째 컨테이너만 사용
        String container = request.getContainers().get(0);
        String url = getContainerUrl(container);
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
                    List<ContainerResultDTO> results = List.of(new ContainerOutputDTO(container, output));

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
        List<ApplicationProperties.NoosphereContainer> configs = containerManager.getConfigs();

        Map<String, CompletableFuture<Map<String, Object>>> futures = new HashMap<>();

        for (ApplicationProperties.NoosphereContainer config : configs) {
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
}
