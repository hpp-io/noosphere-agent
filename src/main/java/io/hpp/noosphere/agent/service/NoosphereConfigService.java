package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoosphereConfigService {

    private static final Logger log = LoggerFactory.getLogger(NoosphereConfigService.class);

    private final NoosphereConfigLoader configLoader;
    private ApplicationProperties.NoosphereConfig activeConfig;

    // Spring 4.3+ 에서는 생성자가 하나일 경우 @Autowired 생략 가능
    public NoosphereConfigService(ApplicationProperties applicationProperties, NoosphereConfigLoader configLoader) {
        this.activeConfig = applicationProperties.getNoosphere();
        this.configLoader = configLoader;
    }

    @PostConstruct
    public void initialize() {
        loadConfiguration();
    }

    /**
     * 설정을 로드하고 우선순위에 따라 설정
     * 우선순위: JSON 파일 > 기본값
     */
    private void loadConfiguration() {
        ApplicationProperties.NoosphereConfig jsonConfig = configLoader.loadConfigFromJson();

        if (jsonConfig != null) {
            // JSON 파일이 존재하면 JSON 설정을 사용
            this.activeConfig = jsonConfig;
            log.info("Using configuration from JSON file");
        }

        logConfigurationSummary();
    }

    /**
     * 현재 활성화된 설정 반환
     */
    public ApplicationProperties.NoosphereConfig getActiveConfig() {
        return activeConfig;
    }

    /**
     * 설정 요약 로그 출력
     */
    private void logConfigurationSummary() {
        if (activeConfig != null) {
            // Optional을 사용하여 Null-safe하게 값을 가져오도록 개선
            log.info("Configuration Summary:");
            log.info("  - Manage Containers: {}", activeConfig.getManageContainers());
            log.info("  - Forward Stats: {}", activeConfig.getForwardStats());
            log.info("  - Containers Count: {}", Optional.ofNullable(activeConfig.getContainers()).map(java.util.List::size).orElse(0));
        }
    }

    /**
     * 설정 다시 로드
     */
    public void reloadConfiguration() {
        log.info("Reloading configuration...");
        loadConfiguration();
    }

    /**
     * 현재 설정을 JSON 템플릿으로 저장
     */
    public void saveCurrentConfigAsTemplate(String filePath) {
        configLoader.saveConfigTemplate(activeConfig, filePath);
    }
    // --- Private Helper Methods for Default Config Creation ---
}
