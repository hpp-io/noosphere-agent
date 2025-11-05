package io.hpp.noosphere.agent.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hpp.noosphere.agent.config.ApplicationProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoosphereConfigService {

    private static final Logger log = LoggerFactory.getLogger(NoosphereConfigService.class);

    private final NoosphereConfigLoader configLoader;
    private final ObjectMapper mergingMapper;
    private ApplicationProperties.NoosphereConfig activeConfig;

    // Spring 4.3+ 에서는 생성자가 하나일 경우 @Autowired 생략 가능
    public NoosphereConfigService(
        ApplicationProperties applicationProperties,
        NoosphereConfigLoader configLoader,
        ObjectMapper objectMapper
    ) {
        this.activeConfig = applicationProperties.getNoosphere();
        this.configLoader = configLoader;

        // JSON 병합 전용으로 사용할 새로운 ObjectMapper를 생성하고 설정합니다.
        // 이렇게 하면 전역 ObjectMapper 설정에 영향을 주지 않습니다.
        this.mergingMapper = objectMapper.copy();
        // `null` 값은 병합(업데이트) 과정에서 무시하도록 설정합니다.
        this.mergingMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.mergingMapper.setDefaultMergeable(true);
    }

    @PostConstruct
    public void initialize() throws JsonMappingException {
        loadConfiguration();
    }

    /**
     * 설정을 로드하고 우선순위에 따라 설정
     * 우선순위: JSON 파일 > 기본값
     */
    private void loadConfiguration() throws JsonMappingException {
        ApplicationProperties.NoosphereConfig jsonConfig = configLoader.loadConfigFromJson();

        if (jsonConfig == null) {
            log.info("No JSON config file found. Using default configuration from application.yml.");
        } else {
            log.info("JSON config file found. Merging with default configuration.");
            // application.yml 설정을 기반으로 JSON 파일의 설정을 덮어씁니다.
            this.activeConfig = mergeConfigs(this.activeConfig, jsonConfig);
        }

        logConfigurationSummary();
    }

    /**
     * 기본 설정(base)에 덮어쓸 설정(override)을 병합합니다.
     * `override`에 명시된 값만 `base`의 값을 덮어씁니다.
     *
     * @param baseConfig     기본이 되는 설정 (e.g., from application.yml)
     * @param overrideConfig 덮어쓸 설정 (e.g., from config.json)
     * @return 병합된 설정 객체
     */
    private ApplicationProperties.NoosphereConfig mergeConfigs(
        ApplicationProperties.NoosphereConfig baseConfig,
        ApplicationProperties.NoosphereConfig overrideConfig
    ) {
        try {
            // `readerForUpdating`을 사용하여 overrideConfig의 non-null 필드만 baseConfig에 덮어씁니다.
            return mergingMapper.readerForUpdating(baseConfig).readValue(mergingMapper.writeValueAsString(overrideConfig));
        } catch (IOException e) {
            log.error("Failed to merge configurations. Falling back to default config.", e);
            return baseConfig; // 병합 실패 시 기본 설정으로 복구
        }
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
    public void reloadConfiguration() throws JsonMappingException {
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
