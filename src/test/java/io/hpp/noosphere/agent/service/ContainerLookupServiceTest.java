package io.hpp.noosphere.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

@ExtendWith(MockitoExtension.class)
class ContainerLookupServiceTest {

    @Mock
    private ContainerManagerService mockContainerManagerService;

    private ContainerLookupService containerLookupService;

    @BeforeEach
    void setUp() {
        // ContainerLookupService를 수동으로 생성하여 @PostConstruct 메서드를 제어합니다.
        containerLookupService = new ContainerLookupService(mockContainerManagerService);
    }

    @Test
    @DisplayName("단일 컨테이너 ID가 주어졌을 때, 해시로 올바르게 조회되어야 한다")
    void getContainers_withSingleContainer_shouldReturnCorrectId() {
        // given
        List<String> activeContainers = List.of("container-a");
        when(mockContainerManagerService.getActiveContainers()).thenReturn(activeContainers);

        // when
        containerLookupService.initContainerLookup(); // @PostConstruct 메서드 수동 실행

        // then
        String expectedHash = calculateKeccak256AbiEncode("container-a");
        List<String> foundContainers = containerLookupService.getContainers(expectedHash);

        assertThat(foundContainers).isNotNull();
        assertThat(foundContainers).hasSize(1);
        assertThat(foundContainers.get(0)).isEqualTo("container-a");
    }

    @Test
    @DisplayName("여러 컨테이너 ID 조합이 주어졌을 때, 해시로 올바르게 조회되어야 한다")
    void getContainers_withMultipleContainers_shouldReturnCorrectIds() {
        // given
        List<String> activeContainers = List.of("id-1", "id-2");
        when(mockContainerManagerService.getActiveContainers()).thenReturn(activeContainers);

        // when
        containerLookupService.initContainerLookup();

        // then: "id-1,id-2" 순서의 조합을 테스트
        String permutation = "id-1,id-2";
        String expectedHash = calculateKeccak256AbiEncode(permutation);
        List<String> foundContainers = containerLookupService.getContainers(expectedHash);

        assertThat(foundContainers).isNotNull();
        assertThat(foundContainers).containsExactly("id-1", "id-2");

        // then: "id-2,id-1" 순서의 조합도 테스트
        String reversePermutation = "id-2,id-1";
        String reverseExpectedHash = calculateKeccak256AbiEncode(reversePermutation);
        List<String> reverseFoundContainers = containerLookupService.getContainers(reverseExpectedHash);

        assertThat(reverseFoundContainers).isNotNull();
        assertThat(reverseFoundContainers).containsExactly("id-2", "id-1");
    }

    @Test
    @DisplayName("존재하지 않는 해시로 조회하면, 빈 리스트를 반환해야 한다")
    void getContainers_withUnknownHash_shouldReturnEmptyList() {
        // given
        when(mockContainerManagerService.getActiveContainers()).thenReturn(Collections.emptyList());

        // when
        containerLookupService.initContainerLookup();
        List<String> foundContainers = containerLookupService.getContainers(
            "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        );

        // then
        assertThat(foundContainers).isNotNull();
        assertThat(foundContainers).isEmpty();
    }

    /**
     * 테스트 검증을 위해 ContainerLookupService의 해시 생성 로직을 동일하게 구현한 헬퍼 메서드입니다.
     */
    private String calculateKeccak256AbiEncode(String input) {
        final org.web3j.abi.datatypes.Function dummyFunction = new org.web3j.abi.datatypes.Function(
            "dummy",
            Arrays.asList(new Utf8String(input)),
            Collections.emptyList()
        );
        String encodedFunctionCall = FunctionEncoder.encode(dummyFunction);
        byte[] encodedParams = Numeric.hexStringToByteArray(encodedFunctionCall.substring(10));
        byte[] hashed = Hash.sha3(encodedParams);
        return Numeric.toHexString(hashed);
    }
}
