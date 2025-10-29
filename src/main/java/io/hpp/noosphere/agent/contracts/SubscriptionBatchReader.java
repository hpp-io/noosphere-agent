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
        "60c03461008557601f6109b638819003918201601f19168301916001600160401b0383118484101761008957808492604094855283398101031261008557610052602061004b8361009d565b920161009d565b6001600160a01b039182166080521660a05260405161090490816100b2823960805181610102015260a051816104a80152f35b5f80fd5b634e487b7160e01b5f52604160045260245ffd5b51906001600160a01b03821682036100855756fe604060808152600480361015610013575f80fd5b60e05f35811c806331f68014146103e05763a566fc7c14610032575f80fd5b3461029b57827ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc36011261029b5781359067ffffffffffffffff93848316830361029b576024906024358681169081810361029b57610092868992610855565b16936100b56100a0866107dd565b956100ad8551978861079c565b8087526107dd565b927fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe060209401845f5b828110610381575050508673ffffffffffffffffffffffffffffffffffffffff97887f000000000000000000000000000000000000000000000000000000000000000016915b8b811690868210156102b0578c61013b8483610855565b16918551907fa50366b80000000000000000000000000000000000000000000000000000000082528d8201526101809081818c81895afa9182156102a6578f9392918d915f936101a9575b5050936101a19161019a8260019697610841565b528c610841565b500116610124565b92509493505083813d831161029f575b6101c3818361079c565b8101031261029b578551926101d78461077f565b80518452898101518a850152868101518785015260606101f881830161089c565b9085015260806102098183016108bd565b9085015260a061021a8183016108bd565b9085015260c061022b8183016108bd565b9085015261023a88820161089c565b8885015261010061024c81830161089c565b9085015261012061025e81830161089c565b90850152610140610270818301610832565b908501526101608091015192831515840361029b579084019290925290918d91908b8061019a610186565b5f80fd5b503d6101b9565b87513d5f823e3d90fd5b85888b878e815193808501918186528451809352818487019501935f915b8483106102db5787870388f35b8551805188528085015188860152808301518884015260608082015183169089015260808082015163ffffffff908116918a019190915260a0808301518216908a015260c080830151909116908901528981015182168a89015261010080820151831690890152610120808201518316908901526101408082015161ffff16908901526101609081015115159088015261018090960195948301946001909201916102ce565b835161038c8161077f565b5f81525f838201525f858201525f60608201525f60808201525f60a08201525f60c08201525f868201525f6101008201525f6101208201525f6101408201525f61016082015282828b0101520185906100de565b50503461029b57817ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc36011261029b5767ffffffffffffffff91813583811161029b576104309036908401610705565b909360249460243582811161029b57918693610450879436908601610705565b94909561045c836107dd565b956104698951978861079c565b838752610475846107dd565b977fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0602099015f5b8181106106d25750507f000000000000000000000000000000000000000000000000000000000000000073ffffffffffffffffffffffffffffffffffffffff16945f5b81811061053c578b8b8b8251928284938401908085528351809252808386019401925f905b8382106105125786860387f35b8451805161ffff168752830151151586840152879650948501949382019360019190910190610505565b61054b81838b9e9c9d9e6107f5565b35888116810361029b576105608286866107f5565b359063ffffffff8216820361029b578b519160a01b9060c01b1781528c8b600c8320918084018092527fbc85694f00000000000000000000000000000000000000000000000000000000825282898501528189818d5afa9283156106c8578f905f9461068f575b5050508b51907f368bf464000000000000000000000000000000000000000000000000000000008252888201528d8188818c5afa908115610685578d9392918f918e905f9261064e575b509461063892859261ffff600198519661062a88610736565b168652151590850152610841565b52610643818d610841565b50019a99989a6104e0565b9550505083813d831161067e575b610666818361079c565b8101031261029b5791518c92908e908d610638610611565b503d61065c565b8c513d5f823e3d90fd5b9080929394503d83116106c1575b6106a7818561079c565b8101031261029b576106b890610832565b908e8e816105c7565b503d61069d565b8d513d5f823e3d90fd5b99808b8b829e9c9d83908e9d9b9d51926106eb84610736565b5f84525f838501520101520190509a99989a97959761049d565b9181601f8401121561029b5782359167ffffffffffffffff831161029b576020808501948460051b01011161029b57565b6040810190811067ffffffffffffffff82111761075257604052565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b610180810190811067ffffffffffffffff82111761075257604052565b90601f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0910116810190811067ffffffffffffffff82111761075257604052565b67ffffffffffffffff81116107525760051b60200190565b91908110156108055760051b0190565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52603260045260245ffd5b519061ffff8216820361029b57565b80518210156108055760209160051b010190565b67ffffffffffffffff918216908216039190821161086f57565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b519073ffffffffffffffffffffffffffffffffffffffff8216820361029b57565b519063ffffffff8216820361029b5756fea2646970667358221220f9ca20236bb8ad5662ab236764c3470733a2d509e54290c4bc3317237ac4e05664736f6c63430008170033";

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
