package io.hpp.noosphere.agent.service.blockchain.web3;

import static io.hpp.noosphere.agent.config.Constants.ZERO_ADDRESS;

import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.Delegator;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;

@Service
public class Web3DelegatorService {

    private static final Logger log = LoggerFactory.getLogger(Web3DelegatorService.class);

    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jConfig.CustomGasProvider gasProvider;

    public Web3DelegatorService(Web3j web3j, Credentials credentials, Web3jConfig.CustomGasProvider gasProvider) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    /**
     * Fetches the delegated signer from a subscription consumer contract.
     */
    public CompletableFuture<String> getDelegatedSigner(SubscriptionDTO subscription, Long blockNumber) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                    Delegator.FUNC_GETSIGNER,
                    java.util.Collections.emptyList(),
                    java.util.Collections.singletonList(new TypeReference<Address>() {})
                );
                String encodedFunction = FunctionEncoder.encode(function);

                DefaultBlockParameter blockParameter;
                if (blockNumber != null && blockNumber > 0) {
                    log.debug("Querying delegated signer for client {} at block {}", subscription.getClient(), blockNumber);
                    blockParameter = DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber));
                } else {
                    log.debug("Querying delegated signer for client {} at latest block...", subscription.getClient());
                    blockParameter = DefaultBlockParameter.valueOf("latest");
                }

                org.web3j.protocol.core.methods.request.Transaction transaction =
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        credentials.getAddress(),
                        subscription.getClient(), // The delegator contract address is the client/owner
                        encodedFunction
                    );

                String result = web3j.ethCall(transaction, blockParameter).send().getValue();
                String signerAddress = (String) FunctionReturnDecoder.decode(result, function.getOutputParameters()).get(0).getValue();

                log.debug("Fetched delegated signer: {}", signerAddress);
                return signerAddress;
            } catch (Exception e) {
                log.warn(
                    "Failed to get delegated signer for client {}. Returning zero address. Error: {}",
                    subscription.getClient(),
                    e.getMessage()
                );
                return ZERO_ADDRESS;
            }
        });
    }
}
