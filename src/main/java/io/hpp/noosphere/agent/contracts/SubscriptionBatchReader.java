package io.hpp.noosphere.agent.contracts;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.10.3.
 */
@SuppressWarnings("rawtypes")
public class SubscriptionBatchReader extends Contract {

    public static final String BINARY =
        "60c03461008557601f610b0638819003918201601f19168301916001600160401b0383118484101761008957808492604094855283398101031261008557610052602061004b8361009d565b920161009d565b6001600160a01b039182166080521660a052604051610a5490816100b2823960805181610120015260a051816105f80152f35b5f80fd5b634e487b7160e01b5f52604160045260245ffd5b51906001600160a01b03821682036100855756fe60406080815260049081361015610014575f80fd5b60e0915f35831c806331f68014146105315763a566fc7c14610034575f80fd5b346103ce57817ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126103ce57803567ffffffffffffffff93848216908183036103ce5760249560243596818816938489036103ce5784106104885760018261009f87839b6109a5565b160182811161045c57821697929692959293926100d36100be8a61092d565b996100cb87519b8c6108ec565b808b5261092d565b937fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0602095015f5b8181106103f057505087908773ffffffffffffffffffffffffffffffffffffffff98897f000000000000000000000000000000000000000000000000000000000000000016935b610218575b50505050505050815194818601928287528751809452828188019801945f925b85841061017457888a0389f35b865180518b5285810151868c015283810151848c01526060808201518416908c015260808082015163ffffffff908116918d019190915260a0808301518216908d015260c080830151909116908c0152818101518316828c0152610100808201518416908c0152610120808201518416908c01526101408082015161ffff16908c0152610160908101511515908b0152610180909901989584019592870192610167565b86819d9b9d9c98999c168381116103e3578761023484846109a5565b16908d51907fa50366b8000000000000000000000000000000000000000000000000000000008252878201526101809081818a818a5afa9182156103d9578a949392918f915f936102dd575b505090610291836102989493610991565b528d610991565b50168681146102b257999b969a96999697968a018a610142565b856011867f4e487b71000000000000000000000000000000000000000000000000000000005f52525ffd5b92509294505081813d83116103d2575b6102f781836108ec565b810103126103ce578d51928e61030c856108cf565b825185528c8301518d8601528083015190850152606061032d8184016109ec565b90850152608061033e818401610a0d565b9085015260a061034f818401610a0d565b9085015260c0610360818401610a0d565b9085015261036f8b83016109ec565b8b8501526101006103818184016109ec565b908501526101206103938184016109ec565b908501526101406103a5818401610982565b90850152610160809201519384151585036103ce57918201939093528892908d61029183610280565b5f80fd5b503d6102ed565b8f513d5f823e3d90fd5b509b999b9a97969a610147565b95879a9681989c9a9c51610403816108cf565b5f81525f838201525f8d8201525f60608201525f60808201525f60a08201525f60c08201525f898201525f6101008201525f6101208201525f6101408201525f61016082015282828d010152019a989a999695996100fb565b6011877f4e487b71000000000000000000000000000000000000000000000000000000005f525260245ffd5b60a48660208951917f08c379a0000000000000000000000000000000000000000000000000000000008352820152604760248201527f537562736372697074696f6e42617463685265616465723a20656e644964206d60448201527f7573742062652067726561746572207468616e206f7220657175616c20746f2060648201527f73746172744964000000000000000000000000000000000000000000000000006084820152fd5b50346103ce57817ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126103ce5767ffffffffffffffff9181358381116103ce576105809036908401610855565b90936024946024358281116103ce579186936105a0879436908601610855565b9490956105ac8361092d565b956105b9895197886108ec565b8387526105c58461092d565b977fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0602099015f5b8181106108225750507f000000000000000000000000000000000000000000000000000000000000000073ffffffffffffffffffffffffffffffffffffffff16945f5b81811061068c578b8b8b8251928284938401908085528351809252808386019401925f905b8382106106625786860387f35b8451805161ffff168752830151151586840152879650948501949382019360019190910190610655565b61069b81838b9e9c9d9e610945565b3588811681036103ce576106b0828686610945565b359063ffffffff821682036103ce578b519160a01b9060c01b1781528c8b600c8320918084018092527fbc85694f00000000000000000000000000000000000000000000000000000000825282898501528189818d5afa928315610818578f905f946107df575b5050508b51907f368bf464000000000000000000000000000000000000000000000000000000008252888201528d8188818c5afa9081156107d5578d9392918f918e905f9261079e575b509461078892859261ffff600198519661077a88610886565b168652151590850152610991565b52610793818d610991565b50019a99989a610630565b9550505083813d83116107ce575b6107b681836108ec565b810103126103ce5791518c92908e908d610788610761565b503d6107ac565b8c513d5f823e3d90fd5b9080929394503d8311610811575b6107f781856108ec565b810103126103ce5761080890610982565b908e8e81610717565b503d6107ed565b8d513d5f823e3d90fd5b99808b8b829e9c9d83908e9d9b9d519261083b84610886565b5f84525f838501520101520190509a99989a9795976105ed565b9181601f840112156103ce5782359167ffffffffffffffff83116103ce576020808501948460051b0101116103ce57565b6040810190811067ffffffffffffffff8211176108a257604052565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b610180810190811067ffffffffffffffff8211176108a257604052565b90601f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0910116810190811067ffffffffffffffff8211176108a257604052565b67ffffffffffffffff81116108a25760051b60200190565b91908110156109555760051b0190565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52603260045260245ffd5b519061ffff821682036103ce57565b80518210156109555760209160051b010190565b67ffffffffffffffff91821690821603919082116109bf57565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b519073ffffffffffffffffffffffffffffffffffffffff821682036103ce57565b519063ffffffff821682036103ce5756fea2646970667358221220d005d63ade94b707872248e97d7350758a748e760287cebd8cef8676323d6feb64736f6c63430008170033";

    public static final String FUNC_GETINTERVALSTATUSES = "getIntervalStatuses";

    public static final String FUNC_GETSUBSCRIPTIONS = "getSubscriptions";

    @Deprecated
    protected SubscriptionBatchReader(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SubscriptionBatchReader(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SubscriptionBatchReader(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SubscriptionBatchReader(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<List> getIntervalStatuses(List<BigInteger> ids, List<BigInteger> intervals) {
        final Function function = new Function(
            FUNC_GETINTERVALSTATUSES,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint64>(
                    org.web3j.abi.datatypes.generated.Uint64.class,
                    org.web3j.abi.Utils.typeMap(ids, org.web3j.abi.datatypes.generated.Uint64.class)
                ),
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint32>(
                    org.web3j.abi.datatypes.generated.Uint32.class,
                    org.web3j.abi.Utils.typeMap(intervals, org.web3j.abi.datatypes.generated.Uint32.class)
                )
            ),
            Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<IntervalStatus>>() {})
        );
        return new RemoteFunctionCall<List>(
            function,
            new Callable<List>() {
                @Override
                @SuppressWarnings("unchecked")
                public List call() throws Exception {
                    List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                    return convertToNative(result);
                }
            }
        );
    }

    public RemoteFunctionCall<List> getSubscriptions(BigInteger startId, BigInteger endId) {
        final Function function = new Function(
            FUNC_GETSUBSCRIPTIONS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint64(startId), new org.web3j.abi.datatypes.generated.Uint64(endId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<ComputeSubscription>>() {})
        );
        return new RemoteFunctionCall<List>(
            function,
            new Callable<List>() {
                @Override
                @SuppressWarnings("unchecked")
                public List call() throws Exception {
                    List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                    return convertToNative(result);
                }
            }
        );
    }

    @Deprecated
    public static SubscriptionBatchReader load(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return new SubscriptionBatchReader(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SubscriptionBatchReader load(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return new SubscriptionBatchReader(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SubscriptionBatchReader load(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        ContractGasProvider contractGasProvider
    ) {
        return new SubscriptionBatchReader(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SubscriptionBatchReader load(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider
    ) {
        return new SubscriptionBatchReader(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<SubscriptionBatchReader> deploy(
        Web3j web3j,
        Credentials credentials,
        ContractGasProvider contractGasProvider,
        String _router,
        String _coordinator
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _router), new org.web3j.abi.datatypes.Address(160, _coordinator))
        );
        return deployRemoteCall(SubscriptionBatchReader.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<SubscriptionBatchReader> deploy(
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider,
        String _router,
        String _coordinator
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _router), new org.web3j.abi.datatypes.Address(160, _coordinator))
        );
        return deployRemoteCall(SubscriptionBatchReader.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<SubscriptionBatchReader> deploy(
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit,
        String _router,
        String _coordinator
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _router), new org.web3j.abi.datatypes.Address(160, _coordinator))
        );
        return deployRemoteCall(SubscriptionBatchReader.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<SubscriptionBatchReader> deploy(
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit,
        String _router,
        String _coordinator
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _router), new org.web3j.abi.datatypes.Address(160, _coordinator))
        );
        return deployRemoteCall(SubscriptionBatchReader.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class IntervalStatus extends StaticStruct {

        public BigInteger redundancyCount;

        public Boolean commitmentExists;

        public IntervalStatus(BigInteger redundancyCount, Boolean commitmentExists) {
            super(new org.web3j.abi.datatypes.generated.Uint16(redundancyCount), new org.web3j.abi.datatypes.Bool(commitmentExists));
            this.redundancyCount = redundancyCount;
            this.commitmentExists = commitmentExists;
        }

        public IntervalStatus(Uint16 redundancyCount, Bool commitmentExists) {
            super(redundancyCount, commitmentExists);
            this.redundancyCount = redundancyCount.getValue();
            this.commitmentExists = commitmentExists.getValue();
        }
    }

    public static class ComputeSubscription extends StaticStruct {

        public byte[] routeId;

        public byte[] containerId;

        public BigInteger feeAmount;

        public String client;

        public BigInteger activeAt;

        public BigInteger intervalSeconds;

        public BigInteger maxExecutions;

        public String wallet;

        public String feeToken;

        public String verifier;

        public BigInteger redundancy;

        public Boolean useDeliveryInbox;

        public ComputeSubscription(
            byte[] routeId,
            byte[] containerId,
            BigInteger feeAmount,
            String client,
            BigInteger activeAt,
            BigInteger intervalSeconds,
            BigInteger maxExecutions,
            String wallet,
            String feeToken,
            String verifier,
            BigInteger redundancy,
            Boolean useDeliveryInbox
        ) {
            super(
                new org.web3j.abi.datatypes.generated.Bytes32(routeId),
                new org.web3j.abi.datatypes.generated.Bytes32(containerId),
                new org.web3j.abi.datatypes.generated.Uint256(feeAmount),
                new org.web3j.abi.datatypes.Address(160, client),
                new org.web3j.abi.datatypes.generated.Uint32(activeAt),
                new org.web3j.abi.datatypes.generated.Uint32(intervalSeconds),
                new org.web3j.abi.datatypes.generated.Uint32(maxExecutions),
                new org.web3j.abi.datatypes.Address(160, wallet),
                new org.web3j.abi.datatypes.Address(160, feeToken),
                new org.web3j.abi.datatypes.Address(160, verifier),
                new org.web3j.abi.datatypes.generated.Uint16(redundancy),
                new org.web3j.abi.datatypes.Bool(useDeliveryInbox)
            );
            this.routeId = routeId;
            this.containerId = containerId;
            this.feeAmount = feeAmount;
            this.client = client;
            this.activeAt = activeAt;
            this.intervalSeconds = intervalSeconds;
            this.maxExecutions = maxExecutions;
            this.wallet = wallet;
            this.feeToken = feeToken;
            this.verifier = verifier;
            this.redundancy = redundancy;
            this.useDeliveryInbox = useDeliveryInbox;
        }

        public ComputeSubscription(
            Bytes32 routeId,
            Bytes32 containerId,
            Uint256 feeAmount,
            Address client,
            Uint32 activeAt,
            Uint32 intervalSeconds,
            Uint32 maxExecutions,
            Address wallet,
            Address feeToken,
            Address verifier,
            Uint16 redundancy,
            Bool useDeliveryInbox
        ) {
            super(
                routeId,
                containerId,
                feeAmount,
                client,
                activeAt,
                intervalSeconds,
                maxExecutions,
                wallet,
                feeToken,
                verifier,
                redundancy,
                useDeliveryInbox
            );
            this.routeId = routeId.getValue();
            this.containerId = containerId.getValue();
            this.feeAmount = feeAmount.getValue();
            this.client = client.getValue();
            this.activeAt = activeAt.getValue();
            this.intervalSeconds = intervalSeconds.getValue();
            this.maxExecutions = maxExecutions.getValue();
            this.wallet = wallet.getValue();
            this.feeToken = feeToken.getValue();
            this.verifier = verifier.getValue();
            this.redundancy = redundancy.getValue();
            this.useDeliveryInbox = useDeliveryInbox.getValue();
        }
    }
}
