package io.hpp.noosphere.agent.service.blockchain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.contracts.SubscriptionBatchReader;
import io.hpp.noosphere.agent.service.ContainerLookupService;
import io.hpp.noosphere.agent.service.NoosphereConfigService;
import io.hpp.noosphere.agent.service.RequestValidatorService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3RouterService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3SubscriptionBatchReaderService;
import io.hpp.noosphere.agent.service.dto.OnchainRequestDTO;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

@ExtendWith(MockitoExtension.class)
class BlockchainListenerTest {

    // @Mock 대신 직접 생성하여 DEEP_STUBS를 사용합니다.
    private Web3j mockWeb3j;

    @Mock
    private Web3RouterService mockWeb3Router;

    @Mock
    private Web3SubscriptionBatchReaderService mockWeb3BatchReader;

    @Mock
    private RequestValidatorService mockRequestValidatorService;

    @Mock
    private BlockChainService mockBlockChainService;

    @Mock
    private NoosphereConfigService mockNoosphereConfigService;

    @Mock
    private ContainerLookupService mockContainerLookupService;

    private BlockchainListener blockchainListener;

    @BeforeEach
    void setUp() throws IOException {
        // Mock ApplicationProperties
        ApplicationProperties.Chain chainProps = new ApplicationProperties.Chain();
        chainProps.setTrailHeadBlocks(1L);
        ApplicationProperties.SnapshotSync snapshotSyncProps = new ApplicationProperties.SnapshotSync();
        snapshotSyncProps.setStartingSubId(5);
        snapshotSyncProps.setBatchSize(100);
        snapshotSyncProps.setSleep(100L);
        chainProps.setSnapshotSync(snapshotSyncProps);

        ApplicationProperties.NoosphereConfig noosphereProps = new ApplicationProperties.NoosphereConfig();
        noosphereProps.setChain(chainProps);

        when(mockNoosphereConfigService.getActiveConfig()).thenReturn(noosphereProps);

        // RETURNS_DEEP_STUBS를 사용하여 mockWeb3j.ethBlockNumber()가 null을 반환하지 않도록 합니다.
        mockWeb3j = mock(Web3j.class, RETURNS_DEEP_STUBS);

        // Mock Web3j ethBlockNumber for the initial setup
        EthBlockNumber ethBlockNumber = new EthBlockNumber();
        ethBlockNumber.setResult("0x" + Long.toHexString(1000L));
        // Use lenient() to allow this stub to be overridden in tests without causing an error.
        // This sets up a default behavior for ethBlockNumber().send().
        lenient().when(mockWeb3j.ethBlockNumber().send()).thenReturn(ethBlockNumber);

        // Initialize the service under test
        blockchainListener = new BlockchainListener(
            mockWeb3j,
            mockWeb3Router,
            mockWeb3BatchReader,
            mockRequestValidatorService,
            mockBlockChainService,
            mockNoosphereConfigService,
            mockContainerLookupService
        );

        // Set initial state to avoid running onApplicationEvent
        // We can test onApplicationEvent in a separate test
        blockchainListener.onApplicationEvent(null); // Simulate startup to set initial block number
    }

    @Test
    @DisplayName("새로운 블록과 구독이 감지되었을 때, 구독 정보를 올바르게 처리해야 한다")
    void subscriptionSyncLoop_whenNewSubscriptionsExist_shouldProcessThem() throws IOException {
        // given: Setup mocks for a sync scenario
        long currentHeadBlock = 1100L;
        long newHeadSubId = 5L;

        // 1. Mock a new block number
        // This will override the mock from setUp() for subsequent calls
        EthBlockNumber newEthBlockNumber = new EthBlockNumber();
        newEthBlockNumber.setResult("0x" + Long.toHexString(currentHeadBlock));
        // We assume the same request object is used, but its send() method returns a new value
        when(mockWeb3j.ethBlockNumber().send()).thenReturn(newEthBlockNumber);

        // 2. Mock the highest subscription ID
        when(mockWeb3Router.getLastSubscriptionId(5L)).thenReturn(CompletableFuture.completedFuture(BigInteger.valueOf(newHeadSubId)));

        // 3. Mock the batch reader to return one subscription
        SubscriptionBatchReader.ComputeSubscription contractSub = new SubscriptionBatchReader.ComputeSubscription(
            new byte[32], // routeId
            new byte[32], // containerId
            BigInteger.valueOf(1000), // feeAmount
            "0xClientAddress", // client
            BigInteger.valueOf(1622548800), // activeAt
            BigInteger.valueOf(3600), // intervalSeconds
            BigInteger.valueOf(100), // maxExecutions
            "0xWalletAddress", // wallet
            "0xFeeTokenAddress", // feeToken
            "0xVerifierAddress", // verifier
            BigInteger.valueOf(3), // redundancy
            true // useDeliveryInbox
        );
        when(mockWeb3BatchReader.getSubscriptions(anyInt(), anyInt(), anyLong())).thenReturn(
            CompletableFuture.completedFuture(List.of(contractSub))
        );

        // 4. Mock other services
        when(mockContainerLookupService.getContainers(any(byte[].class))).thenReturn(List.of("test-container-id"));
        when(mockRequestValidatorService.validateOnChainRequest(any(OnchainRequestDTO.class))).thenReturn(true);
        // Mock getIntervalStatuses to avoid NullPointerException
        when(mockWeb3BatchReader.getIntervalStatuses(anyList(), anyList(), anyLong())).thenReturn(
            CompletableFuture.completedFuture(Collections.emptyList())
        );

        // when: Execute the sync loop
        blockchainListener.subscriptionSyncLoop();

        // then: Verify that the blockchain service processed the request
        ArgumentCaptor<OnchainRequestDTO> captor = ArgumentCaptor.forClass(OnchainRequestDTO.class);
        verify(mockBlockChainService, times(1)).processIncomingRequest(captor.capture());

        // Assert the content of the captured DTO
        SubscriptionDTO processedSub = captor.getValue().getSubscription();
        assertThat(processedSub.getId()).isEqualTo(0L); // Assuming sync starts from subId 0
        assertThat(processedSub.getClient()).isEqualTo("0xClientAddress");
        assertThat(processedSub.getContainerId()).isEqualTo("test-container-id");
        assertThat(processedSub.getIntervalSeconds()).isEqualTo(3600L);
    }
}
