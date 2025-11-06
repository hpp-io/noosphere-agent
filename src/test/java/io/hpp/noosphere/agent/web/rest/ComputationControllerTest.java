package io.hpp.noosphere.agent.web.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hpp.noosphere.agent.service.blockchain.BlockChainService;
import io.hpp.noosphere.agent.service.blockchain.dto.SignatureParamsDTO;
import io.hpp.noosphere.agent.service.dto.DelegatedRequestDTO;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import io.hpp.noosphere.agent.service.dto.enumeration.RequestType;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ComputationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BlockChainService mockBlockChainService;

    @Test
    @DisplayName("POST /delegated - 유효한 위임 요청이 오면 200 OK와 빈 객체를 반환해야 한다")
    void createDelegatedComputation_withValidRequest_shouldReturn200Ok() throws Exception {
        // given: 유효한 요청 데이터 준비
        SubscriptionDTO subscription = SubscriptionDTO.builder().client("0x123").build();
        SignatureParamsDTO signature = new SignatureParamsDTO(1, System.currentTimeMillis() / 1000L, 27, BigInteger.ONE, BigInteger.TWO);
        DelegatedRequestDTO requestDTO = DelegatedRequestDTO.builder()
            .type(RequestType.DELEGATED_COMPUTATION)
            .subscription(subscription)
            .signature(signature)
            .build();

        // and: BlockChainService가 성공적으로 완료되는 CompletableFuture를 반환하도록 설정
        when(mockBlockChainService.processIncomingRequest(any(DelegatedRequestDTO.class))).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // when & then: /api/computations/delegated 엔드포인트로 POST 요청을 보내고 결과를 검증
        mockMvc
            .perform(
                post("/api/computations/delegated")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{}")); // 성공 시 빈 객체를 반환
    }

    @Test
    @DisplayName("POST /delegated - 서비스 처리 중 예외가 발생하면 500 Internal Server Error를 반환해야 한다")
    void createDelegatedComputation_whenServiceThrowsException_shouldReturn500() throws Exception {
        // given: 유효한 요청 데이터 준비
        SubscriptionDTO subscription = SubscriptionDTO.builder().client("0x456").build();
        SignatureParamsDTO signature = new SignatureParamsDTO(2, System.currentTimeMillis() / 1000L, 28, BigInteger.TEN, BigInteger.ONE);
        DelegatedRequestDTO requestDTO = DelegatedRequestDTO.builder().subscription(subscription).signature(signature).build();

        // and: BlockChainService가 예외를 던지도록 설정
        String errorMessage = "Blockchain connection failed";
        doThrow(new RuntimeException(errorMessage)).when(mockBlockChainService).processIncomingRequest(any(DelegatedRequestDTO.class));

        // when & then: /api/computations/delegated 엔드포인트로 POST 요청을 보내고 결과를 검증
        mockMvc
            .perform(
                post("/api/computations/delegated")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO))
            )
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Could not enqueue Computation: " + errorMessage));
    }
}
