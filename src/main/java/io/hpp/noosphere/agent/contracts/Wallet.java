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
import org.web3j.abi.datatypes.generated.Bytes4;
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
        "60a0346200012957601f620021a638819003918201601f19168301916001600160401b038311848410176200012d578084926040948552833981010312620001295760206200004e8262000141565b916001600160a01b0391829162000066910162000141565b1691821562000111575f80546001600160a01b03198116851782556040519491908416907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09080a3169081156200010257506080526001805560405161204f90816200015782396080518181816103330152818161067a01528181610abc01528181610c09015281816110130152818161133a01526116560152f35b632530e88560e11b8152600490fd5b604051631e4fbdf760e01b81525f6004820152602490fd5b5f80fd5b634e487b7160e01b5f52604160045260245ffd5b51906001600160a01b0382168203620001295756fe6080806040526004361015610047575b50361561001a575f80fd5b5f7fe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c6020604051348152a2005b5f3560e01c90816311d8668c146115e3575080631626ba7e14611525578063181f5a7714611415578063303d0c6b14611310578063711dc79214610fac578063715018a614610f125780637c72ccf614610eab5780638612d04914610e305780638da5cb5b14610de057806394862f9c14610d72578063a04889e914610b99578063b676899814610a92578063b9b3e06a14610a19578063c3909fa1146109b6578063cb7aa2fa1461062c578063dd62ed3e146105b3578063e1f21c671461053a578063ec8669bd146102e8578063f2fde38b1461020a5763f3fef3a31461012f575f61000f565b346102065760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc360112610206576101666118ee565b60243590610172611e1b565b61017a611b7f565b61018381611bba565b82116101dc57602073ffffffffffffffffffffffffffffffffffffffff7f884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364926101cd853383611c80565b6040519485521692a260018055005b60046040517f356680b7000000000000000000000000000000000000000000000000000000008152fd5b5f80fd5b346102065760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc360112610206576102416118ee565b610249611e1b565b73ffffffffffffffffffffffffffffffffffffffff8091169081156102b8575f54827fffffffffffffffffffffffff00000000000000000000000000000000000000008216175f55167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e05f80a3005b60246040517f1e4fbdf70000000000000000000000000000000000000000000000000000000081525f6004820152fd5b34610206576020807ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126102065773ffffffffffffffffffffffffffffffffffffffff906004357f00000000000000000000000000000000000000000000000000000000000000008316330361051057610362611b7f565b805f526005825260405f20916040519061037b82611934565b84845416825284600185015416938183019485526002810154604084015260ff600460038301549260608601938452015461ffff80821660808701528160101c1660a0860152831c1615801560c08501526104e6575194808351165f526003825260405f20818651165f52825260405f20548087116104bc577f7d370a94e557d98fc60928112c72d25d1a18d792043237cc9e3715666c9e61e193610421888493611a5c565b828251165f526003855260405f20838951165f52855260405f2055818751165f526002845260405f20610455898254611a5c565b9055818151165f526004845260405f20828851165f52845260405f2061047c898254611a96565b9055855f52600584526104a960405f2060045f918281558260018201558260028201558260038201550155565b511694511694604051908152a460018055005b60046040517f39c8bcf9000000000000000000000000000000000000000000000000000000008152fd5b60046040517ff0da79df000000000000000000000000000000000000000000000000000000008152fd5b60046040517fc41a5b09000000000000000000000000000000000000000000000000000000008152fd5b34610206577f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925602061056b366119cb565b6105789492939194611e1b565b73ffffffffffffffffffffffffffffffffffffffff80941693845f526004835260405f20951694855f5282528060405f2055604051908152a3005b346102065760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc360112610206576105ea6118ee565b6105f2611911565b9073ffffffffffffffffffffffffffffffffffffffff8091165f52600460205260405f2091165f52602052602060405f2054604051908152f35b346102065760607ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc36011261020657610663611911565b73ffffffffffffffffffffffffffffffffffffffff7f0000000000000000000000000000000000000000000000000000000000000000163303610510576106a8611b7f565b6004355f52600560205260405f20600481015460ff8160201c16156104e6576044351561098c576003820154604435116109625761ffff8082169160101c1610156109385773ffffffffffffffffffffffffffffffffffffffff8154165f52600360205260405f2073ffffffffffffffffffffffffffffffffffffffff6001830154165f5260205260405f206107416044358254611a5c565b905573ffffffffffffffffffffffffffffffffffffffff6001820154165f52600260205260405f206107766044358254611a5c565b90556107886044356003830154611a5c565b60038201556004810180547fffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000ffff8116601091821c61ffff1660010190911b63ffff00001617905561ffff600482015460101c16906108026044358473ffffffffffffffffffffffffffffffffffffffff600185015416611c80565b600181015460408051604435815261ffff8516602082015273ffffffffffffffffffffffffffffffffffffffff9283169586931691600435917ffab66afb795acabdb9d25b8483330bd32d4ac9b22e83e44ae20cffc94101a33e9190a4600381015490811580159384610926575b5061087c575b60018055005b5473ffffffffffffffffffffffffffffffffffffffff16916108fe575b6004355f5260056020526108c760405f2060045f918281558260018201558260028201558260038201550155565b6040519081527f7d370a94e557d98fc60928112c72d25d1a18d792043237cc9e3715666c9e61e1602060043592a480808080610876565b815f52600460205260405f20835f5260205260405f2061091f828254611a96565b9055610899565b905061ffff6004830154161485610870565b60046040517f3a082e21000000000000000000000000000000000000000000000000000000008152fd5b60046040517f5d4b1077000000000000000000000000000000000000000000000000000000008152fd5b60046040517f1f2a2005000000000000000000000000000000000000000000000000000000008152fd5b346102065760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126102065773ffffffffffffffffffffffffffffffffffffffff610a026118ee565b165f526002602052602060405f2054604051908152f35b346102065760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc36011261020657610a506118ee565b610a58611911565b9073ffffffffffffffffffffffffffffffffffffffff8091165f52600360205260405f2091165f52602052602060405f2054604051908152f35b3461020657610aa0366119cb565b73ffffffffffffffffffffffffffffffffffffffff92919291827f0000000000000000000000000000000000000000000000000000000000000000163303610510578290610aec611b7f565b1691825f526020906003825260405f20941693845f52815260405f205482116101dc577f813582499997f00ba0142c7813740a6e381df71a63d11d8c8f208f66b7795d26915f604092858252600381528382208783528152838220610b52848254611a5c565b905586825260028152838220610b69848254611a5c565b9055858252600481528382208783528152838220610b88848254611a96565b90558351928352820152a360018055005b346102065760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc36011261020657610bd06118ee565b60243567ffffffffffffffff811161020657610bf0903690600401611a2b565b73ffffffffffffffffffffffffffffffffffffffff90817f000000000000000000000000000000000000000000000000000000000000000016330361051057610c37611b7f565b5f5b818110610c465760018055005b610c51818386611b21565b9060408201359182610c69575b506001915001610c39565b848716805f526020936004855260405f20908584019188610c8984611b5e565b165f52865260405f205491818310610d4857610d18610d128a92600199610cd1867fd1398bee19313d6bf672ccb116e51f4a1a947e91c757907f51fbb5b5e56c698f98611a5c565b90885f526004815260405f209086610ce885611b5e565b165f525260405f2055610d0d85610cfe83611b5e565b610d078b611b5e565b90611c80565b611b5e565b95611b5e565b6040805173ffffffffffffffffffffffffffffffffffffffff97909716875260208701939093521693a386610c5e565b60046040517f13be252b000000000000000000000000000000000000000000000000000000008152fd5b346102065760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc360112610206576004355f526005602052600460405f20015460ff8160201c165f14610dd75761ffff60209160101c165b61ffff60405191168152f35b5060205f610dcb565b34610206575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc36011261020657602073ffffffffffffffffffffffffffffffffffffffff5f5416604051908152f35b346102065760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc36011261020657610e676118ee565b610e6f611911565b9073ffffffffffffffffffffffffffffffffffffffff8091165f52600360205260405f2091165f52602052602060405f20541515604051908152f35b346102065760207ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc360112610206576004355f52600560205260405f2060ff600482015460201c165f14610f0957600360209101545b604051908152f35b5060205f610f01565b34610206575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc36011261020657610f48611e1b565b5f73ffffffffffffffffffffffffffffffffffffffff81547fffffffffffffffffffffffff000000000000000000000000000000000000000081168355167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a3005b346102065760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126102065760243567ffffffffffffffff811161020657610ffb903690600401611a2b565b9073ffffffffffffffffffffffffffffffffffffffff7f000000000000000000000000000000000000000000000000000000000000000016330361051057611041611b7f565b6004355f52600560205260405f2060048101549260ff8460201c16156104e65761ffff841661ffff8560101c1610156109385773ffffffffffffffffffffffffffffffffffffffff6001830154169373ffffffffffffffffffffffffffffffffffffffff835416936003840154925f935f5b8281106112a2575080851161096257865f52600360205260405f20885f5260205260405f20548086116104bc5785611173926110f460049861112694611a5c565b8a5f52600360205260405f208c5f5260205260405f2055600260205260405f2061111f838254611a5c565b9055611a5c565b9561ffff6001818760101c16011695818860038994015501907fffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000ffff63ffff000083549260101b169116179055565b5f5b8181106112205750505061ffff161461118e5760018055005b806111f8575b6004355f5260056020526111c260405f2060045f918281558260018201558260028201558260038201550155565b6040519081527f7d370a94e557d98fc60928112c72d25d1a18d792043237cc9e3715666c9e61e1602060043592a4808080610876565b815f52600460205260405f20835f5260205260405f20611219828254611a96565b9055611194565b808861122f6001938587611b21565b73ffffffffffffffffffffffffffffffffffffffff61126161125083611b5e565b92610d0d6040820135809587611c80565b6040805193845261ffff8b166020850152911691600435917ffab66afb795acabdb9d25b8483330bd32d4ac9b22e83e44ae20cffc94101a33e91a401611175565b946112ae868486611b21565b908973ffffffffffffffffffffffffffffffffffffffff6112d160208501611b5e565b16036112e657604060019201350195016110b3565b60046040517f4bc15dc1000000000000000000000000000000000000000000000000000000008152fd5b346102065761131e366119cb565b73ffffffffffffffffffffffffffffffffffffffff92919291827f000000000000000000000000000000000000000000000000000000000000000016330361051057611368611b7f565b61137184611bba565b82116101dc57821691825f526020906004825260405f20941693845f5281528160405f205410610d48577f813582499997f00ba0142c7813740a6e381df71a63d11d8c8f208f66b7795d26916001604092855f5260048152835f20875f528152835f206113df848254611a5c565b9055855f5260038152835f20875f528152835f206113fe848254611a96565b9055865f5260028152835f20610b88848254611a96565b34610206575f7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc360112610206576040516040810181811067ffffffffffffffff8211176114f857604052600c81526020907f57616c6c657420312e302e30000000000000000000000000000000000000000060208201526040518092602082528251928360208401525f5b8481106114e1575050507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0601f835f604080968601015201168101030190f35b8181018301518682016040015285935082016114a1565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b346102065760407ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126102065760243567ffffffffffffffff811161020657366023820112156102065780600401359061158082611991565b61158d6040519182611950565b8281523660248484010111610206575f602084819560246115b996018386013783010152600435611aa3565b7fffffffff0000000000000000000000000000000000000000000000000000000060405191168152f35b346102065760a07ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc3601126102065761161a6118ee565b611622611911565b91604435606435916084359061ffff94858316918284036102065773ffffffffffffffffffffffffffffffffffffffff90817f00000000000000000000000000000000000000000000000000000000000000001633036118c65750611685611b7f565b855f526020916005835260ff600460405f200154841c1661189c576116a989611bba565b86116101dc57811696875f52600483528160405f20991698895f5283528560405f205410610d4857875f526004835260405f20895f52835260405f20868154906116f291611a5c565b9055875f526003835260405f20895f52835260405f208681549061171591611a96565b9055885f526002835260405f208681549061172f91611a96565b905560405161173d81611934565b88815281848201918b83526040810189815260608201908a82526080830198895260a08301945f865260c0840197600189528d5f5260058a528060405f20955116907fffffffffffffffffffffffff00000000000000000000000000000000000000009182875416178655600186019251169082541617905551600283015551600382015560040195511685547fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff000016178555511661182b9084907fffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000ffff63ffff000083549260101b169116179055565b5182547fffffffffffffffffffffffffffffffffffffffffffffffffffffff00ffffffff1664ff0000000091151590921b161790556040805192835261ffff9190911660208301527f21a0cbcf4ecd8cd3ff869836abc07ebc6ecf06048838bc149520ad654264f03391a460018055005b60046040517f85112af6000000000000000000000000000000000000000000000000000000008152fd5b807fc41a5b090000000000000000000000000000000000000000000000000000000060049252fd5b6004359073ffffffffffffffffffffffffffffffffffffffff8216820361020657565b6024359073ffffffffffffffffffffffffffffffffffffffff8216820361020657565b60e0810190811067ffffffffffffffff8211176114f857604052565b90601f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0910116810190811067ffffffffffffffff8211176114f857604052565b67ffffffffffffffff81116114f857601f017fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe01660200190565b7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc60609101126102065773ffffffffffffffffffffffffffffffffffffffff90600435828116810361020657916024359081168103610206579060443590565b9181601f840112156102065782359167ffffffffffffffff8311610206576020808501946060850201011161020657565b91908203918211611a6957565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b91908201809211611a6957565b611ab991611ab091611e6b565b90929192611ea5565b73ffffffffffffffffffffffffffffffffffffffff805f5416911614611afd577fffffffff0000000000000000000000000000000000000000000000000000000090565b7f1626ba7e0000000000000000000000000000000000000000000000000000000090565b9190811015611b31576060020190565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52603260045260245ffd5b3573ffffffffffffffffffffffffffffffffffffffff811681036102065790565b600260015414611b90576002600155565b60046040517f3ee5aeb5000000000000000000000000000000000000000000000000000000008152fd5b73ffffffffffffffffffffffffffffffffffffffff16805f52600260205260405f205481155f14611bf357611bf0915047611a5c565b90565b6020602492604051938480927f70a082310000000000000000000000000000000000000000000000000000000082523060048301525afa8015611c75575f90611c41575b611bf09250611a5c565b506020823d602011611c6d575b81611c5b60209383611950565b8101031261020657611bf09151611c37565b3d9150611c4e565b6040513d5f823e3d90fd5b73ffffffffffffffffffffffffffffffffffffffff9190821680611d5a5750824710611d22575f809381938293165af13d15611d1a573d90611cc182611991565b91611ccf6040519384611950565b82523d5f602084013e5b15611ce15750565b805115611cf057602081519101fd5b60046040517fd6bda275000000000000000000000000000000000000000000000000000000008152fd5b606090611cd9565b6044834790604051917fcf47918100000000000000000000000000000000000000000000000000000000835260048301526024820152fd5b6040517fa9059cbb00000000000000000000000000000000000000000000000000000000602080830191825273ffffffffffffffffffffffffffffffffffffffff94909416602483015260448083019690965294815290939192505f9190611dc3606482611950565b519082855af115611c75575f513d611e125750803b155b611de15750565b602490604051907f5274afe70000000000000000000000000000000000000000000000000000000082526004820152fd5b60011415611dda565b73ffffffffffffffffffffffffffffffffffffffff5f54163303611e3b57565b60246040517f118cdaa7000000000000000000000000000000000000000000000000000000008152336004820152fd5b8151919060418303611e9b57611e949250602082015190606060408401519301515f1a90611f8a565b9192909190565b50505f9160029190565b6004811015611f5d5780611eb7575050565b60018103611ee95760046040517ff645eedf000000000000000000000000000000000000000000000000000000008152fd5b60028103611f2257602482604051907ffce698f70000000000000000000000000000000000000000000000000000000082526004820152fd5b600314611f2c5750565b602490604051907fd78bce0c0000000000000000000000000000000000000000000000000000000082526004820152fd5b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602160045260245ffd5b91907f7fffffffffffffffffffffffffffffff5d576e7357a4501ddfe92f46681b20a0841161200e579160209360809260ff5f9560405194855216868401526040830152606082015282805260015afa15611c75575f5173ffffffffffffffffffffffffffffffffffffffff81161561200457905f905f90565b505f906001905f90565b5050505f916003919056fea26469706673582212203f7a54711bc7084b0d6fb06a05ba03ed7a2d5a58620e2be52d159bef18f3caf364736f6c63430008170033";

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_DISBURSEFORFULFILLMENT = "disburseForFulfillment";

    public static final String FUNC_DISBURSEFORREQUEST = "disburseForRequest";

    public static final String FUNC_ISLOCKED = "isLocked";

    public static final String FUNC_ISVALIDSIGNATURE = "isValidSignature";

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

    public RemoteFunctionCall<byte[]> isValidSignature(byte[] hash_, byte[] signature_) {
        final Function function = new Function(
            FUNC_ISVALIDSIGNATURE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(hash_), new org.web3j.abi.datatypes.DynamicBytes(signature_)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes4>() {})
        );
        return executeRemoteCallSingleValueReturn(function, byte[].class);
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
