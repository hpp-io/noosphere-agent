package io.hpp.noosphere.agent.service.blockchain.dto;

import java.math.BigInteger;
import org.web3j.utils.Numeric;

/**
 * Represents the parameters of an EIP-712 signature for Coordinator functions.
 * This is an immutable data carrier, equivalent to a Python dataclass.
 *
 * @param nonce  The nonce of the signature, used to prevent replay attacks.
 * @param expiry The Unix timestamp after which the signature is no longer valid.
 * @param v      The recovery ID of the signature.
 * @param r      The r-value of the signature's elliptic curve point.
 * @param s      The s-value of the signature's elliptic curve point.
 */
public record SignatureParamsDTO(int nonce, long expiry, int v, BigInteger r, BigInteger s) {
    /**
     * Concatenates r, s, and v to form a single byte array signature,
     * which is the format expected by the smart contract.
     *
     * @return The concatenated signature as a byte array (65 bytes).
     */
    public byte[] signature() {
        byte[] rBytes = Numeric.toBytesPadded(r, 32);
        byte[] sBytes = Numeric.toBytesPadded(s, 32);
        byte[] vByte = new byte[] { (byte) v };

        byte[] result = new byte[65];
        System.arraycopy(rBytes, 0, result, 0, 32);
        System.arraycopy(sBytes, 0, result, 32, 32);
        System.arraycopy(vByte, 0, result, 64, 1);
        return result;
    }
}
