package io.hpp.noosphere.agent.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
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
public class TransientComputeClient extends Contract {

    public static final String BINARY = "";

    public static final String FUNC_CREATECOMPUTESUBSCRIPTION = "createComputeSubscription";

    public static final String FUNC_GETCOMPUTEINPUTS = "getComputeInputs";

    public static final String FUNC_GETDELIVERY = "getDelivery";

    public static final String FUNC_GETNODESFORREQUEST = "getNodesForRequest";

    public static final String FUNC_HASDELIVERY = "hasDelivery";

    public static final String FUNC_RECEIVEREQUESTCOMPUTE = "receiveRequestCompute";

    public static final String FUNC_SENDREQUEST = "sendRequest";

    public static final String FUNC_TYPEANDVERSION = "typeAndVersion";

    public static final Event DELIVERYCLEARED_EVENT = new Event(
        "DeliveryCleared",
        Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Address>(true) {})
    );

    public static final Event DELIVERYSUBMITTED_EVENT = new Event(
        "DeliverySubmitted",
        Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Address>(true) {})
    );

    public static final Event NODEADDED_EVENT = new Event(
        "NodeAdded",
        Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Address>(true) {})
    );

    public static final Event NODEREMOVED_EVENT = new Event(
        "NodeRemoved",
        Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Address>(true) {})
    );

    @Deprecated
    protected TransientComputeClient(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TransientComputeClient(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TransientComputeClient(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TransientComputeClient(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> createComputeSubscription(
        String containerId,
        BigInteger maxExecutions,
        BigInteger intervalSeconds,
        BigInteger redundancy,
        Boolean useDeliveryInbox,
        String feeToken,
        BigInteger feeAmount,
        String wallet,
        String verifier,
        byte[] routeId
    ) {
        final Function function = new Function(
            FUNC_CREATECOMPUTESUBSCRIPTION,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Utf8String(containerId),
                new org.web3j.abi.datatypes.generated.Uint32(maxExecutions),
                new org.web3j.abi.datatypes.generated.Uint32(intervalSeconds),
                new org.web3j.abi.datatypes.generated.Uint16(redundancy),
                new org.web3j.abi.datatypes.Bool(useDeliveryInbox),
                new org.web3j.abi.datatypes.Address(160, feeToken),
                new org.web3j.abi.datatypes.generated.Uint256(feeAmount),
                new org.web3j.abi.datatypes.Address(160, wallet),
                new org.web3j.abi.datatypes.Address(160, verifier),
                new org.web3j.abi.datatypes.generated.Bytes32(routeId)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<byte[]> getComputeInputs(
        BigInteger subscriptionId,
        BigInteger interval,
        BigInteger timestamp,
        String caller
    ) {
        final Function function = new Function(
            FUNC_GETCOMPUTEINPUTS,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint64(subscriptionId),
                new org.web3j.abi.datatypes.generated.Uint32(interval),
                new org.web3j.abi.datatypes.generated.Uint32(timestamp),
                new org.web3j.abi.datatypes.Address(160, caller)
            ),
            Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {})
        );
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<Tuple2<Boolean, PendingDelivery>> getDelivery(byte[] requestId, String node) {
        final Function function = new Function(
            FUNC_GETDELIVERY,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(requestId), new org.web3j.abi.datatypes.Address(160, node)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<PendingDelivery>() {})
        );
        return new RemoteFunctionCall<Tuple2<Boolean, PendingDelivery>>(
            function,
            new Callable<Tuple2<Boolean, PendingDelivery>>() {
                @Override
                public Tuple2<Boolean, PendingDelivery> call() throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple2<Boolean, PendingDelivery>((Boolean) results.get(0).getValue(), (PendingDelivery) results.get(1));
                }
            }
        );
    }

    public RemoteFunctionCall<List> getNodesForRequest(byte[] requestId) {
        final Function function = new Function(
            FUNC_GETNODESFORREQUEST,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(requestId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {})
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

    public RemoteFunctionCall<Boolean> hasDelivery(byte[] requestId, String node) {
        final Function function = new Function(
            FUNC_HASDELIVERY,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(requestId), new org.web3j.abi.datatypes.Address(160, node)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {})
        );
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> receiveRequestCompute(
        BigInteger subscriptionId,
        BigInteger interval,
        BigInteger numRedundantDeliveries,
        Boolean useDeliveryInbox,
        String node,
        byte[] input,
        byte[] output,
        byte[] proof,
        byte[] containerId
    ) {
        final Function function = new Function(
            FUNC_RECEIVEREQUESTCOMPUTE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint64(subscriptionId),
                new org.web3j.abi.datatypes.generated.Uint32(interval),
                new org.web3j.abi.datatypes.generated.Uint16(numRedundantDeliveries),
                new org.web3j.abi.datatypes.Bool(useDeliveryInbox),
                new org.web3j.abi.datatypes.Address(160, node),
                new org.web3j.abi.datatypes.DynamicBytes(input),
                new org.web3j.abi.datatypes.DynamicBytes(output),
                new org.web3j.abi.datatypes.DynamicBytes(proof),
                new org.web3j.abi.datatypes.generated.Bytes32(containerId)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> sendRequest(BigInteger subscriptionId, BigInteger interval) {
        final Function function = new Function(
            FUNC_SENDREQUEST,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint64(subscriptionId),
                new org.web3j.abi.datatypes.generated.Uint32(interval)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> typeAndVersion() {
        final Function function = new Function(
            FUNC_TYPEANDVERSION,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {})
        );
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public static List<DeliveryClearedEventResponse> getDeliveryClearedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DELIVERYCLEARED_EVENT, transactionReceipt);
        ArrayList<DeliveryClearedEventResponse> responses = new ArrayList<DeliveryClearedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DeliveryClearedEventResponse typedResponse = new DeliveryClearedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.node = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DeliveryClearedEventResponse getDeliveryClearedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DELIVERYCLEARED_EVENT, log);
        DeliveryClearedEventResponse typedResponse = new DeliveryClearedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.node = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DeliveryClearedEventResponse> deliveryClearedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDeliveryClearedEventFromLog(log));
    }

    public Flowable<DeliveryClearedEventResponse> deliveryClearedEventFlowable(
        DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock
    ) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DELIVERYCLEARED_EVENT));
        return deliveryClearedEventFlowable(filter);
    }

    public static List<DeliverySubmittedEventResponse> getDeliverySubmittedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DELIVERYSUBMITTED_EVENT, transactionReceipt);
        ArrayList<DeliverySubmittedEventResponse> responses = new ArrayList<DeliverySubmittedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DeliverySubmittedEventResponse typedResponse = new DeliverySubmittedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.node = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DeliverySubmittedEventResponse getDeliverySubmittedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DELIVERYSUBMITTED_EVENT, log);
        DeliverySubmittedEventResponse typedResponse = new DeliverySubmittedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.node = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DeliverySubmittedEventResponse> deliverySubmittedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDeliverySubmittedEventFromLog(log));
    }

    public Flowable<DeliverySubmittedEventResponse> deliverySubmittedEventFlowable(
        DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock
    ) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DELIVERYSUBMITTED_EVENT));
        return deliverySubmittedEventFlowable(filter);
    }

    public static List<NodeAddedEventResponse> getNodeAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(NODEADDED_EVENT, transactionReceipt);
        ArrayList<NodeAddedEventResponse> responses = new ArrayList<NodeAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NodeAddedEventResponse typedResponse = new NodeAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.node = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static NodeAddedEventResponse getNodeAddedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(NODEADDED_EVENT, log);
        NodeAddedEventResponse typedResponse = new NodeAddedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.node = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<NodeAddedEventResponse> nodeAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getNodeAddedEventFromLog(log));
    }

    public Flowable<NodeAddedEventResponse> nodeAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(NODEADDED_EVENT));
        return nodeAddedEventFlowable(filter);
    }

    public static List<NodeRemovedEventResponse> getNodeRemovedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(NODEREMOVED_EVENT, transactionReceipt);
        ArrayList<NodeRemovedEventResponse> responses = new ArrayList<NodeRemovedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NodeRemovedEventResponse typedResponse = new NodeRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.node = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static NodeRemovedEventResponse getNodeRemovedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(NODEREMOVED_EVENT, log);
        NodeRemovedEventResponse typedResponse = new NodeRemovedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.node = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<NodeRemovedEventResponse> nodeRemovedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getNodeRemovedEventFromLog(log));
    }

    public Flowable<NodeRemovedEventResponse> nodeRemovedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(NODEREMOVED_EVENT));
        return nodeRemovedEventFlowable(filter);
    }

    @Deprecated
    public static TransientComputeClient load(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return new TransientComputeClient(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TransientComputeClient load(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return new TransientComputeClient(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TransientComputeClient load(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        ContractGasProvider contractGasProvider
    ) {
        return new TransientComputeClient(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TransientComputeClient load(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider
    ) {
        return new TransientComputeClient(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<TransientComputeClient> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TransientComputeClient.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<TransientComputeClient> deploy(
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return deployRemoteCall(TransientComputeClient.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<TransientComputeClient> deploy(
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider
    ) {
        return deployRemoteCall(TransientComputeClient.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<TransientComputeClient> deploy(
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return deployRemoteCall(TransientComputeClient.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class PendingDelivery extends DynamicStruct {

        public BigInteger timestamp;

        public BigInteger subscriptionId;

        public BigInteger interval;

        public byte[] input;

        public byte[] output;

        public byte[] proof;

        public PendingDelivery(
            BigInteger timestamp,
            BigInteger subscriptionId,
            BigInteger interval,
            byte[] input,
            byte[] output,
            byte[] proof
        ) {
            super(
                new org.web3j.abi.datatypes.generated.Uint32(timestamp),
                new org.web3j.abi.datatypes.generated.Uint64(subscriptionId),
                new org.web3j.abi.datatypes.generated.Uint32(interval),
                new org.web3j.abi.datatypes.DynamicBytes(input),
                new org.web3j.abi.datatypes.DynamicBytes(output),
                new org.web3j.abi.datatypes.DynamicBytes(proof)
            );
            this.timestamp = timestamp;
            this.subscriptionId = subscriptionId;
            this.interval = interval;
            this.input = input;
            this.output = output;
            this.proof = proof;
        }

        public PendingDelivery(
            Uint32 timestamp,
            Uint64 subscriptionId,
            Uint32 interval,
            DynamicBytes input,
            DynamicBytes output,
            DynamicBytes proof
        ) {
            super(timestamp, subscriptionId, interval, input, output, proof);
            this.timestamp = timestamp.getValue();
            this.subscriptionId = subscriptionId.getValue();
            this.interval = interval.getValue();
            this.input = input.getValue();
            this.output = output.getValue();
            this.proof = proof.getValue();
        }
    }

    public static class Commitment extends StaticStruct {

        public byte[] requestId;

        public BigInteger subscriptionId;

        public byte[] containerId;

        public BigInteger interval;

        public Boolean useDeliveryInbox;

        public BigInteger redundancy;

        public String walletAddress;

        public BigInteger feeAmount;

        public String feeToken;

        public String verifier;

        public String coordinator;

        public Commitment(
            byte[] requestId,
            BigInteger subscriptionId,
            byte[] containerId,
            BigInteger interval,
            Boolean useDeliveryInbox,
            BigInteger redundancy,
            String walletAddress,
            BigInteger feeAmount,
            String feeToken,
            String verifier,
            String coordinator
        ) {
            super(
                new org.web3j.abi.datatypes.generated.Bytes32(requestId),
                new org.web3j.abi.datatypes.generated.Uint64(subscriptionId),
                new org.web3j.abi.datatypes.generated.Bytes32(containerId),
                new org.web3j.abi.datatypes.generated.Uint32(interval),
                new org.web3j.abi.datatypes.Bool(useDeliveryInbox),
                new org.web3j.abi.datatypes.generated.Uint16(redundancy),
                new org.web3j.abi.datatypes.Address(160, walletAddress),
                new org.web3j.abi.datatypes.generated.Uint256(feeAmount),
                new org.web3j.abi.datatypes.Address(160, feeToken),
                new org.web3j.abi.datatypes.Address(160, verifier),
                new org.web3j.abi.datatypes.Address(160, coordinator)
            );
            this.requestId = requestId;
            this.subscriptionId = subscriptionId;
            this.containerId = containerId;
            this.interval = interval;
            this.useDeliveryInbox = useDeliveryInbox;
            this.redundancy = redundancy;
            this.walletAddress = walletAddress;
            this.feeAmount = feeAmount;
            this.feeToken = feeToken;
            this.verifier = verifier;
            this.coordinator = coordinator;
        }

        public Commitment(
            Bytes32 requestId,
            Uint64 subscriptionId,
            Bytes32 containerId,
            Uint32 interval,
            Bool useDeliveryInbox,
            Uint16 redundancy,
            Address walletAddress,
            Uint256 feeAmount,
            Address feeToken,
            Address verifier,
            Address coordinator
        ) {
            super(
                requestId,
                subscriptionId,
                containerId,
                interval,
                useDeliveryInbox,
                redundancy,
                walletAddress,
                feeAmount,
                feeToken,
                verifier,
                coordinator
            );
            this.requestId = requestId.getValue();
            this.subscriptionId = subscriptionId.getValue();
            this.containerId = containerId.getValue();
            this.interval = interval.getValue();
            this.useDeliveryInbox = useDeliveryInbox.getValue();
            this.redundancy = redundancy.getValue();
            this.walletAddress = walletAddress.getValue();
            this.feeAmount = feeAmount.getValue();
            this.feeToken = feeToken.getValue();
            this.verifier = verifier.getValue();
            this.coordinator = coordinator.getValue();
        }
    }

    public static class DeliveryClearedEventResponse extends BaseEventResponse {

        public byte[] requestId;

        public String node;
    }

    public static class DeliverySubmittedEventResponse extends BaseEventResponse {

        public byte[] requestId;

        public String node;
    }

    public static class NodeAddedEventResponse extends BaseEventResponse {

        public byte[] requestId;

        public String node;
    }

    public static class NodeRemovedEventResponse extends BaseEventResponse {

        public byte[] requestId;

        public String node;
    }
}
