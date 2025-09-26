package io.hpp.noosphere.agent.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import io.hpp.noosphere.agent.config.ApplicationProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ContainerManagerService {

    private static final Logger log = LoggerFactory.getLogger(ContainerManagerService.class);

    private final NoosphereConfigService noosphereConfigService;

    private Double startupWait;
    private Boolean managed;
    private DockerClient dockerClient;
    private List<ApplicationProperties.NoosphereContainer> configs;
    private ApplicationProperties.Docker credentials;

    // 동적 포트 매핑을 위한 맵들
    private final Map<String, Integer> portMappings = new ConcurrentHashMap<>();
    private final Map<String, String> urlMappings = new ConcurrentHashMap<>();
    private final Map<String, String> bearerMappings = new ConcurrentHashMap<>();
    private final Map<String, String> containers = new ConcurrentHashMap<>();
    private final Set<String> images = new HashSet<>();

    public ContainerManagerService(NoosphereConfigService noosphereConfigService) {
        this.noosphereConfigService = noosphereConfigService;
    }

    @PostConstruct
    public void initialize() {
        ApplicationProperties.NoosphereConfig config = noosphereConfigService.getActiveConfig();
        this.startupWait = config.getStartupWait();
        this.managed = config.getManageContainers();
        this.configs = config.getContainers();
        this.credentials = config.getDocker();

        log.info("Initializing ContainerManagerService - managed: {}, containers: {}", managed, configs.size());

        if (managed) {
            this.dockerClient = initializeDockerClient();
            if (dockerClient != null) {
                log.info("Docker client successfully initialized");
            } else {
                log.error("Failed to initialize Docker client");
            }
        }
    }

    /**
     * Docker 클라이언트 초기화 - 여러 방법을 시도하여 연결
     */
    private DockerClient initializeDockerClient() {
        List<String> dockerHosts = getDockerHostCandidates();
        Exception lastException = null;

        for (String dockerHost : dockerHosts) {
            try {
                log.debug("Trying Docker host: {}", dockerHost);

                DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(dockerHost);

                if (credentials != null && credentials.getUsername() != null) {
                    configBuilder.withRegistryUsername(credentials.getUsername()).withRegistryPassword(credentials.getPassword());
                }

                DefaultDockerClientConfig config = configBuilder.build();

                ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();

                DockerClient client = DockerClientBuilder.getInstance(config).withDockerHttpClient(httpClient).build();

                // 연결 테스트
                client.versionCmd().exec();
                log.info("Successfully connected to Docker at: {}", dockerHost);
                return client;
            } catch (Exception e) {
                lastException = e;
                log.warn("Failed to connect to Docker at {}: {}", dockerHost, e.getMessage());
            }
        }

        log.error("Failed to initialize Docker client after trying all candidates: {}", buildDetailedErrorMessage(lastException));
        return null;
    }

    /**
     * 운영체제별 Docker 호스트 후보 목록을 생성합니다
     */
    private List<String> getDockerHostCandidates() {
        List<String> candidates = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            candidates.add("npipe://./pipe/docker_engine");
            candidates.add("tcp://localhost:2375");
            candidates.add("tcp://localhost:2376");
        } else if (os.contains("mac")) {
            candidates.add("unix:///var/run/docker.sock");
            candidates.add("unix:///Users/" + System.getProperty("user.name") + "/.docker/run/docker.sock");
            candidates.add("tcp://localhost:2375");
        } else {
            candidates.add("unix:///var/run/docker.sock");
            candidates.add("tcp://localhost:2375");
            candidates.add("tcp://localhost:2376");
        }

        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost != null && !dockerHost.isEmpty()) {
            candidates.add(0, dockerHost);
        }

        return candidates.stream().filter(host -> !host.startsWith("tcp://") || isPortAccessible(host)).collect(Collectors.toList());
    }

    /**
     * TCP 포트 접근성을 확인합니다
     */
    private boolean isPortAccessible(String dockerHost) {
        if (!dockerHost.startsWith("tcp://")) return true;

        try {
            String[] parts = dockerHost.replace("tcp://", "").split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 3000);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 상세한 오류 메시지를 생성합니다
     */
    private String buildDetailedErrorMessage(Exception lastException) {
        StringBuilder message = new StringBuilder();
        message.append("Docker connection failed. ");

        if (lastException != null) {
            message.append("Last error: ").append(lastException.getMessage()).append(". ");
        }

        message.append("Please ensure Docker is running and accessible.");
        return message.toString();
    }

    /**
     * 컨테이너 설정 추가
     */
    public void addConfig(ApplicationProperties.NoosphereContainer config) {
        if (configs == null) {
            configs = new ArrayList<>();
        }
        configs.add(config);
        log.info("Added container config: {}", config.getId());
    }

    public List<ApplicationProperties.NoosphereContainer> getConfigs() {
        return configs != null ? configs : new ArrayList<>();
    }

    /**
     * 포트 매핑 반환
     */
    public Map<String, Integer> getPortMappings() {
        return new HashMap<>(portMappings);
    }

    /**
     * 컨테이너 포트 조회 (동적 매핑된 포트)
     */
    public Integer getPort(String containerId) {
        Integer dynamicPort = portMappings.get(containerId);
        if (dynamicPort != null) {
            log.debug("Found dynamic port for container {}: {}", containerId, dynamicPort);
            return dynamicPort;
        }

        // 설정에서 기본 포트 조회
        return configs
            .stream()
            .filter(config -> config.getId().equals(containerId))
            .findFirst()
            .map(ApplicationProperties.NoosphereContainer::getPort)
            .orElse(null);
    }

    /**
     * 컨테이너 URL 조회
     */
    public String getUrl(String containerId) {
        String dynamicUrl = urlMappings.get(containerId);
        if (dynamicUrl != null) {
            return dynamicUrl;
        }

        // 설정에서 기본 URL 조회
        return configs
            .stream()
            .filter(config -> config.getId().equals(containerId))
            .findFirst()
            .map(ApplicationProperties.NoosphereContainer::getUrl)
            .orElse(null);
    }

    /**
     * 컨테이너 Bearer 토큰 조회
     */
    public String getBearer(String containerId) {
        String dynamicBearer = bearerMappings.get(containerId);
        if (dynamicBearer != null) {
            return dynamicBearer;
        }

        // 설정에서 기본 Bearer 조회
        return configs
            .stream()
            .filter(config -> config.getId().equals(containerId))
            .findFirst()
            .map(ApplicationProperties.NoosphereContainer::getBearer)
            .orElse(null);
    }

    /**
     * 컨테이너 설정 및 실행
     */
    @Async
    public CompletableFuture<Void> setup() {
        if (!managed || dockerClient == null) {
            log.info("Container management is disabled or Docker client is not available");
            return CompletableFuture.completedFuture(null);
        }

        try {
            log.info("Starting container setup process");
            pullImages();
            pruneContainers();
            runContainers();

            // 시작 대기
            Thread.sleep((long) (startupWait * 1000));
            log.info("Container setup completed");

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Container setup failed", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 무한 실행 (헬스체크 및 모니터링)
     */
    @Scheduled(fixedDelay = 30000)
    public void runForever() {
        if (!managed || dockerClient == null) {
            return;
        }

        try {
            for (ApplicationProperties.NoosphereContainer config : configs) {
                String containerName = "noosphere-" + config.getId();

                if (!isContainerHealthy(containerName)) {
                    log.warn("Container {} is not healthy, attempting restart", containerName);
                    restartContainer(config);
                }
            }
        } catch (Exception e) {
            log.error("Error during container health check", e);
        }
    }

    /**
     * 컨테이너 중지
     */
    @PreDestroy
    public void stop() {
        if (!managed || dockerClient == null) {
            return;
        }

        log.info("Stopping all managed containers");
        for (String containerId : containers.keySet()) {
            try {
                String containerName = containers.get(containerId);
                dockerClient.stopContainerCmd(containerName).exec();
                log.info("Stopped container: {}", containerName);
            } catch (Exception e) {
                log.error("Failed to stop container {}", containerId, e);
            }
        }
    }

    /**
     * 리소스 정리
     */
    public void cleanup() {
        if (dockerClient != null) {
            try {
                dockerClient.close();
                log.info("Docker client closed");
            } catch (IOException e) {
                log.error("Failed to close Docker client", e);
            }
        }
    }

    /**
     * 이미지 Pull
     */
    private void pullImages() {
        for (ApplicationProperties.NoosphereContainer config : configs) {
            try {
                log.info("Pulling image: {}", config.getImage());
                dockerClient
                    .pullImageCmd(config.getImage())
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(5, java.util.concurrent.TimeUnit.MINUTES);

                images.add(config.getImage());
                log.info("Successfully pulled image: {}", config.getImage());
            } catch (Exception e) {
                log.error("Failed to pull image {}: {}", config.getImage(), e.getMessage());
            }
        }
    }

    /**
     * 기존 컨테이너 정리
     */
    private void pruneContainers() {
        for (ApplicationProperties.NoosphereContainer config : configs) {
            String containerName = "noosphere-" + config.getId();
            try {
                List<Container> existingContainers = dockerClient
                    .listContainersCmd()
                    .withShowAll(true)
                    .withNameFilter(Collections.singleton(containerName))
                    .exec();

                for (Container container : existingContainers) {
                    log.info("Removing existing container: {}", containerName);
                    dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
                }
            } catch (Exception e) {
                log.warn("Failed to remove existing container {}: {}", containerName, e.getMessage());
            }
        }
    }

    /**
     * 컨테이너 실행
     */
    private void runContainers() {
        for (ApplicationProperties.NoosphereContainer config : configs) {
            try {
                createAndStartContainer(config);
            } catch (Exception e) {
                log.error("Failed to create and start container {}: {}", config.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 컨테이너 생성 및 시작 (동적 포트 매핑 적용)
     */
    private void createAndStartContainer(ApplicationProperties.NoosphereContainer config) {
        String containerName = "noosphere-" + config.getId();

        try {
            // 포트 바인딩 설정 (동적 할당)
            List<ExposedPort> exposedPorts = new ArrayList<>();
            Ports portBindings = new Ports();

            if (config.getPort() != null) {
                ExposedPort exposedPort = ExposedPort.tcp(config.getPort());
                exposedPorts.add(exposedPort);
                // 호스트 포트를 null로 설정하면 Docker가 자동으로 사용 가능한 포트 할당
                portBindings.bind(exposedPort, Ports.Binding.empty());
            }

            // 환경 변수 설정
            List<String> envList = config
                .getEnv()
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList());

            // 볼륨 바인딩 설정
            List<Bind> binds = config
                .getVolumes()
                .stream()
                .map(volume -> {
                    String[] parts = volume.split(":");
                    return new Bind(parts[0], new Volume(parts[1]));
                })
                .collect(Collectors.toList());

            // 컨테이너 생성
            CreateContainerResponse container = dockerClient
                .createContainerCmd(config.getImage())
                .withName(containerName)
                .withExposedPorts(exposedPorts)
                .withPortBindings(portBindings)
                .withEnv(envList)
                .withBinds(binds)
                .withCmd(config.getCommand() != null ? config.getCommand().split(" ") : null)
                .exec();

            // 컨테이너 시작
            dockerClient.startContainerCmd(container.getId()).exec();
            log.info("Started container: {} ({})", containerName, container.getId());

            // 컨테이너 정보 저장
            containers.put(config.getId(), containerName);

            // 동적으로 할당된 포트 정보 가져오기
            updateDynamicPortMappings(config, container.getId());

            // 컨테이너 상태 검증
            validateContainerHealth(containerName);
        } catch (DockerException e) {
            log.error("Docker error creating container {}: {}", containerName, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating container {}: {}", containerName, e.getMessage(), e);
            throw new RuntimeException("Failed to create container: " + containerName, e);
        }
    }

    /**
     * 동적 포트 매핑 정보 업데이트
     */
    private void updateDynamicPortMappings(ApplicationProperties.NoosphereContainer config, String containerId) {
        try {
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();

            if (containerInfo.getNetworkSettings() != null && containerInfo.getNetworkSettings().getPorts() != null) {
                Map<ExposedPort, Ports.Binding[]> portBindings = containerInfo.getNetworkSettings().getPorts().getBindings();

                for (Map.Entry<ExposedPort, Ports.Binding[]> entry : portBindings.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().length > 0) {
                        Ports.Binding binding = entry.getValue()[0];
                        Integer hostPort = Integer.valueOf(binding.getHostPortSpec());

                        // 동적 포트 매핑 저장
                        portMappings.put(config.getId(), hostPort);

                        // URL 매핑 업데이트
                        String dynamicUrl = String.format("http://localhost:%d", hostPort);
                        urlMappings.put(config.getId(), dynamicUrl);

                        // Bearer 토큰이 있으면 매핑에 추가
                        if (config.getBearer() != null && !config.getBearer().isEmpty()) {
                            bearerMappings.put(config.getId(), config.getBearer());
                        }

                        log.info("Dynamic port mapping for container {}: {} -> {}", config.getId(), entry.getKey().getPort(), hostPort);

                        break; // 첫 번째 포트 매핑만 사용
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to update dynamic port mappings for container {}: {}", config.getId(), e.getMessage(), e);
        }
    }

    /**
     * 컨테이너 상태 검증
     */
    private void validateContainerHealth(String containerName) {
        try {
            // 컨테이너가 실행 중인지 확인
            List<Container> runningContainers = dockerClient
                .listContainersCmd()
                .withNameFilter(Collections.singleton(containerName))
                .exec();

            if (runningContainers.isEmpty()) {
                throw new RuntimeException("Container is not running: " + containerName);
            }

            Container container = runningContainers.get(0);
            if (!"running".equals(container.getState())) {
                throw new RuntimeException("Container state is not running: " + container.getState());
            }

            log.info("Container {} is healthy and running", containerName);
        } catch (Exception e) {
            log.error("Container health validation failed for {}: {}", containerName, e.getMessage());
            throw new RuntimeException("Container health check failed", e);
        }
    }

    /**
     * 실행 중인 컨테이너 정보
     */
    public Map<String, String> getRunningContainerInfo() {
        Map<String, String> info = new HashMap<>();

        if (!managed || dockerClient == null) {
            info.put("status", "Docker management is disabled");
            return info;
        }

        try {
            List<Container> runningContainers = dockerClient.listContainersCmd().exec();

            for (Container container : runningContainers) {
                String containerName = container.getNames()[0].substring(1); // Remove leading '/'
                if (containerName.startsWith("noosphere-")) {
                    String containerId = containerName.substring("noosphere-".length());
                    Integer port = portMappings.get(containerId);
                    String status = String.format("Running on port %s, State: %s", port != null ? port : "unknown", container.getState());
                    info.put(containerId, status);
                }
            }

            if (info.isEmpty()) {
                info.put("status", "No running noosphere containers found");
            }
        } catch (Exception e) {
            log.error("Failed to get running container info", e);
            info.put("error", "Failed to retrieve container information: " + e.getMessage());
        }

        return info;
    }

    /**
     * 컨테이너 재시작
     */
    private void restartContainer(ApplicationProperties.NoosphereContainer config) {
        String containerName = "noosphere-" + config.getId();

        try {
            log.info("Restarting container: {}", containerName);

            // 기존 컨테이너 중지 및 제거
            List<Container> existingContainers = dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .withNameFilter(Collections.singleton(containerName))
                .exec();

            for (Container container : existingContainers) {
                dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
                log.info("Removed existing container: {}", containerName);
            }

            // 포트 매핑 정리
            portMappings.remove(config.getId());
            urlMappings.remove(config.getId());
            bearerMappings.remove(config.getId());

            // 새 컨테이너 생성 및 시작
            createAndStartContainer(config);

            log.info("Successfully restarted container: {}", containerName);
        } catch (Exception e) {
            log.error("Failed to restart container {}: {}", containerName, e.getMessage(), e);
        }
    }

    /**
     * 컨테이너 강제 재생성 (기존 컨테이너 제거 후 새로 생성)
     */
    private void recreateContainer(ApplicationProperties.NoosphereContainer config) {
        restartContainer(config); // 현재는 재시작과 동일한 로직 사용
    }

    /**
     * 컨테이너 상태 확인 및 복구
     */
    private boolean isContainerHealthy(String containerName) {
        try {
            List<Container> containers = dockerClient.listContainersCmd().withNameFilter(Collections.singleton(containerName)).exec();

            if (containers.isEmpty()) {
                log.debug("Container {} not found", containerName);
                return false;
            }

            Container container = containers.get(0);
            boolean isRunning = "running".equals(container.getState());

            if (!isRunning) {
                log.debug("Container {} is not running: {}", containerName, container.getState());
            }

            return isRunning;
        } catch (Exception e) {
            log.error("Failed to check container health for {}: {}", containerName, e.getMessage());
            return false;
        }
    }
}
