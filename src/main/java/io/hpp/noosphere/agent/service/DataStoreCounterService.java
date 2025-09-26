package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.domain.ComputationCounter;
import io.hpp.noosphere.agent.domain.ContainerCounter;
import io.hpp.noosphere.agent.repository.ComputationCounterRepository;
import io.hpp.noosphere.agent.repository.ContainerCounterRepository;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationStatus;
import io.hpp.noosphere.agent.service.dto.enumeration.ContainerStatus;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataStoreCounterService {

    private static final Logger log = LoggerFactory.getLogger(DataStoreCounterService.class);
    private final ComputationCounterRepository computationCounterRepository;
    private final ContainerCounterRepository containerCounterRepository;

    public DataStoreCounterService(
        ComputationCounterRepository computationCounterRepository,
        ContainerCounterRepository containerCounterRepository
    ) {
        this.computationCounterRepository = computationCounterRepository;
        this.containerCounterRepository = containerCounterRepository;
    }

    @PostConstruct
    public void initializeCounters() {
        // 모든 상태와 위치 조합에 대해 카운터 초기화
        for (ComputationStatus status : ComputationStatus.values()) {
            for (ComputationLocation location : ComputationLocation.values()) {
                if (computationCounterRepository.findByLocationAndStatus(location, status).isEmpty()) {
                    ComputationCounter counter = ComputationCounter.builder().location(location).status(status).count(0L).build();
                    computationCounterRepository.save(counter);
                    log.debug("Initialized counter for location: {}, status: {}", location, status);
                }
            }
        }
    }

    /**
     * 계산 작업 카운터 조회 및 리셋
     */
    @Transactional
    public Map<ComputationStatus, Long> popComputationCounters() {
        List<ComputationCounter> counters = computationCounterRepository.findAll();

        Map<ComputationStatus, Long> result = counters
            .stream()
            .collect(Collectors.toMap(ComputationCounter::getStatus, ComputationCounter::getCount));

        // 모든 카운터 리셋
        computationCounterRepository.resetAllCounters();

        return result;
    }

    /**
     * 컨테이너 카운터 조회 및 리셋
     */
    @Transactional
    public Map<String, Map<ComputationStatus, Long>> popContainerCounters() {
        List<ContainerCounter> counters = containerCounterRepository.findAll();

        Map<String, Map<ComputationStatus, Long>> result = new HashMap<>();

        for (ContainerCounter counter : counters) {
            Map<ComputationStatus, Long> statusMap = result.computeIfAbsent(counter.getContainerId(), k -> new HashMap<>());
            statusMap.put(ComputationStatus.SUCCESS, counter.getSuccessCount());
            statusMap.put(ComputationStatus.FAILED, counter.getFailedCount());
        }

        // 모든 카운터 리셋
        containerCounterRepository.resetAllCounters();

        return result;
    }

    /**
     * 계산 작업 카운터 증가
     */
    @Transactional
    public void incrementComputationCounter(ComputationLocation location, ComputationStatus status) {
        int updated = computationCounterRepository.incrementCounter(location, status);

        if (updated == 0) {
            // 카운터가 존재하지 않으면 새로 생성
            ComputationCounter counter = ComputationCounter.builder().location(location).status(status).count(1L).build();
            computationCounterRepository.save(counter);
        }

        log.debug("Computation counter incremented - location: {} status: {}", location, status);
    }

    /**
     * 컨테이너 카운터 증가
     */
    @Transactional
    public void incrementContainerCounter(String containerId, ContainerStatus status) {
        int updated;
        if (status == ContainerStatus.SUCCESS) {
            updated = containerCounterRepository.incrementSuccess(containerId);
        } else {
            updated = containerCounterRepository.incrementFailure(containerId);
        }

        if (updated == 0) {
            // 카운터가 존재하지 않으면 새로 생성합니다.
            // 동시성 문제를 피하기 위해 findById로 다시 한번 확인합니다.
            containerCounterRepository
                .findById(containerId)
                .orElseGet(() -> {
                    ContainerCounter newCounter = new ContainerCounter(containerId, 0L, 0L);
                    if (status == ContainerStatus.SUCCESS) {
                        newCounter.setSuccessCount(1L);
                    } else {
                        newCounter.setFailedCount(1L);
                    }
                    return containerCounterRepository.save(newCounter);
                });
        }

        log.debug("Container counter incremented - container: {}, status: {}", containerId, status);
    }

    /**
     * 현재 카운터 상태 조회 (리셋하지 않음)
     */
    @Transactional(readOnly = true)
    public DataStoreCounters getCounters() {
        List<ComputationCounter> computationCounters = computationCounterRepository.findAll();
        List<ContainerCounter> containerCounters = containerCounterRepository.findAll();

        Map<ComputationStatus, Long> computationCounterMap = computationCounters
            .stream()
            .collect(Collectors.toMap(ComputationCounter::getStatus, ComputationCounter::getCount));

        Map<String, Map<ComputationStatus, Long>> containerCounterMap = new HashMap<>();
        for (ContainerCounter counter : containerCounters) {
            Map<ComputationStatus, Long> statusMap = containerCounterMap.computeIfAbsent(counter.getContainerId(), k -> new HashMap<>());
            statusMap.put(ComputationStatus.SUCCESS, counter.getSuccessCount());
            statusMap.put(ComputationStatus.FAILED, counter.getFailedCount());
        }

        return new DataStoreCounters(computationCounterMap, containerCounterMap);
    }

    @Data
    public static class DataStoreCounters {

        private final Map<ComputationStatus, Long> computationCounters;
        private final Map<String, Map<ComputationStatus, Long>> containerCounters;

        public DataStoreCounters(
            Map<ComputationStatus, Long> computationCounters,
            Map<String, Map<ComputationStatus, Long>> containerCounters
        ) {
            this.computationCounters = new HashMap<>(computationCounters);
            this.containerCounters = new HashMap<>(containerCounters);
        }
    }
}
