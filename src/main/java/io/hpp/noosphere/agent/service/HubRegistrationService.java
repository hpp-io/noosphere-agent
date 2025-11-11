package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.service.blockchain.WalletService;
import io.hpp.noosphere.agent.service.dto.AgentDTO;
import io.hpp.noosphere.agent.service.dto.AgentRegistrationDTO;
import io.hpp.noosphere.agent.service.util.CommonUtil;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class HubRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(HubRegistrationService.class);

    private final WalletService walletService;
    private final RestTemplate restTemplate;
    private final ApplicationProperties.NoosphereConfig noosphereConfig;
    private final AgentService agentService;

    public HubRegistrationService(NoosphereConfigService noosphereConfigService, WalletService walletService, AgentService agentService) {
        this.noosphereConfig = noosphereConfigService.getActiveConfig();
        this.walletService = walletService;
        this.agentService = agentService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Starts the hub registration process once the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerWithHubOnStartup() {
        ApplicationProperties.Hub hubConfig = noosphereConfig.getHub();
        if (hubConfig == null || !hubConfig.getRegister()) {
            log.info("Hub registration is disabled in the configuration.");
            return;
        }

        CompletableFuture.runAsync(this::checkAndRegister).join();
    }

    /**
     * Checks if the agent is already registered with the hub,
     * and if not, attempts a new registration.
     */
    private void checkAndRegister() {
        String hubUrl = noosphereConfig.getHub().getUrl();
        String checkUrl = hubUrl + "/api/agents/";
        UUID registeredId = agentService.getRegisteredAgent().getId();
        try {
            // 1. Check if the agent is already registered with the hub.
            log.info("Checking registration status with hub at: {}", checkUrl);
            if (registeredId != null) {
                restTemplate.getForObject(checkUrl + registeredId, String.class);
            } else {
                registerNewAgent(hubUrl, walletService.getAddress());
            }
            log.info("Agent {} is already registered with the hub.", registeredId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // 2. If not registered (404 Not Found), register a new agent.
                log.info("Agent not found on hub. Proceeding with registration...");
                registerNewAgent(hubUrl, walletService.getAddress());
            } else {
                log.error("Error checking agent registration status: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to connect to the hub at {}: {}", hubUrl, e.getMessage());
        }
    }

    /**
     * Registers the agent by sending its information to the hub via a POST request.
     */
    private void registerNewAgent(String hubUrl, String agentAddress) {
        ApplicationProperties.Agent agentConfig = noosphereConfig.getAgent();

        if (agentConfig.getApiKey() == null || agentConfig.getEmail() == null) {
            log.warn("Agent's name, apiKey, or email is not configured. Skipping hub registration.");
            return;
        }

        AgentRegistrationDTO registrationDTO = AgentRegistrationDTO.builder()
            .name(CommonUtil.isValid(agentConfig.getName()) ? agentConfig.getName() : agentAddress)
            .apiKey(agentConfig.getApiKey())
            .walletAddress(agentAddress)
            .email(agentConfig.getEmail())
            .build();

        String registerUrl = hubUrl + "/api/agents/register";
        try {
            AgentDTO agentDTO = restTemplate.postForObject(registerUrl, registrationDTO, AgentDTO.class);
            if (agentDTO != null) {
                agentService.syncAgent(agentDTO);
            }
            log.info("Successfully registered agent {} with the hub.", agentDTO);
        } catch (Exception e) {
            log.error("Failed to register agent with the hub: {}", e.getMessage());
        }
    }
}
