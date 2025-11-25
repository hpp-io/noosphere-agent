package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.service.blockchain.BlockChainService;
import io.hpp.noosphere.agent.service.dto.DelegatedRequestDTO;
import io.hpp.noosphere.agent.service.dto.KeepAliveResponseDTO;
import io.hpp.noosphere.agent.service.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HubKeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(HubKeepAliveService.class);

    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;
    private final AgentService agentService;
    private final BlockChainService blockChainService;

    public HubKeepAliveService(
        ApplicationProperties applicationProperties,
        RestTemplate restTemplate,
        AgentService agentService,
        BlockChainService blockChainService
    ) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = restTemplate;
        this.agentService = agentService;
        this.blockChainService = blockChainService;
    }

    /**
     * Periodically sends a keep-alive signal to the noosphere-hub.
     * The interval is configured in application.yml.
     */
    @Scheduled(fixedDelayString = "${application.noosphere.hub.keep-alive.interval-ms}")
    public void sendKeepAlive() {
        ApplicationProperties.NoosphereConfig.Hub hubConfig = applicationProperties.getNoosphere().getHub();
        if (hubConfig == null || !hubConfig.getKeepAlive().getEnabled() || agentService.getRegisteredAgent() == null) {
            // Keep-alive is disabled, do nothing.
            return;
        }

        String agentId = agentService.getRegisteredAgent().getId().toString();
        String hubUrl = hubConfig.getUrl();
        String keepAliveUrl = hubUrl + "/api/agents/" + agentId + "/keep-alive";

        try {
            log.debug("Sending keep-alive ping to hub for agent {}", agentId);

            // Send a POST request and expect a KeepAliveResponseDTO
            KeepAliveResponseDTO response = restTemplate.postForObject(keepAliveUrl, null, KeepAliveResponseDTO.class);

            if (response != null && CommonUtil.isValid(response.getCount()) && response.getCount() > 0) {
                log.info("Successfully sent keep-alive signal for agent {}. Hub subscription count: {}.", agentId, response.getCount());
                // 처리할 구독이 있는 경우, 배치 단위로 나누어 처리합니다.
                long remainingCount = response.getCount();
                long batchSize = hubConfig.getKeepAlive().getBatchSize();

                while (remainingCount > 0) {
                    long fetchCount = Math.min(remainingCount, batchSize);
                    String getSubscriptionUrl = hubUrl + "/api/agents/" + agentId + "/subscriptions?count=" + fetchCount;

                    log.info("Fetching next batch of {} delegated requests from the hub...", fetchCount);
                    DelegatedRequestDTO[] subscriptions = restTemplate.getForObject(getSubscriptionUrl, DelegatedRequestDTO[].class);

                    if (subscriptions != null) {
                        for (DelegatedRequestDTO requestDTO : subscriptions) {
                            blockChainService.processIncomingRequest(requestDTO);
                        }
                        remainingCount -= subscriptions.length;
                        log.info("Processed {} requests, {} remaining.", subscriptions.length, remainingCount);
                    } else {
                        // 더 이상 가져올 구독이 없으면 루프를 중단합니다.
                        log.warn("Received null subscription array from hub. Stopping fetch loop.");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to send keep-alive signal to hub at {}: {}", keepAliveUrl, e.getMessage());
        }
    }
}
