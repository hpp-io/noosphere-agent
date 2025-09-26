package io.hpp.noosphere.agent.web.rest;

import io.hpp.noosphere.agent.domain.Computation;
import io.hpp.noosphere.agent.service.ComputationService;
import io.hpp.noosphere.agent.service.DataStoreService;
import io.hpp.noosphere.agent.service.RequestValidatorService;
import io.hpp.noosphere.agent.service.dto.BaseRequestDTO;
import io.hpp.noosphere.agent.service.dto.DelegatedRequestDTO;
import io.hpp.noosphere.agent.service.dto.OffchainRequestDTO;
import io.hpp.noosphere.agent.service.dto.enumeration.RequestType;
import io.hpp.noosphere.agent.web.rest.vm.RequestUnionVM;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ComputationController {

    private static final Logger log = LoggerFactory.getLogger(ComputationController.class);

    private final RequestValidatorService requestValidatorService;
    private final ComputationService computationService;
    private final DataStoreService dataStoreService;

    public ComputationController(
        RequestValidatorService requestValidatorService,
        ComputationService computationService,
        DataStoreService dataStoreService
    ) {
        this.requestValidatorService = requestValidatorService;
        this.computationService = computationService;
        this.dataStoreService = dataStoreService;
    }

    @PostMapping("/computations")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createComputations(
        @RequestBody RequestUnionVM requestData,
        HttpServletRequest request
    ) {
        return handleComputationRequest(requestData, request, false);
    }

    @GetMapping("/computations")
    public ResponseEntity<Object> getComputations(
        @RequestParam(value = "id", required = false) List<UUID> computationIds,
        @RequestParam(value = "pending", required = false) String pending,
        @RequestParam(value = "intermediate", required = false, defaultValue = "false") boolean intermediate,
        HttpServletRequest request
    ) {
        String clientIp = getClientIp(request);
        if (clientIp == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not get client IP address"));
        }

        try {
            if (computationIds == null || computationIds.isEmpty()) {
                // Return all Computation IDs based on pending status
                List<UUID> ids;
                if ("true".equals(pending)) {
                    ids = dataStoreService.getComputationIds(clientIp, true);
                } else if ("false".equals(pending)) {
                    ids = dataStoreService.getComputationIds(clientIp, false);
                } else {
                    ids = dataStoreService.getComputationIds(clientIp);
                }
                return ResponseEntity.ok(ids);
            } else {
                List<Computation> result = new ArrayList<>();
                // Return specific Computation results
                for (UUID id : computationIds) {
                    Optional<Computation> data = dataStoreService.get(id, clientIp);
                    if (data.isPresent()) {
                        result.add(data.get());
                    }
                }
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            log.error("Error getting Computations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not retrieve Computations"));
        }
    }

    // Helper methods
    private CompletableFuture<ResponseEntity<Map<String, Object>>> handleComputationRequest(
        RequestUnionVM requestData,
        HttpServletRequest request,
        boolean isStream
    ) {
        String clientIp = getClientIp(request);
        if (clientIp == null) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(Map.of("error", "Could not get client IP address")));
        }

        try {
            Map<String, Object> returnObj = new HashMap<>();
            if (requestData.isOffchainRequest()) {
                OffchainRequestDTO requestDTO = requestData.getOffchainRequest();
                requestDTO.setId(UUID.randomUUID());
                requestDTO.setClientIp(clientIp);
                requestDTO.setType(RequestType.OFF_CHAIN_COMPUTATION);
                requestValidatorService.validateOffChainRequest(requestDTO);
                return computationService
                    .processOffchainComputation(requestDTO)
                    .thenApply(v -> {
                        returnObj.put("id", requestDTO.getId());
                        log.debug(
                            "Processed REST response - endpoint: {}, " + "method: {}, status: 200, type: {}, id: {}",
                            request.getRequestURI(),
                            request.getMethod(),
                            requestDTO.getType(),
                            requestDTO.getId()
                        );
                        return ResponseEntity.ok(returnObj);
                    });
            } else if (requestData.isDelegatedRequest()) {
                DelegatedRequestDTO requestDTO = requestData.getDelegatedRequest();
                // TODO: delegated call section
                //                return chainManagerService.procassDelegated(requestDTO)
                //                    .thenApply(v -> {
                //                        log.debug("Processed REST response - endpoint: {}, " +
                //                                "method: {}, status: 200, type: {}, id: {}",
                //                            request.getRequestURI(), request.getMethod(),
                //                            requestDTO.getType(), requestDTO.getId());
                //                        return ResponseEntity.ok(returnObj);
                //                    });
            } else {
                throw new IllegalArgumentException("Unknown message type: " + requestData.getRequestType());
            }

            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unknown message type"))
            );
        } catch (Exception e) {
            log.error("Error processing Computation request", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not enqueue Computation: " + e.getMessage())
                )
            );
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    private boolean isLocalIp(String ip) {
        return (
            ip != null &&
            (ip.equals("127.0.0.1") ||
                ip.equals("0:0:0:0:0:0:0:1") ||
                ip.equals("localhost") ||
                ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172."))
        );
    }
}
