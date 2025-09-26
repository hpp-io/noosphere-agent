package io.hpp.noosphere.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.service.dto.ValidationResultDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service class for managing application configuration.
 */
@Service
public class ApplicationConfigurationService {

    private final Logger log = LoggerFactory.getLogger(ApplicationConfigurationService.class);

    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ApplicationConfigurationService(ApplicationProperties applicationProperties, ObjectMapper objectMapper, Validator validator) {
        this.applicationProperties = applicationProperties;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Get the current application configuration.
     *
     * @return the current configuration
     */
    public ApplicationProperties getCurrentConfiguration() {
        log.debug("Getting current application configuration");
        return applicationProperties;
    }

    /**
     * Update the application configuration and save it to file.
     *
     * @param newConfig the new configuration to save
     * @return the updated configuration
     * @throws IOException if an I/O error occurs during file writing
     */
    public ApplicationProperties updateConfiguration(ApplicationProperties newConfig) throws IOException {
        log.debug("Updating application configuration: {}", newConfig);

        // 설정 검증
        ValidationResultDTO validationResult = validateConfiguration(newConfig);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Invalid configuration: " + validationResult.getErrors());
        }

        // 파일에 저장
        String configFilePath = applicationProperties.getNoosphere().getConfigFilePath();
        File configFile = new File(configFilePath);

        // 부모 디렉토리가 없으면 생성
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                log.warn("Failed to create parent directories for config file: {}", configFilePath);
            }
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, newConfig);

        log.info("Configuration saved to file: {}", configFilePath);

        return newConfig;
    }

    /**
     * Reload configuration from file.
     *
     * @return the reloaded configuration
     * @throws IOException if an I/O error occurs during file reading
     */
    public ApplicationProperties reloadConfiguration() throws IOException {
        log.debug("Reloading application configuration from file");

        String configFilePath = applicationProperties.getNoosphere().getConfigFilePath();
        File configFile = new File(configFilePath);

        if (!configFile.exists()) {
            log.warn("Configuration file does not exist: {}", configFilePath);
            return getCurrentConfiguration();
        }

        ApplicationProperties reloadedConfig = objectMapper.readValue(configFile, ApplicationProperties.class);
        log.info("Configuration reloaded from file: {}", configFilePath);

        return reloadedConfig;
    }

    /**
     * Validate the given configuration.
     *
     * @param config the configuration to validate
     * @return validation result
     */
    public ValidationResultDTO validateConfiguration(ApplicationProperties config) {
        log.debug("Validating application configuration");

        ValidationResultDTO result = new ValidationResultDTO();

        try {
            Set<ConstraintViolation<ApplicationProperties>> violations = validator.validate(config);

            if (violations.isEmpty()) {
                result.setValid(true);
                result.setMessage("Configuration is valid");
            } else {
                result.setValid(false);
                result.setMessage("Configuration validation failed");
                result.setErrors(
                    violations
                        .stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .collect(Collectors.toList())
                );
            }
        } catch (Exception e) {
            log.error("Error during configuration validation", e);
            result.setValid(false);
            result.setMessage("Validation error: " + e.getMessage());
        }

        return result;
    }
}
