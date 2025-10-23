package io.hpp.noosphere.agent.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
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
public class MyTransientClient extends Contract {

    public static final String BINARY =
        "60a034620000ce57601f6200213d38819003918201601f19168301916001600160401b03831184841017620000d2578084926040948552833981010312620000ce576200005a60206200005283620000e6565b9201620000e6565b6001600160a01b039182168015620000bc576080521660018060a01b031960045416176004556040516120419081620000fc823960805181818161013d0152818161044f0152818161072e015281816109970152818161140d0152611a3d0152f35b604051632530e88560e11b8152600490fd5b5f80fd5b634e487b7160e01b5f52604160045260245ffd5b51906001600160a01b0382168203620000ce5756fe6080806040526004361015610012575f80fd5b5f905f3560e01c9081630f163e5d1461162757508063155c440c146115b0578063181f5a77146115225780631cfe615c14611363578063293640fd146101ff57806333764c3f1461127757806347bf047614611239578063489ce0ca146111bc5780634a93fe99146108af5780634daa323f14610819578063588aa4f3146107c857806363c13a891461059c5780636b5d7ea8146105545780637ac3c02f1461050357806394079a7b146104c8578063bdad773f14610322578063c6e930d314610204578063c7b1cf54146101ff5763e55d2116146100ef575f80fd5b346101fb5760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb576101266117cb565b73ffffffffffffffffffffffffffffffffffffffff7f00000000000000000000000000000000000000000000000000000000000000001690813b156101fb575f80926024604051809581937f0b76f77400000000000000000000000000000000000000000000000000000000835267ffffffffffffffff80971660048401525af180156101f0576101b5578280f35b90809250116101c357604052005b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b6040513d5f823e3d90fd5b5f80fd5b6118e6565b346101fb575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb576040516006545f8261024383611c40565b91828252602093600190856001821691825f146102e4575050600114610289575b5061027192500383611841565b61028560405192828493845283019061176f565b0390f35b84915060065f527ff652222313e28459528d920b65115c16c04f3efc82aaedc97be59f3f377c0d3f905f915b8583106102cc575050610271935082010185610264565b805483890185015287945086939092019181016102b5565b7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00168582015261027195151560051b85010192508791506102649050565b346101fb576101007ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb5767ffffffffffffffff6004358181116101fb57610373903690600401611c22565b9060243561ffff81168091036101fb576044358015158091036101fb57610398611729565b936103a161174c565b60c4359073ffffffffffffffffffffffffffffffffffffffff928383168093036101fb5760209561041093855f94816040519c8d9a8b998a987f63c13a89000000000000000000000000000000000000000000000000000000008a5261014060048b01526101448a019061176f565b96600160248a01528960448a0152606489015260848801521660a486015260843560c48601521660e484015261010483015260e43561012483015203927f0000000000000000000000000000000000000000000000000000000000000000165af19081156101f0575f9161048c575b6020925060405191168152f35b90506020823d6020116104c0575b816104a760209383611841565b810103126101fb576104ba602092611da3565b9061047f565b3d915061049a565b346101fb575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb576020600754604051908152f35b346101fb575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb57602073ffffffffffffffffffffffffffffffffffffffff60045416604051908152f35b346101fb575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb57602067ffffffffffffffff60045460a01c16604051908152f35b346101fb576101407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb5760043567ffffffffffffffff81116101fb576105ec903690600401611c22565b6105f46117e2565b906105fd6117f5565b60643561ffff811681036101fb5760843580151581036101fb5761061f61174c565b9060e43573ffffffffffffffffffffffffffffffffffffffff811681036101fb576101049283359273ffffffffffffffffffffffffffffffffffffffff841684036101fb5761012495604051998a997f63c13a89000000000000000000000000000000000000000000000000000000008b5260048b0161014090526101448b016106a89161176f565b9863ffffffff80921660248c01521660448a015261ffff1660648901521515608488015273ffffffffffffffffffffffffffffffffffffffff1660a487015260c43560c487015273ffffffffffffffffffffffffffffffffffffffff1660e486015273ffffffffffffffffffffffffffffffffffffffff169084015280359083015203817f000000000000000000000000000000000000000000000000000000000000000073ffffffffffffffffffffffffffffffffffffffff1691815a6020945f91f180156101f0575f9061078e575b60209067ffffffffffffffff60405191168152f35b506020813d6020116107c0575b816107a860209383611841565b810103126101fb576107bb602091611da3565b610779565b3d915061079b565b346101fb575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb57602073ffffffffffffffffffffffffffffffffffffffff60055416604051908152f35b346101fb5760807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb576108506117cb565b67ffffffffffffffff6108616117e2565b9161086a6117f5565b50610873611729565b50165f52600360205263ffffffff60405f2091165f5260205261028561089b60405f20611c91565b60405191829160208352602083019061176f565b346101fb576101207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb576108e76117cb565b6108ef6117e2565b9060443561ffff8116036101fb5760643580151581036101fb576084359073ffffffffffffffffffffffffffffffffffffffff9283831683036101fb5760a43567ffffffffffffffff81116101fb5761094c903690600401611bf4565b929060c43567ffffffffffffffff81116101fb5761096e903690600401611bf4565b93909760e43567ffffffffffffffff81116101fb57610991903690600401611bf4565b939094897f00000000000000000000000000000000000000000000000000000000000000001633036111925715610f6f57604051948260a01b8460c01b178652600c86209a602087016040528b5f52600260205260405f208b8b165f5260205260ff60405f20541615610e63575b610a5490610a45610a639463ffffffff9b610a1c60208c01611808565b8c421660208c015267ffffffffffffffff60408c01991689528c60608c01981688523691611882565b9860808901998a523691611882565b9460a087019586523691611882565b9560c08501968752895f525f60205260405f208989165f52602052602060405f20950151167fffffffffffffffffffffffffffffffff000000000000000000000000000000006fffffffff0000000000000000000000006bffffffffffffffff000000008754955160201b16935160601b1693161717178255600192838301905180519067ffffffffffffffff82116101c357610b0a82610b048554611c40565b85611fbc565b602090601f8311600114610dc757610b5792915f9183610d1b575b50507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8260011b9260031b1c19161790565b90555b518051600283019167ffffffffffffffff82116101c357610b7f82610b048554611c40565b602090601f8311600114610d26579180610bd09260039695945f92610d1b5750507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8260011b9260031b1c19161790565b90555b01915180519067ffffffffffffffff82116101c357610bfc82610bf68654611c40565b86611fbc565b602092601f8311600114610c7e5750610c48925f9183610c735750507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8260011b9260031b1c19161790565b90555b16907f3222d7aae4358f0440baf74dc5b02ecc3d8c64a781b85f3ab0194c013ad882e75f80a3005b015190508780610b25565b927fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0831691855f528360205f20935f5b87828210610d0257505010610ccb575b505050811b019055610c4b565b01517fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff60f88460031b161c19169055868080610cbe565b8486015187559095019460209485019487935001610cae565b015190508b80610b25565b93929185917fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0821690845f5260205f20915f5b818110610daf5750968360039810610d79575b505050811b019055610bd3565b01517fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff60f884891b161c191690558a8080610d6c565b82890151845589959093019260209283019201610d59565b82917fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe088941691855f5260205f20925f5b818110610e4b57508411610e14575b505050811b019055610b5a565b01517fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff60f88460031b161c191690558a8080610e07565b8284015185558a969094019360209384019301610df8565b8b5f52600260205260405f208b8b165f5260205260405f2060017fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff008254161790558b5f52600160205260405f20908154680100000000000000008110156101c35760018101808455811015610f4257610a63948e8e8e610a4594610a54975f5260205f20018282167fffffffffffffffffffffffff000000000000000000000000000000000000000082541617905516907f0b2a02472e6a4ef1c8e7fbd552bbcfd4a115e97d72be9c700ad6994b0f996b4f5f80a394505090506109ff565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52603260045260245ffd5b509250929594935050837bffffffffffffffff00000000000000000000000000000000000000007fffffffff000000000000000000000000000000000000000000000000000000006004549360e01b169360a01b1691161717600455167fffffffffffffffffffffffff0000000000000000000000000000000000000000600554161760055567ffffffffffffffff81116101c35761100f600654611c40565b601f8111611133575b505f601f821160011461107b578190611063935f926110705750507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8260011b9260031b1c19161790565b6006555b61010435600755005b013590508380610b25565b7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe08216927ff652222313e28459528d920b65115c16c04f3efc82aaedc97be59f3f377c0d3f915f5b85811061111b575083600195106110e3575b505050811b01600655611067565b7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff60f88560031b161c199101351690558280806110d5565b909260206001819286860135815501940191016110c3565b7ff652222313e28459528d920b65115c16c04f3efc82aaedc97be59f3f377c0d3f601f830160051c81019160208410611188575b601f0160051c01905b81811061117d5750611018565b5f8155600101611170565b9091508190611167565b60046040517f91655201000000000000000000000000000000000000000000000000000000008152fd5b346101fb5760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb5760043573ffffffffffffffffffffffffffffffffffffffff81168091036101fb577fffffffffffffffffffffffff000000000000000000000000000000000000000060045416176004555f80f35b346101fb575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb57602060045460e01c604051908152f35b346101fb5760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb576112b96112b1611706565b600435611edc565b90610285604051928392151583526040602084015263ffffffff80825116604085015267ffffffffffffffff60208301511660608501526040820151166080840152611315606082015160c060a086015261010085019061176f565b60a06113516080840151927fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc093848883030160c089015261176f565b920151908483030160e085015261176f565b346101fb5760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb5761139a6117cb565b63ffffffff6113a76117e2565b6113af611d52565b5067ffffffffffffffff604051937f1cfe615c0000000000000000000000000000000000000000000000000000000085521660048401521660248201526101809081816044815f73ffffffffffffffffffffffffffffffffffffffff7f0000000000000000000000000000000000000000000000000000000000000000165af180156101f0575f915f916114ef575b506114ed9060405192835260208301908051825267ffffffffffffffff60208201511660208301526040810151604083015263ffffffff606082015116606083015260808101511515608083015261ffff60a08201511660a083015260c081015173ffffffffffffffffffffffffffffffffffffffff80911660c084015260e082015160e084015261010081818401511690840152610120818184015116908401526101408092015116910152565bf35b6114ed92506115149150833d851161151b575b61150c8183611841565b810190611dd9565b909161143e565b503d611502565b346101fb575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb57604051604081019080821067ffffffffffffffff8311176101c35761028591604052601881527f4d795472616e7369656e74436c69656e745f76312e302e300000000000000000602082015260405191829160208352602083019061176f565b346101fb5760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb576115e7611706565b6004355f525f60205273ffffffffffffffffffffffffffffffffffffffff60405f2091165f52602052602063ffffffff60405f2054161515604051908152f35b346101fb576020807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb57906004355f52600180835260405f209283548084528184019081955f52825f20905f5b8181106116dd5750505083611690910384611841565b6040519281840190828552518091526040840194915f5b8281106116b45785870386f35b835173ffffffffffffffffffffffffffffffffffffffff168752958101959281019284016116a7565b825473ffffffffffffffffffffffffffffffffffffffff1684529284019291850191850161167a565b6024359073ffffffffffffffffffffffffffffffffffffffff821682036101fb57565b6064359073ffffffffffffffffffffffffffffffffffffffff821682036101fb57565b60a4359073ffffffffffffffffffffffffffffffffffffffff821682036101fb57565b91908251928382525f5b8481106117b75750507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0601f845f6020809697860101520116010190565b602081830181015184830182015201611779565b6004359067ffffffffffffffff821682036101fb57565b6024359063ffffffff821682036101fb57565b6044359063ffffffff821682036101fb57565b60c0810190811067ffffffffffffffff8211176101c357604052565b610160810190811067ffffffffffffffff8211176101c357604052565b90601f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0910116810190811067ffffffffffffffff8211176101c357604052565b92919267ffffffffffffffff82116101c357604051916118ca60207fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0601f8401160184611841565b8294818452818301116101fb578281602093845f960137010152565b346101fb576040807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fb5760043567ffffffffffffffff908181168091036101fb576024358281116101fb57366023820112156101fb57611956903690602481600401359101611882565b9161195f611d52565b50611968611d52565b50815f5260209260038452845f209160019260015f528552855f209282519182116101c35761199b82610bf68654611c40565b8590601f8311600114611b51575081906119e7935f92611b465750507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8260011b9260031b1c19161790565b90555b82517f1cfe615c000000000000000000000000000000000000000000000000000000008152816004820152600160248201526101809283826044815f73ffffffffffffffffffffffffffffffffffffffff7f0000000000000000000000000000000000000000000000000000000000000000165af1918215611b3c57946114ed9394955f93611b1b575b50519384528301908051825267ffffffffffffffff60208201511660208301526040810151604083015263ffffffff606082015116606083015260808101511515608083015261ffff60a08201511660a083015260c081015173ffffffffffffffffffffffffffffffffffffffff80911660c084015260e082015160e084015261010081818401511690840152610120818184015116908401526101408092015116910152565b611b33919350863d881161151b5761150c8183611841565b9050915f611a74565b85513d5f823e3d90fd5b015190505f80610b25565b91927fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe08416855f52875f20935f905b89838310611bdd57505050908460019594939210611ba6575b505050811b0190556119ea565b01517fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff60f88460031b161c191690555f8080611b99565b818697829394978701518155019601940190611b80565b9181601f840112156101fb5782359167ffffffffffffffff83116101fb57602083818601950101116101fb57565b9080601f830112156101fb57816020611c3d93359101611882565b90565b90600182811c92168015611c87575b6020831014611c5a57565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b91607f1691611c4f565b9060405191825f8254611ca381611c40565b908184526020946001916001811690815f14611d115750600114611cd3575b505050611cd192500383611841565b565b5f90815285812095935091905b818310611cf9575050611cd193508201015f8080611cc2565b85548884018501529485019487945091830191611ce0565b915050611cd19593507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0091501682840152151560051b8201015f8080611cc2565b60405190611d5f82611824565b5f610140838281528260208201528260408201528260608201528260808201528260a08201528260c08201528260e082015282610100820152826101208201520152565b519067ffffffffffffffff821682036101fb57565b519073ffffffffffffffffffffffffffffffffffffffff821682036101fb57565b809291039161018083126101fb57807fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe091519361016092839101126101fb5760405191611e2583611824565b60208201518352611e3860408301611da3565b602084015260608201516040840152608082015163ffffffff811681036101fb57606084015260a082015180151581036101fb57608084015260c08201519061ffff821682036101fb57611ed59160a0850152611e9760e08401611db8565b60c0850152610100928381015160e086015261012093611eb8858301611db8565b9086015261014093611ecb858301611db8565b9086015201611db8565b9082015290565b919091604090815193611eee85611808565b5f85525f60208601525f8386015260609182808701528260808701528260a08701525f525f60205273ffffffffffffffffffffffffffffffffffffffff835f2091165f52602052815f2091825463ffffffff90818116968715611fb057509160039391611fa59593825198611f628a611808565b895267ffffffffffffffff8160201c1660208a0152831c1690870152611f8a60018301611c91565b90860152611f9a60028201611c91565b608086015201611c91565b60a083015260019190565b965050505050505f9190565b601f8211611fc957505050565b5f5260205f20906020601f840160051c83019310612001575b601f0160051c01905b818110611ff6575050565b5f8155600101611feb565b9091508190611fe256fea2646970667358221220cab88c72e94d9d3c3f29f5eff9e5b7b2b14a4395933326c137768f875d3e903864736f6c63430008170033";

    public static final String FUNC_MOCKDELEGATORSCHEDULEDCOMPUTECLIENT = "MockDelegatorScheduledComputeClient";

    public static final String FUNC_CANCELSUBSCRIPTION = "cancelSubscription";

    public static final String FUNC_CREATECOMPUTESUBSCRIPTION = "createComputeSubscription";

    public static final String FUNC_CREATESUBSCRIPTION = "createSubscription";

    public static final String FUNC_GETCOMPUTEINPUTS = "getComputeInputs";

    public static final String FUNC_GETDELIVERY = "getDelivery";

    public static final String FUNC_GETNODESFORREQUEST = "getNodesForRequest";

    public static final String FUNC_GETSIGNER = "getSigner";

    public static final String FUNC_HASDELIVERY = "hasDelivery";

    public static final String FUNC_LASTRECEIVEDCONTAINERID = "lastReceivedContainerId";

    public static final String FUNC_LASTRECEIVEDINTERVAL = "lastReceivedInterval";

    public static final String FUNC_LASTRECEIVEDNODE = "lastReceivedNode";

    public static final String FUNC_LASTRECEIVEDOUTPUT = "lastReceivedOutput";

    public static final String FUNC_LASTRECEIVEDSUBSCRIPTIONID = "lastReceivedSubscriptionId";

    public static final String FUNC_RECEIVEREQUESTCOMPUTE = "receiveRequestCompute";

    public static final String FUNC_REQUESTCOMPUTE = "requestCompute";

    public static final String FUNC_SENDREQUEST = "sendRequest";

    public static final String FUNC_TYPEANDVERSION = "typeAndVersion";

    public static final String FUNC_UPDATEMOCKSIGNER = "updateMockSigner";

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
    protected MyTransientClient(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected MyTransientClient(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected MyTransientClient(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected MyTransientClient(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> MockDelegatorScheduledComputeClient(BigInteger subscriptionId, byte[] inputs) {
        final Function function = new Function(
            FUNC_MOCKDELEGATORSCHEDULEDCOMPUTECLIENT,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint64(subscriptionId),
                new org.web3j.abi.datatypes.DynamicBytes(inputs)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> cancelSubscription(BigInteger subscriptionId) {
        final Function function = new Function(
            FUNC_CANCELSUBSCRIPTION,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint64(subscriptionId)),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
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

    public RemoteFunctionCall<TransactionReceipt> createSubscription(
        String containerId,
        BigInteger redundancy,
        Boolean useDeliveryInbox,
        String feeToken,
        BigInteger feeAmount,
        String wallet,
        String verifier,
        byte[] routeId
    ) {
        final Function function = new Function(
            FUNC_CREATESUBSCRIPTION,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Utf8String(containerId),
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

    public RemoteFunctionCall<String> getSigner() {
        final Function function = new Function(
            FUNC_GETSIGNER,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {})
        );
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Boolean> hasDelivery(byte[] requestId, String node) {
        final Function function = new Function(
            FUNC_HASDELIVERY,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(requestId), new org.web3j.abi.datatypes.Address(160, node)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {})
        );
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<byte[]> lastReceivedContainerId() {
        final Function function = new Function(
            FUNC_LASTRECEIVEDCONTAINERID,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {})
        );
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<BigInteger> lastReceivedInterval() {
        final Function function = new Function(
            FUNC_LASTRECEIVEDINTERVAL,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> lastReceivedNode() {
        final Function function = new Function(
            FUNC_LASTRECEIVEDNODE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {})
        );
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<byte[]> lastReceivedOutput() {
        final Function function = new Function(
            FUNC_LASTRECEIVEDOUTPUT,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {})
        );
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<BigInteger> lastReceivedSubscriptionId() {
        final Function function = new Function(
            FUNC_LASTRECEIVEDSUBSCRIPTIONID,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint64>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
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

    public RemoteFunctionCall<TransactionReceipt> requestCompute(BigInteger subscriptionId, byte[] inputs) {
        final Function function = new Function(
            FUNC_REQUESTCOMPUTE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint64(subscriptionId),
                new org.web3j.abi.datatypes.DynamicBytes(inputs)
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

    public RemoteFunctionCall<TransactionReceipt> updateMockSigner(String newSigner) {
        final Function function = new Function(
            FUNC_UPDATEMOCKSIGNER,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newSigner)),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
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
    public static MyTransientClient load(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return new MyTransientClient(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static MyTransientClient load(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return new MyTransientClient(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static MyTransientClient load(
        String contractAddress,
        Web3j web3j,
        Credentials credentials,
        ContractGasProvider contractGasProvider
    ) {
        return new MyTransientClient(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static MyTransientClient load(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider
    ) {
        return new MyTransientClient(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<MyTransientClient> deploy(
        Web3j web3j,
        Credentials credentials,
        ContractGasProvider contractGasProvider,
        String router,
        String signer
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, router), new org.web3j.abi.datatypes.Address(160, signer))
        );
        return deployRemoteCall(MyTransientClient.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<MyTransientClient> deploy(
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider,
        String router,
        String signer
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, router), new org.web3j.abi.datatypes.Address(160, signer))
        );
        return deployRemoteCall(MyTransientClient.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<MyTransientClient> deploy(
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit,
        String router,
        String signer
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, router), new org.web3j.abi.datatypes.Address(160, signer))
        );
        return deployRemoteCall(MyTransientClient.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<MyTransientClient> deploy(
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit,
        String router,
        String signer
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, router), new org.web3j.abi.datatypes.Address(160, signer))
        );
        return deployRemoteCall(MyTransientClient.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
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
