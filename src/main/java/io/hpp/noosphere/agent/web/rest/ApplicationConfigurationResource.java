package io.hpp.noosphere.agent.web.rest;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.service.ApplicationConfigurationService;
import io.hpp.noosphere.agent.service.dto.ValidationResultDTO;
import jakarta.validation.Valid;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;

/**
 * REST controller for managing Application Configuration.
 */
@RestController
@RequestMapping("/api/application-configuration")
public class ApplicationConfigurationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfigurationResource.class);

    private static final String ENTITY_NAME = "applicationConfiguration";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ApplicationConfigurationService applicationConfigurationService;

    public ApplicationConfigurationResource(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }

    /**
     * {@code GET  /application-configuration} : get the current application configuration.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the current configuration.
     */
    @GetMapping("")
    public ResponseEntity<ApplicationProperties> getApplicationConfiguration() {
        LOG.debug("REST request to get Application Configuration");
        ApplicationProperties config = applicationConfigurationService.getCurrentConfiguration();
        return ResponseEntity.ok().body(config);
    }

    /**
     * {@code PUT  /application-configuration} : Updates an existing application configuration.
     *
     * @param applicationProperties the application configuration to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated configuration,
     * or with status {@code 400 (Bad Request)} if the configuration is not valid,
     * or with status {@code 500 (Internal Server Error)} if the configuration couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("")
    public ResponseEntity<ApplicationProperties> updateApplicationConfiguration(
        @Valid @RequestBody ApplicationProperties applicationProperties
    ) throws URISyntaxException {
        LOG.debug("REST request to update Application Configuration : {}", applicationProperties);

        try {
            ApplicationProperties result = applicationConfigurationService.updateConfiguration(applicationProperties);
            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, "configuration"))
                .body(result);
        } catch (Exception e) {
            LOG.error("Failed to update application configuration", e);
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(applicationName, true, ENTITY_NAME, "updatefailed", e.getMessage()))
                .build();
        }
    }

    /**
     * {@code POST  /application-configuration/reload} : Reload the application configuration from file.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the reloaded configuration.
     */
    @PostMapping("/reload")
    public ResponseEntity<ApplicationProperties> reloadApplicationConfiguration() {
        LOG.debug("REST request to reload Application Configuration");

        try {
            ApplicationProperties config = applicationConfigurationService.reloadConfiguration();
            return ResponseEntity.ok()
                .headers(HeaderUtil.createAlert(applicationName, "Configuration reloaded successfully", "configuration"))
                .body(config);
        } catch (Exception e) {
            LOG.error("Failed to reload application configuration", e);
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(applicationName, true, ENTITY_NAME, "reloadfailed", e.getMessage()))
                .build();
        }
    }

    /**
     * {@code POST  /application-configuration/validate} : Validate application configuration.
     *
     * @param applicationProperties the configuration to validate.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and validation result.
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResultDTO> validateApplicationConfiguration(
        @Valid @RequestBody ApplicationProperties applicationProperties
    ) {
        LOG.debug("REST request to validate Application Configuration");

        ValidationResultDTO result = applicationConfigurationService.validateConfiguration(applicationProperties);
        return ResponseEntity.ok().body(result);
    }
}
