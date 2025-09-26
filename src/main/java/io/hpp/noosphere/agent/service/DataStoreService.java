package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.domain.Computation;
import io.hpp.noosphere.agent.repository.ComputationRepository;
import io.hpp.noosphere.agent.service.dto.*;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationStatus;
import io.hpp.noosphere.agent.service.dto.enumeration.ContainerStatus;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataStoreService {

    private static final Logger log = LoggerFactory.getLogger(DataStoreService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final ComputationRepository computationRepository;
    private final DataStoreCounterService counterService;

    public DataStoreService(ComputationRepository computationRepository, DataStoreCounterService counterService) {
        this.computationRepository = computationRepository;
        this.counterService = counterService;
    }

    /**
     * 계산 작업 조회
     */
    @Transactional(readOnly = true)
    public Optional<Computation> get(UUID computationId) {
        return computationRepository.findById(computationId);
    }

    /**
     * 클라이언트 IP별 계산 작업 조회
     */
    @Transactional(readOnly = true)
    public Optional<Computation> get(UUID computationId, String clientIp) {
        return computationRepository.findByIdAndClientIp(computationId, clientIp);
    }

    /**
     * 계산 작업 ID 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UUID> getComputationIds(String clientIp) {
        return computationRepository.findByClientIp(clientIp).stream().map(Computation::getId).collect(Collectors.toList());
    }

    /**
     * 클라이언트별 계산 작업 ID 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UUID> getComputationIds(String clientIp, Boolean pending) {
        if (pending == null) {
            return computationRepository.findByClientIp(clientIp).stream().map(Computation::getId).collect(Collectors.toList());
        } else if (pending) {
            return computationRepository
                .findByClientIpAndStatus(clientIp, ComputationStatus.RUNNING.name())
                .stream()
                .map(Computation::getId)
                .collect(Collectors.toList());
        } else {
            List<Computation> completed = new ArrayList<>();
            completed.addAll(computationRepository.findByClientIpAndStatus(clientIp, ComputationStatus.SUCCESS.name()));
            completed.addAll(computationRepository.findByClientIpAndStatus(clientIp, ComputationStatus.FAILED.name()));

            return completed.stream().map(Computation::getId).collect(Collectors.toList());
        }
    }

    /**
     * 다중 계산 작업 조회
     */
    @Transactional(readOnly = true)
    public List<Computation> get(List<BaseRequestDTO> requestList) {
        // N+1 쿼리 문제를 해결하기 위해 모든 ID를 한 번에 조회합니다.
        List<UUID> computationIds = requestList.stream().map(BaseRequestDTO::getId).collect(Collectors.toList());

        Map<UUID, Computation> computationMap = computationRepository
            .findAllById(computationIds)
            .stream()
            .collect(Collectors.toMap(Computation::getId, computation -> computation));

        return requestList
            .stream()
            .map(request -> {
                Computation computation = computationMap.get(request.getId());
                // 조회가 되었더라도 요청한 clientIp와 일치하는지 확인하여 보안을 강화합니다.
                if (computation != null && Objects.equals(computation.getClientIp(), request.getClientIp())) {
                    return computation;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * 계산 작업을 실행 중으로 설정 (Optimistic Locking 처리 개선)
     */
    public void setRunning(OffchainRequestDTO request) {
        if (request == null) {
            return;
        }

        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                Optional<Computation> existingComputation = computationRepository.findById(request.getId());

                Computation computation;
                if (existingComputation.isPresent()) {
                    // 기존 레코드 업데이트
                    computation = existingComputation.get();
                    computation.setStatus(ComputationStatus.RUNNING.name());
                    computation.setContainers(request.getContainers());
                    computation.setData(request.getData());
                    computation.setRequiresProof(request.getRequiresProof());
                    computation.setClientIp(request.getClientIp());
                } else {
                    // 새 레코드 생성
                    computation = Computation.builder()
                        .id(request.getId())
                        .status(ComputationStatus.RUNNING.name())
                        .containers(request.getContainers())
                        .data(request.getData())
                        .requiresProof(request.getRequiresProof())
                        .clientIp(request.getClientIp())
                        .build();
                }

                computationRepository.save(computation);
                counterService.incrementComputationCounter(ComputationLocation.OFF_CHAIN, ComputationStatus.RUNNING);

                log.info("Computation set to running: {}", request.getId());
                return; // 성공시 종료
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error(
                        "Failed to set computation as running after {} attempts due to optimistic locking: {}",
                        MAX_RETRY_ATTEMPTS,
                        request.getId(),
                        e
                    );
                    throw e;
                }

                log.warn("Optimistic locking failure on attempt {} for computation {}, retrying...", attempts, request.getId());

                // 재시도 전 잠시 대기
                try {
                    Thread.sleep(50 * attempts); // 점진적 백오프
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry wait", ie);
                }
            } catch (Exception e) {
                log.error("Failed to set computation as running: {}", request.getId(), e);
                return; // 다른 예외의 경우 재시도하지 않음
            }
        }
    }

    /**
     * 계산 작업을 성공으로 설정 (Optimistic Locking 처리 개선)
     */
    public void setSuccess(OffchainRequestDTO request, List<ContainerResultDTO> results) {
        if (request == null) {
            return;
        }

        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                Optional<Computation> computationOpt = computationRepository.findById(request.getId());

                Computation computation = computationOpt.orElse(
                    Computation.builder()
                        .id(request.getId())
                        .containers(request.getContainers())
                        .data(request.getData())
                        .requiresProof(request.getRequiresProof())
                        .clientIp(request.getClientIp())
                        .build()
                );

                computation.setStatus(ComputationStatus.SUCCESS.name());
                computation.setResults(serializeResults(results));
                computation.setCompletedAt(LocalDateTime.now());

                computationRepository.save(computation);
                counterService.incrementComputationCounter(ComputationLocation.OFF_CHAIN, ComputationStatus.SUCCESS);

                log.info("Computation set to success: {}", request.getId());
                return;
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error(
                        "Failed to set computation as success after {} attempts due to optimistic locking: {}",
                        MAX_RETRY_ATTEMPTS,
                        request.getId(),
                        e
                    );
                    return;
                }

                log.warn("Optimistic locking failure on attempt {} for computation success {}, retrying...", attempts, request.getId());

                try {
                    Thread.sleep(50 * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to set computation as success: {}", request.getId(), e);
                return;
            }
        }
    }

    /**
     * 계산 작업을 실패로 설정 (Optimistic Locking 처리 개선)
     */
    public void setFailed(OffchainRequestDTO request, List<ContainerResultDTO> results) {
        if (request == null) {
            return;
        }

        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                Optional<Computation> computationOpt = computationRepository.findById(request.getId());

                Computation computation = computationOpt.orElse(
                    Computation.builder()
                        .id(request.getId())
                        .containers(request.getContainers())
                        .data(request.getData())
                        .requiresProof(request.getRequiresProof())
                        .clientIp(request.getClientIp())
                        .build()
                );

                computation.setStatus(ComputationStatus.FAILED.name());
                computation.setResults(serializeResults(results));
                computation.setCompletedAt(LocalDateTime.now());

                computationRepository.save(computation);
                counterService.incrementComputationCounter(ComputationLocation.OFF_CHAIN, ComputationStatus.FAILED);

                log.warn("Computation set to failed: {}", request.getId());
                return;
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error(
                        "Failed to set computation as failed after {} attempts due to optimistic locking: {}",
                        MAX_RETRY_ATTEMPTS,
                        request.getId(),
                        e
                    );
                    return;
                }

                log.warn("Optimistic locking failure on attempt {} for computation failure {}, retrying...", attempts, request.getId());

                try {
                    Thread.sleep(50 * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to set computation as failed: {}", request.getId(), e);
                return;
            }
        }
    }

    /**
     * 컨테이너 상태 추적
     */
    public void trackContainerStatus(String containerId, ContainerStatus status) {
        try {
            counterService.incrementContainerCounter(containerId, status);
            log.debug("Container status tracked - container: {}, status: {}", containerId, status);
        } catch (Exception e) {
            log.error("Failed to track container status - container: {}, status: {}", containerId, status, e);
        }
    }

    /**
     * 만료된 실행 중인 계산 작업들을 정리
     */
    @Transactional
    public void cleanupExpiredComputations(int timeoutHours) {
        LocalDateTime expiredTime = LocalDateTime.now().minusHours(timeoutHours);

        List<Computation> expiredComputations = computationRepository.findExpiredRunningComputations(expiredTime);

        for (Computation computation : expiredComputations) {
            computation.setStatus(ComputationStatus.FAILED.name());
            computation.setCompletedAt(LocalDateTime.now());

            // 결과에 타임아웃 정보 추가
            List<Map<String, Object>> timeoutResults = new ArrayList<>();
            Map<String, Object> timeoutResult = new HashMap<>();
            timeoutResult.put("error", "Computation timed out");
            timeoutResult.put("timeout_hours", timeoutHours);
            timeoutResults.add(timeoutResult);
            computation.setResults(timeoutResults);
        }

        computationRepository.saveAll(expiredComputations);
        log.info("Cleaned up {} expired computations", expiredComputations.size());
    }

    /**
     * 완료된 계산 작업들을 정리 (TTL 시뮬레이션)
     */
    @Transactional
    public void cleanupCompletedComputations(int retentionDays) {
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(retentionDays);
        computationRepository.deleteByCompletedAtBefore(expiredTime);
        log.info("Cleaned up completed computations older than {} days", retentionDays);
    }

    /**
     * 결과를 직렬화 가능한 형태로 변환
     */
    private List<Map<String, Object>> serializeResults(List<ContainerResultDTO> results) {
        return results
            .stream()
            .map(result -> {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("container", result.getContainer());

                // Java 16+ Pattern Matching for instanceof
                if (result instanceof ContainerOutputDTO outputDTO) {
                    resultMap.put("success", true);
                    resultMap.put("output", outputDTO.getOutput());
                } else if (result instanceof ContainerErrorDTO errorDTO) {
                    resultMap.put("success", false);
                    resultMap.put("error", errorDTO.getError());
                }

                return resultMap;
            })
            .collect(Collectors.toList());
    }
}
