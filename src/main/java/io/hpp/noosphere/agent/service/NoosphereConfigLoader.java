package io.hpp.noosphere.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.hpp.noosphere.agent.config.ApplicationProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NoosphereConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(NoosphereConfigLoader.class);

    @Value("${application.noosphere.configFilePath}")
    private String configFileName;

    private final ObjectMapper objectMapper;

    public NoosphereConfigLoader() {
        this.objectMapper = new ObjectMapper();
        // JDK8 모듈 등록 (Optional 지원)
        this.objectMapper.registerModule(new Jdk8Module());
    }

    /**
     * JSON 설정 파일을 로드하여 NoosphereConfig 객체로 반환
     *
     * @return NoosphereConfig 객체, 파일이 없거나 오류 시 null
     */
    public ApplicationProperties.NoosphereConfig loadConfigFromJson() {
        try {
            Path configPath = findConfigFile();
            if (configPath == null) {
                LOG.info("No external config file found. Using default configuration.");
                return null;
            }

            LOG.info("Loading configuration from: {}", configPath.toAbsolutePath());
            String jsonContent = Files.readString(configPath);

            ApplicationProperties.NoosphereConfig config = objectMapper.readValue(jsonContent, ApplicationProperties.NoosphereConfig.class);
            LOG.info("Successfully loaded configuration from JSON file");
            return config;
        } catch (IOException e) {
            LOG.error("Failed to load configuration from JSON file", e);
            return null;
        }
    }

    /**
     * 설정 파일을 여러 위치에서 찾기
     * 우선순위: 1) 현재 디렉터리, 2) config 디렉터리, 3) 홈 디렉터리, 4) /etc 디렉터리
     */
    private Path findConfigFile() {
        String[] searchPaths = {
            ".", // 현재 디렉터리
            "./config", // config 디렉터리
            System.getProperty("user.home"), // 홈 디렉터리
            "/etc/noosphere-agent", // 시스템 설정 디렉터리
        };

        for (String searchPath : searchPaths) {
            Path configPath = Paths.get(searchPath, configFileName);
            if (Files.exists(configPath) && Files.isReadable(configPath)) {
                LOG.debug("Found config file at: {}", configPath.toAbsolutePath());
                return configPath;
            }
        }

        return null;
    }

    /**
     * NoosphereConfig 객체를 JSON 파일로 저장 (템플릿 생성용)
     */
    public void saveConfigTemplate(ApplicationProperties.NoosphereConfig config, String filePath) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), config);
            LOG.info("Configuration template saved to: {}", filePath);
        } catch (IOException e) {
            LOG.error("Failed to save configuration template", e);
        }
    }
}
