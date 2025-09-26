package io.hpp.noosphere.agent.scheduler;

import io.hpp.noosphere.agent.service.DataStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataCleanupScheduler.class);

    private final DataStoreService dataStoreService;

    @Value("${computation.timeout.hours:24}")
    private int computationTimeoutHours;

    @Value("${computation.retention.days:30}")
    private int computationRetentionDays;

    public DataCleanupScheduler(DataStoreService dataStoreService) {
        this.dataStoreService = dataStoreService;
    }

    /**
     * 매시간 만료된 계산 작업 정리
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다
    public void cleanupExpiredComputations() {
        log.debug("Starting cleanup of expired computations");
        dataStoreService.cleanupExpiredComputations(computationTimeoutHours);
    }

    /**
     * 매일 자정에 오래된 완료 계산 작업 정리
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void cleanupCompletedComputations() {
        log.debug("Starting cleanup of completed computations");
        dataStoreService.cleanupCompletedComputations(computationRetentionDays);
    }
}
