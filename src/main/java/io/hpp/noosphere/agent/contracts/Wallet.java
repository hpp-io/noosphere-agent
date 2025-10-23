package io.hpp.noosphere.agent.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
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
public class Wallet extends Contract {

    public static final String BINARY =
        "60a0346200012957601f62001fc538819003918201601f19168301916001600160401b038311848410176200012d578084926040948552833981010312620001295760206200004e8262000141565b916001600160a01b0391829162000066910162000141565b1691821562000111575f80546001600160a01b03198116851782556040519491908416907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09080a31690811562000102575060805260018055604051611e6e908162000157823960805181818161032f0152818161066301528181610a4301528181610bdf01528181611006015281816113c401526116440152f35b632530e88560e11b8152600490fd5b604051631e4fbdf760e01b81525f6004820152602490fd5b5f80fd5b634e487b7160e01b5f52604160045260245ffd5b51906001600160a01b0382168203620001295756fe60806040908082526004908136101561004b575b5050361561001f575f80fd5b7fe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c60205f9251348152a2005b5f3560e01c90816311d8668c146115ce57508063181f5a77146114c2578063303d0c6b14611398578063711dc79214610f98578063715018a614610efe5780637c72ccf614610e9c5780638612d04914610e255780638da5cb5b14610dd457806394862f9c14610d6a578063a04889e914610b6c578063b676899814610a17578063b9b3e06a146109a2578063c3909fa114610940578063cb7aa2fa1461060b578063dd62ed3e14610597578063e1f21c6714610524578063ec8669bd146102e2578063f2fde38b146102015763f3fef3a3146101285780610013565b90346101fd57807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd5761015f611918565b906024359261016c611de8565b610174611b1e565b61017d83611b59565b84116101d6575073ffffffffffffffffffffffffffffffffffffffff7f884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364926020926101c9863384611c1f565b519485521692a260018055005b90517f356680b7000000000000000000000000000000000000000000000000000000008152fd5b5f80fd5b50346101fd5760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd57610239611918565b90610242611de8565b73ffffffffffffffffffffffffffffffffffffffff8092169283156102b35750505f54827fffffffffffffffffffffffff00000000000000000000000000000000000000008216175f55167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e05f80a3005b905f60249251917f1e4fbdf7000000000000000000000000000000000000000000000000000000008352820152fd5b5090346101fd576020807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd57823573ffffffffffffffffffffffffffffffffffffffff91827f00000000000000000000000000000000000000000000000000000000000000001633036104fb5761035d611b1e565b815f5260058152835f209284519061037482611a36565b808554168252806001860154169483830195865260028101548784015260ff8860038301549260608601938452015461ffff80821660808701528160101c1660a0860152851c1615801560c08501526104d3575191818151165f5260038452865f20828751165f528452865f20548084116104ab57907f7d370a94e557d98fc60928112c72d25d1a18d792043237cc9e3715666c9e61e19596979861041a8585946119ef565b838351165f52600387528a5f20848b51165f5287528a5f2055828951165f5260028652895f2061044b8682546119ef565b9055828251165f528552885f20828951165f528552885f2061046e858254611a29565b9055865f526005855261049a895f2060045f918281558260018201558260028201558260038201550155565b51169551169551908152a460018055005b8888517f39c8bcf9000000000000000000000000000000000000000000000000000000008152fd5b8787517ff0da79df000000000000000000000000000000000000000000000000000000008152fd5b505050517fc41a5b09000000000000000000000000000000000000000000000000000000008152fd5b50346101fd577f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92560206105563661195e565b91610562969196611de8565b73ffffffffffffffffffffffffffffffffffffffff80911695865f528452815f20961695865f52835281815f205551908152a3005b5090346101fd57807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd576020916105d2611918565b6105da61193b565b9173ffffffffffffffffffffffffffffffffffffffff8092165f528452825f2091165f528252805f20549051908152f35b5090346101fd5760607ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd57813561064661193b565b6044359273ffffffffffffffffffffffffffffffffffffffff94857f000000000000000000000000000000000000000000000000000000000000000016330361091957610691611b1e565b835f526005602052815f20928184019586549160ff8360201c16156108f15781156108c9576003860191825481116108a15761ffff93848082169160101c1610156108795791869593918995938b809954165f526003602052897ffab66afb795acabdb9d25b8483330bd32d4ac9b22e83e44ae20cffc94101a33e8a86895f209a60018d019b838d54165f526020528a5f2061072e8782546119ef565b9055828c54165f5260026020528a5f206107498782546119ef565b90556107568689546119ef565b885580547fffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000ffff8116601091821c8416600101841690911b63ffff0000161781555460101c16996107a9858784845416611c1f565b54895194855261ffff8b166020860152169d8e941692604090a454938415918215998a61086c575b5050506107df575b60018055005b7f7d370a94e557d98fc60928112c72d25d1a18d792043237cc9e3715666c9e61e194602094541696610849575b50845f5260058352610837815f2060045f918281558260018201558260028201558260038201550155565b51908152a45f808080808080806107d9565b865f528352805f20875f528352805f20610864838254611a29565b90555f61080c565b54161490505f80806107d1565b8486517f3a082e21000000000000000000000000000000000000000000000000000000008152fd5b8486517f5d4b1077000000000000000000000000000000000000000000000000000000008152fd5b8385517f1f2a2005000000000000000000000000000000000000000000000000000000008152fd5b8385517ff0da79df000000000000000000000000000000000000000000000000000000008152fd5b90517fc41a5b09000000000000000000000000000000000000000000000000000000008152fd5b82346101fd5760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd5760209073ffffffffffffffffffffffffffffffffffffffff610990611918565b165f5260028252805f20549051908152f35b82346101fd57807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd576020906109dc611918565b6109e461193b565b9073ffffffffffffffffffffffffffffffffffffffff8091165f5260038452825f2091165f528252805f20549051908152f35b5090346101fd57610a273661195e565b73ffffffffffffffffffffffffffffffffffffffff93919392837f0000000000000000000000000000000000000000000000000000000000000000163303610b44578390610a73611b1e565b1692835f5260209060038252835f20951694855f528152825f20548211610b1c575f908495967f813582499997f00ba0142c7813740a6e381df71a63d11d8c8f208f66b7795d26958352600382528483208884528252848320610ad78582546119ef565b905587835260028252848320610aee8582546119ef565b905586835281528382208783528152838220610b0b848254611a29565b90558351928352820152a360018055005b8583517f356680b7000000000000000000000000000000000000000000000000000000008152fd5b8583517fc41a5b09000000000000000000000000000000000000000000000000000000008152fd5b5090346101fd57807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd57610ba4611918565b9060243567ffffffffffffffff81116101fd57610bc490369085016119be565b909273ffffffffffffffffffffffffffffffffffffffff91827f0000000000000000000000000000000000000000000000000000000000000000163303610d4257610c0d611b1e565b5f5b818110610c1c5760018055005b610c27818388611ac0565b8581013580610c3b575b5050600101610c0f565b85851690815f5260208a81528a895f2091808601928a610c5a85611afd565b165f5281528a5f2054848110610d1a57610ce27fd1398bee19313d6bf672ccb116e51f4a1a947e91c757907f51fbb5b5e56c698f9486948e8e95610ca660019d9c9b9a610ce8976119ef565b928a5f528152815f209087610cba86611afd565b165f52525f2055610cdd85610cce83611afd565b610cd78b611afd565b90611c1f565b611afd565b95611afd565b8b5173ffffffffffffffffffffffffffffffffffffffff96909616865260208601929092521692604090a3905f610c31565b828c517f13be252b000000000000000000000000000000000000000000000000000000008152fd5b8584517fc41a5b09000000000000000000000000000000000000000000000000000000008152fd5b5090346101fd5760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd5781602092355f5260058352815f20015460ff81841c165f14610dc95761ffff809160101c16915b5191168152f35b5061ffff5f91610dc2565b82346101fd575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd5760209073ffffffffffffffffffffffffffffffffffffffff5f54169051908152f35b82346101fd57807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd57602090610e5f611918565b610e6761193b565b9073ffffffffffffffffffffffffffffffffffffffff8091165f5260038452825f2091165f528252805f205415159051908152f35b5090346101fd5760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd5781602092355f526005835260ff825f2091820154841c165f14610ef65760030154905b51908152f35b505f90610ef0565b346101fd575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd57610f34611de8565b5f73ffffffffffffffffffffffffffffffffffffffff81547fffffffffffffffffffffffff000000000000000000000000000000000000000081168355167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a3005b5090346101fd57807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd5781356024803567ffffffffffffffff81116101fd57610fea90369086016119be565b73ffffffffffffffffffffffffffffffffffffffff94919491827f000000000000000000000000000000000000000000000000000000000000000016330361137057611034611b1e565b845f5260209260058452815f209288840180549060ff82881c16156113485761ffff91828082169160101c161015611320575f995f9a600188019b868d54169a5b86821061128457505060038801998a54821161125c578689541690815f5260038b52885f20815f528b52885f205480841161123457906110ba846110ee9594936119ef565b915f5260038c52895f20905f528b52885f2055868d54165f5260028a52875f206110e58282546119ef565b90558a546119ef565b895581547fffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000ffff8116601091821c8516600101851690911b63ffff000016178255815460101c8316938a5f888e5b8483106111af575050505050505416146111545760018055005b807f7d370a94e557d98fc60928112c72d25d1a18d792043237cc9e3715666c9e61e1955493541696541696826108495750845f5260058352610837815f2060045f918281558260018201558260028201558260038201550155565b6001937ffab66afb795acabdb9d25b8483330bd32d4ac9b22e83e44ae20cffc94101a33e8b6111df86898b611ac0565b936112278d836112068185541698610cdd6111f982611afd565b9a8c8301359b8c91611c1f565b935416975193849316968390929161ffff6020916040840195845216910152565b0390a4018b90888e61113a565b8f8a517f39c8bcf9000000000000000000000000000000000000000000000000000000008152fd5b8d88517f5d4b1077000000000000000000000000000000000000000000000000000000008152fd5b90918b8861129d8d611297878c8a611ac0565b01611afd565b16036112c5576112bd6001918a6112b5868b89611ac0565b013590611a29565b920190611075565b60648f6018848e8d51937f08c379a00000000000000000000000000000000000000000000000000000000085528401528201527f4d69736d617463686564207061796d656e7420746f6b656e00000000000000006044820152fd5b8a85517f3a082e21000000000000000000000000000000000000000000000000000000008152fd5b8a85517ff0da79df000000000000000000000000000000000000000000000000000000008152fd5b8690517fc41a5b09000000000000000000000000000000000000000000000000000000008152fd5b5090346101fd576113a83661195e565b73ffffffffffffffffffffffffffffffffffffffff93919392837f0000000000000000000000000000000000000000000000000000000000000000163303610b44576113f2611b1e565b6113fb85611b59565b8211610b1c57831692835f52602090868252835f20951694855f52815281835f20541061149a576001908495967f813582499997f00ba0142c7813740a6e381df71a63d11d8c8f208f66b7795d26955f528152835f20875f528152835f206114648482546119ef565b9055855f5260038152835f20875f528152835f20611483848254611a29565b9055865f5260028152835f20610b0b848254611a29565b8583517f13be252b000000000000000000000000000000000000000000000000000000008152fd5b5090346101fd575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd578051918183019083821067ffffffffffffffff8311176115a257508152600c825260207f57616c6c657420312e302e30000000000000000000000000000000000000000060208401528151928391602083528151918260208501525f5b83811061158c5750505f83830185015250601f017fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0168101030190f35b818101830151878201870152869450820161154f565b6041907f4e487b71000000000000000000000000000000000000000000000000000000005f525260245ffd5b9050346101fd5760a07ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126101fd57611607611918565b9261161061193b565b9360443591606435936084359261ffff808516928386036101fd5773ffffffffffffffffffffffffffffffffffffffff90817f00000000000000000000000000000000000000000000000000000000000000001633036118f15750611673611b1e565b875f526020926005845260ff8a875f200154851c166118c9576116958b611b59565b88116118a157811698895f5280845281865f209b169a8b5f52845287865f20541061187a5790828993928b5f52808652875f208d5f528652875f208a8154906116dd916119ef565b90558b5f5260038652875f208d5f528652875f208a8154906116fe91611a29565b90558c5f5260028652875f208a81549061171791611a29565b9055875161172481611a36565b8c81528d8782019081528982018c815260608301918d8352608084019a8b5260a08401965f885260c085019960018b525f5260058b52808d5f20955116907fffffffffffffffffffffffff0000000000000000000000000000000000000000918287541617865560018601925116908254161790555160028301555160038201550195511685547fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff000016178555511661180c9084907fffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000ffff63ffff000083549260101b169116179055565b5182547fffffffffffffffffffffffffffffffffffffffffffffffffffffff00ffffffff1664ff0000000091151590921b161790555191825261ffff1660208201527f21a0cbcf4ecd8cd3ff869836abc07ebc6ecf06048838bc149520ad654264f03390604090a460018055005b85517f13be252b000000000000000000000000000000000000000000000000000000008152fd5b8986517f356680b7000000000000000000000000000000000000000000000000000000008152fd5b8986517f85112af6000000000000000000000000000000000000000000000000000000008152fd5b807fc41a5b09000000000000000000000000000000000000000000000000000000008b9252fd5b6004359073ffffffffffffffffffffffffffffffffffffffff821682036101fd57565b6024359073ffffffffffffffffffffffffffffffffffffffff821682036101fd57565b7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc60609101126101fd5773ffffffffffffffffffffffffffffffffffffffff9060043582811681036101fd579160243590811681036101fd579060443590565b9181601f840112156101fd5782359167ffffffffffffffff83116101fd57602080850194606085020101116101fd57565b919082039182116119fc57565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b919082018092116119fc57565b60e0810190811067ffffffffffffffff821117611a5257604052565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b90601f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0910116810190811067ffffffffffffffff821117611a5257604052565b9190811015611ad0576060020190565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52603260045260245ffd5b3573ffffffffffffffffffffffffffffffffffffffff811681036101fd5790565b600260015414611b2f576002600155565b60046040517f3ee5aeb5000000000000000000000000000000000000000000000000000000008152fd5b73ffffffffffffffffffffffffffffffffffffffff16805f52600260205260405f205481155f14611b9257611b8f9150476119ef565b90565b6020602492604051938480927f70a082310000000000000000000000000000000000000000000000000000000082523060048301525afa8015611c14575f90611be0575b611b8f92506119ef565b506020823d602011611c0c575b81611bfa60209383611a7f565b810103126101fd57611b8f9151611bd6565b3d9150611bed565b6040513d5f823e3d90fd5b73ffffffffffffffffffffffffffffffffffffffff9190821680611d275750824710611cef575f809381938293165af13d15611ce7573d9067ffffffffffffffff8211611a525760405191611c9c60207fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0601f8401160184611a7f565b82523d5f602084013e5b15611cae5750565b805115611cbd57602081519101fd5b60046040517fd6bda275000000000000000000000000000000000000000000000000000000008152fd5b606090611ca6565b6044834790604051917fcf47918100000000000000000000000000000000000000000000000000000000835260048301526024820152fd5b6040517fa9059cbb00000000000000000000000000000000000000000000000000000000602080830191825273ffffffffffffffffffffffffffffffffffffffff94909416602483015260448083019690965294815290939192505f9190611d90606482611a7f565b519082855af115611c14575f513d611ddf5750803b155b611dae5750565b602490604051907f5274afe70000000000000000000000000000000000000000000000000000000082526004820152fd5b60011415611da7565b73ffffffffffffffffffffffffffffffffffffffff5f54163303611e0857565b60246040517f118cdaa7000000000000000000000000000000000000000000000000000000008152336004820152fdfea26469706673582212202c4c8cb2a1efdc4038374d6d2a1a499d4e7be74798c18330a561bac88dac896f64736f6c63430008170033";

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_DISBURSEFORFULFILLMENT = "disburseForFulfillment";

    public static final String FUNC_DISBURSEFORREQUEST = "disburseForRequest";

    public static final String FUNC_ISLOCKED = "isLocked";

    public static final String FUNC_LOCKESCROW = "lockEscrow";

    public static final String FUNC_LOCKFORREQUEST = "lockForRequest";

    public static final String FUNC_LOCKEDOF = "lockedOf";

    public static final String FUNC_LOCKEDOFREQUEST = "lockedOfRequest";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PAIDCOUNTOFREQUEST = "paidCountOfRequest";

    public static final String FUNC_RELEASEESCROW = "releaseEscrow";

    public static final String FUNC_RELEASEFORREQUEST = "releaseForRequest";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_TOTALLOCKEDFOR = "totalLockedFor";

    public static final String FUNC_TRANSFERBYROUTER = "transferByRouter";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_TYPEANDVERSION = "typeAndVersion";

    public static final String FUNC_WITHDRAW = "withdraw";

    public static final Event APPROVAL_EVENT = new Event(
        "Approval",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Address>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Uint256>() {}
        )
    );

    public static final Event DEPOSIT_EVENT = new Event(
        "Deposit",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {})
    );

    public static final Event ESCROW_EVENT = new Event(
        "Escrow",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Address>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Uint256>() {},
            new TypeReference<Bool>() {}
        )
    );

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event(
        "OwnershipTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {})
    );

    public static final Event REQUESTDISBURSED_EVENT = new Event(
        "RequestDisbursed",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Bytes32>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Uint256>() {},
            new TypeReference<Uint16>() {}
        )
    );

    public static final Event REQUESTLOCKED_EVENT = new Event(
        "RequestLocked",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Bytes32>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Uint256>() {},
            new TypeReference<Uint16>() {}
        )
    );

    public static final Event REQUESTRELEASED_EVENT = new Event(
        "RequestReleased",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Bytes32>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Uint256>() {}
        )
    );

    public static final Event TRANSFER_EVENT = new Event(
        "Transfer",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Address>(true) {},
            new TypeReference<Address>() {},
            new TypeReference<Address>(true) {},
            new TypeReference<Uint256>() {}
        )
    );

    public static final Event WITHDRAW_EVENT = new Event(
        "Withdraw",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {})
    );

    @Deprecated
    protected Wallet(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Wallet(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Wallet(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Wallet(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<BigInteger> allowance(String param0, String param1) {
        final Function function = new Function(
            FUNC_ALLOWANCE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0), new org.web3j.abi.datatypes.Address(160, param1)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> approve(String spender, String token, BigInteger amount) {
        final Function function = new Function(
            FUNC_APPROVE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, spender),
                new org.web3j.abi.datatypes.Address(160, token),
                new org.web3j.abi.datatypes.generated.Uint256(amount)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> disburseForFulfillment(byte[] requestId, List<Payment> payments) {
        final Function function = new Function(
            FUNC_DISBURSEFORFULFILLMENT,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Bytes32(requestId),
                new org.web3j.abi.datatypes.DynamicArray<Payment>(Payment.class, payments)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> disburseForRequest(byte[] requestId, String to, BigInteger amount) {
        final Function function = new Function(
            FUNC_DISBURSEFORREQUEST,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Bytes32(requestId),
                new org.web3j.abi.datatypes.Address(160, to),
                new org.web3j.abi.datatypes.generated.Uint256(amount)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> isLocked(String spender, String token) {
        final Function function = new Function(
            FUNC_ISLOCKED,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, spender), new org.web3j.abi.datatypes.Address(160, token)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {})
        );
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> lockEscrow(String spender, String token, BigInteger amount) {
        final Function function = new Function(
            FUNC_LOCKESCROW,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, spender),
                new org.web3j.abi.datatypes.Address(160, token),
                new org.web3j.abi.datatypes.generated.Uint256(amount)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> lockForRequest(
        String spender,
        String token,
        BigInteger totalAmount,
        byte[] requestId,
        BigInteger redundancy
    ) {
        final Function function = new Function(
            FUNC_LOCKFORREQUEST,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, spender),
                new org.web3j.abi.datatypes.Address(160, token),
                new org.web3j.abi.datatypes.generated.Uint256(totalAmount),
                new org.web3j.abi.datatypes.generated.Bytes32(requestId),
                new org.web3j.abi.datatypes.generated.Uint16(redundancy)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> lockedOf(String spender, String token) {
        final Function function = new Function(
            FUNC_LOCKEDOF,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, spender), new org.web3j.abi.datatypes.Address(160, token)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> lockedOfRequest(byte[] requestId) {
        final Function function = new Function(
            FUNC_LOCKEDOFREQUEST,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(requestId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(
            FUNC_OWNER,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {})
        );
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> paidCountOfRequest(byte[] requestId) {
        final Function function = new Function(
            FUNC_PAIDCOUNTOFREQUEST,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(requestId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint16>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> releaseEscrow(String spender, String token, BigInteger amount) {
        final Function function = new Function(
            FUNC_RELEASEESCROW,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, spender),
                new org.web3j.abi.datatypes.Address(160, token),
                new org.web3j.abi.datatypes.generated.Uint256(amount)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> releaseForRequest(byte[] requestId) {
        final Function function = new Function(
            FUNC_RELEASEFORREQUEST,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(requestId)),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(FUNC_RENOUNCEOWNERSHIP, Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> totalLockedFor(String token) {
        final Function function = new Function(
            FUNC_TOTALLOCKEDFOR,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferByRouter(String spender, List<Payment> payments) {
        final Function function = new Function(
            FUNC_TRANSFERBYROUTER,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(160, spender),
                new org.web3j.abi.datatypes.DynamicArray<Payment>(Payment.class, payments)
            ),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
            FUNC_TRANSFEROWNERSHIP,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
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

    public RemoteFunctionCall<TransactionReceipt> withdraw(String token, BigInteger amount) {
        final Function function = new Function(
            FUNC_WITHDRAW,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token), new org.web3j.abi.datatypes.generated.Uint256(amount)),
            Collections.<TypeReference<?>>emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public static List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.spender = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.token = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ApprovalEventResponse getApprovalEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVAL_EVENT, log);
        ApprovalEventResponse typedResponse = new ApprovalEventResponse();
        typedResponse.log = log;
        typedResponse.spender = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.token = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApprovalEventFromLog(log));
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    public static List<DepositEventResponse> getDepositEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEPOSIT_EVENT, transactionReceipt);
        ArrayList<DepositEventResponse> responses = new ArrayList<DepositEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DepositEventResponse typedResponse = new DepositEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.token = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DepositEventResponse getDepositEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEPOSIT_EVENT, log);
        DepositEventResponse typedResponse = new DepositEventResponse();
        typedResponse.log = log;
        typedResponse.token = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<DepositEventResponse> depositEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDepositEventFromLog(log));
    }

    public Flowable<DepositEventResponse> depositEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEPOSIT_EVENT));
        return depositEventFlowable(filter);
    }

    public static List<EscrowEventResponse> getEscrowEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ESCROW_EVENT, transactionReceipt);
        ArrayList<EscrowEventResponse> responses = new ArrayList<EscrowEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            EscrowEventResponse typedResponse = new EscrowEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.spender = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.token = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.locked = (Boolean) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static EscrowEventResponse getEscrowEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ESCROW_EVENT, log);
        EscrowEventResponse typedResponse = new EscrowEventResponse();
        typedResponse.log = log;
        typedResponse.spender = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.token = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.locked = (Boolean) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<EscrowEventResponse> escrowEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getEscrowEventFromLog(log));
    }

    public Flowable<EscrowEventResponse> escrowEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ESCROW_EVENT));
        return escrowEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
        DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock
    ) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static List<RequestDisbursedEventResponse> getRequestDisbursedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(REQUESTDISBURSED_EVENT, transactionReceipt);
        ArrayList<RequestDisbursedEventResponse> responses = new ArrayList<RequestDisbursedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestDisbursedEventResponse typedResponse = new RequestDisbursedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.token = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.paidCount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static RequestDisbursedEventResponse getRequestDisbursedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(REQUESTDISBURSED_EVENT, log);
        RequestDisbursedEventResponse typedResponse = new RequestDisbursedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.token = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.paidCount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<RequestDisbursedEventResponse> requestDisbursedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getRequestDisbursedEventFromLog(log));
    }

    public Flowable<RequestDisbursedEventResponse> requestDisbursedEventFlowable(
        DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock
    ) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTDISBURSED_EVENT));
        return requestDisbursedEventFlowable(filter);
    }

    public static List<RequestLockedEventResponse> getRequestLockedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(REQUESTLOCKED_EVENT, transactionReceipt);
        ArrayList<RequestLockedEventResponse> responses = new ArrayList<RequestLockedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestLockedEventResponse typedResponse = new RequestLockedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.token = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.totalAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.redundancy = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static RequestLockedEventResponse getRequestLockedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(REQUESTLOCKED_EVENT, log);
        RequestLockedEventResponse typedResponse = new RequestLockedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.token = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.totalAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.redundancy = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<RequestLockedEventResponse> requestLockedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getRequestLockedEventFromLog(log));
    }

    public Flowable<RequestLockedEventResponse> requestLockedEventFlowable(
        DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock
    ) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTLOCKED_EVENT));
        return requestLockedEventFlowable(filter);
    }

    public static List<RequestReleasedEventResponse> getRequestReleasedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(REQUESTRELEASED_EVENT, transactionReceipt);
        ArrayList<RequestReleasedEventResponse> responses = new ArrayList<RequestReleasedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestReleasedEventResponse typedResponse = new RequestReleasedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.token = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.amountRefunded = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static RequestReleasedEventResponse getRequestReleasedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(REQUESTRELEASED_EVENT, log);
        RequestReleasedEventResponse typedResponse = new RequestReleasedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.token = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.amountRefunded = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<RequestReleasedEventResponse> requestReleasedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getRequestReleasedEventFromLog(log));
    }

    public Flowable<RequestReleasedEventResponse> requestReleasedEventFlowable(
        DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock
    ) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTRELEASED_EVENT));
        return requestReleasedEventFlowable(filter);
    }

    public static List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.spender = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.token = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.spender = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.token = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public static List<WithdrawEventResponse> getWithdrawEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(WITHDRAW_EVENT, transactionReceipt);
        ArrayList<WithdrawEventResponse> responses = new ArrayList<WithdrawEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WithdrawEventResponse typedResponse = new WithdrawEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.token = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static WithdrawEventResponse getWithdrawEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(WITHDRAW_EVENT, log);
        WithdrawEventResponse typedResponse = new WithdrawEventResponse();
        typedResponse.log = log;
        typedResponse.token = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<WithdrawEventResponse> withdrawEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getWithdrawEventFromLog(log));
    }

    public Flowable<WithdrawEventResponse> withdrawEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WITHDRAW_EVENT));
        return withdrawEventFlowable(filter);
    }

    @Deprecated
    public static Wallet load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Wallet(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Wallet load(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit
    ) {
        return new Wallet(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Wallet load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Wallet(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Wallet load(
        String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider
    ) {
        return new Wallet(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Wallet> deploy(
        Web3j web3j,
        Credentials credentials,
        ContractGasProvider contractGasProvider,
        String router,
        String initialOwner
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, router), new org.web3j.abi.datatypes.Address(160, initialOwner))
        );
        return deployRemoteCall(Wallet.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<Wallet> deploy(
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider,
        String router,
        String initialOwner
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, router), new org.web3j.abi.datatypes.Address(160, initialOwner))
        );
        return deployRemoteCall(Wallet.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Wallet> deploy(
        Web3j web3j,
        Credentials credentials,
        BigInteger gasPrice,
        BigInteger gasLimit,
        String router,
        String initialOwner
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, router), new org.web3j.abi.datatypes.Address(160, initialOwner))
        );
        return deployRemoteCall(Wallet.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Wallet> deploy(
        Web3j web3j,
        TransactionManager transactionManager,
        BigInteger gasPrice,
        BigInteger gasLimit,
        String router,
        String initialOwner
    ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, router), new org.web3j.abi.datatypes.Address(160, initialOwner))
        );
        return deployRemoteCall(Wallet.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class Payment extends StaticStruct {

        public String recipient;

        public String feeToken;

        public BigInteger feeAmount;

        public Payment(String recipient, String feeToken, BigInteger feeAmount) {
            super(
                new org.web3j.abi.datatypes.Address(160, recipient),
                new org.web3j.abi.datatypes.Address(160, feeToken),
                new org.web3j.abi.datatypes.generated.Uint256(feeAmount)
            );
            this.recipient = recipient;
            this.feeToken = feeToken;
            this.feeAmount = feeAmount;
        }

        public Payment(Address recipient, Address feeToken, Uint256 feeAmount) {
            super(recipient, feeToken, feeAmount);
            this.recipient = recipient.getValue();
            this.feeToken = feeToken.getValue();
            this.feeAmount = feeAmount.getValue();
        }
    }

    public static class ApprovalEventResponse extends BaseEventResponse {

        public String spender;

        public String token;

        public BigInteger amount;
    }

    public static class DepositEventResponse extends BaseEventResponse {

        public String token;

        public BigInteger amount;
    }

    public static class EscrowEventResponse extends BaseEventResponse {

        public String spender;

        public String token;

        public BigInteger amount;

        public Boolean locked;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {

        public String previousOwner;

        public String newOwner;
    }

    public static class RequestDisbursedEventResponse extends BaseEventResponse {

        public byte[] requestId;

        public String to;

        public String token;

        public BigInteger amount;

        public BigInteger paidCount;
    }

    public static class RequestLockedEventResponse extends BaseEventResponse {

        public byte[] requestId;

        public String spender;

        public String token;

        public BigInteger totalAmount;

        public BigInteger redundancy;
    }

    public static class RequestReleasedEventResponse extends BaseEventResponse {

        public byte[] requestId;

        public String spender;

        public String token;

        public BigInteger amountRefunded;
    }

    public static class TransferEventResponse extends BaseEventResponse {

        public String spender;

        public String to;

        public String token;

        public BigInteger amount;
    }

    public static class WithdrawEventResponse extends BaseEventResponse {

        public String token;

        public BigInteger amount;
    }
}
