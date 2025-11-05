package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.service.blockchain.WalletService;
import io.hpp.noosphere.agent.service.dto.KeepAliveResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HubKeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(HubKeepAliveService.class);

    private final ApplicationProperties applicationProperties;
    private final WalletService walletService;
    private final RestTemplate restTemplate;
    private HubRegistrationService hubRegistrationService;

    public HubKeepAliveService(ApplicationProperties applicationProperties, WalletService walletService) {
        this.applicationProperties = applicationProperties;
        this.walletService = walletService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Periodically sends a keep-alive signal to the noosphere-hub.
     * The interval is configured in application.yml.
     */
    @Scheduled(fixedDelayString = "${application.noosphere.hub.keep-alive.interval-ms}")
    public void sendKeepAlive() {
        ApplicationProperties.Hub hubConfig = applicationProperties.getNoosphere().getHub();
        if (hubConfig == null || !hubConfig.getKeepAlive().isEnabled()) {
            // Keep-alive is disabled, do nothing.
            return;
        }

        String agentAddress = hubRegistrationService.getAgentId() != null
            ? hubRegistrationService.getAgentId().toString()
            : walletService.getAddress();
        String hubUrl = hubConfig.getUrl();
        String keepAliveUrl = hubUrl + "/api/agents/" + agentAddress + "/keep-alive";

        try {
            log.debug("Sending keep-alive ping to hub for agent {}", agentAddress);

            // Send a POST request and expect a KeepAliveResponseDTO
            KeepAliveResponseDTO response = restTemplate.postForObject(keepAliveUrl, null, KeepAliveResponseDTO.class);

            if (response != null && response.getStatusCode() == 200) {
                log.info(
                    "Successfully sent keep-alive signal for agent {}. Hub responded with status {}.",
                    agentAddress,
                    response.getStatusCode()
                );
            } else {
                log.warn(
                    "Received a non-200 status from hub keep-alive: {}",
                    response != null ? response.getStatusCode() : "null response"
                );
            }
        } catch (Exception e) {
            log.error("Failed to send keep-alive signal to hub at {}: {}", keepAliveUrl, e.getMessage());
        }
    }
}
