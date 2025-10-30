package io.hpp.noosphere.agent.service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * Since on-chain container IDs are keccak hashes of comma-separated container IDs,
 * we need to build a lookup table to find out which containers are required for a given
 * subscription. This service is instantiated at the beginning of the noosphere-agent's
 * lifecycle and is used to decode container IDs from keccak hashes.
 */
@Service
public class ContainerLookupService {

    private static final Logger log = LoggerFactory.getLogger(ContainerLookupService.class);

    private final ContainerManagerService containerManagerService;
    private final Map<String, List<String>> containerLookup = new ConcurrentHashMap<>();

    public ContainerLookupService(ContainerManagerService containerManagerService) {
        this.containerManagerService = containerManagerService;
    }

    /**
     * Builds a lookup table from a keccak hash of a container set to the container set.
     * This method is executed once after the service is constructed.
     */
    @PostConstruct
    public void initContainerLookup() {
        List<String> containerIds = containerManagerService.getActiveContainers();
        if (containerIds.isEmpty()) {
            log.warn("No containers configured. Container lookup table will be empty.");
            return;
        }

        List<String> allPermutations = getAllCommaSeparatedPermutations(containerIds);

        for (String perm : allPermutations) {
            // To match Solidity's `abi.encode(string)`, we must use the FunctionEncoder.
            // This performs proper ABI encoding, including padding and length prefixing for dynamic types.
            // We create a dummy function with a single string parameter to get the ABI-encoded value.
            final org.web3j.abi.datatypes.Function dummyFunction = new org.web3j.abi.datatypes.Function(
                "dummy", // The function name does not affect the parameter encoding
                Arrays.asList(new Utf8String(perm)),
                Collections.emptyList()
            );
            String encodedFunctionCall = FunctionEncoder.encode(dummyFunction);

            // `abi.encode` only encodes parameters, so we must remove the 4-byte function selector.
            // The selector is the first 10 characters ("0x" + 8 hex chars).
            byte[] encoded = Numeric.hexStringToByteArray(encodedFunctionCall.substring(10));

            byte[] hashed = Hash.sha3(encoded);
            String hashHex = Numeric.toHexString(hashed);
            containerLookup.put(hashHex, List.of(perm.split(",")));
        }
        log.debug("Initialized container lookup with {} entries.", containerLookup.size());
    }

    /**
     * Get the container IDs from a keccak hash.
     *
     * @param hash Keccak hash of the comma-separated container IDs.
     * @return A list of container IDs, or an empty list if the hash is not found.
     */
    public List<String> getContainers(String hash) {
        return containerLookup.getOrDefault(hash, Collections.emptyList());
    }

    /**
     * Get the container IDs from a keccak hash byte array.
     * This is an overloaded method for convenience.
     *
     * @param hashBytes Keccak hash of the comma-separated container IDs as a byte array.
     * @return A list of container IDs, or an empty list if the hash is not found.
     */
    public List<String> getContainers(byte[] hashBytes) {
        String hashHex = Numeric.toHexString(hashBytes);
        return getContainers(hashHex);
    }

    /**
     * Generates all possible permutations of comma-separated container IDs from the
     * power set of the given list.
     *
     * @param containers List of container IDs.
     * @return A list of all possible permutations as comma-separated strings.
     */
    private List<String> getAllCommaSeparatedPermutations(List<String> containers) {
        List<String> allElements = new ArrayList<>();
        int n = containers.size();

        // Iterate through all possible subset sizes (from 1 to n)
        for (int r = 1; r <= n; r++) {
            // Generate permutations for each subset size
            permute(containers, new ArrayList<>(), new boolean[n], r, allElements);
        }
        return allElements;
    }

    /**
     * A recursive helper method to generate permutations.
     */
    private void permute(List<String> original, List<String> currentPermutation, boolean[] used, int r, List<String> allElements) {
        if (currentPermutation.size() == r) {
            allElements.add(String.join(",", currentPermutation));
            return;
        }

        for (int i = 0; i < original.size(); i++) {
            if (!used[i]) {
                used[i] = true;
                currentPermutation.add(original.get(i));
                permute(original, currentPermutation, used, r, allElements);
                currentPermutation.remove(currentPermutation.size() - 1);
                used[i] = false;
            }
        }
    }
}
