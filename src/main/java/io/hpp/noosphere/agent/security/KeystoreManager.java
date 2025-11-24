package io.hpp.noosphere.agent.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.exception.CipherException;
import org.web3j.utils.Numeric;

public class KeystoreManager {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoreManager.class);
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEY_ALGORITHM_AES = "AES";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void createKeyStore(Path path, String password) throws GeneralSecurityException, IOException {
        try {
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(null, password.toCharArray());
            saveKeyStore(ks, path, password);
            LOG.debug("Created new empty keystore at: {}", path);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException("Failed to create keystore", e);
        }
    }

    public static void createKeyStoreWithUtf8String(Path path, String password, String alias, String utf8Value)
        throws GeneralSecurityException, IOException {
        try {
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(null, password.toCharArray());
            addSecretKeyWithUtf8String(ks, password, alias, utf8Value);
            saveKeyStore(ks, path, password);
            LOG.debug("Created new keystore with secret key at: {}", path);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException("Failed to create keystore", e);
        }
    }

    public static void createKeyStoreWithEthWallet(Path path, String password, String alias, String v3Json, String v3Password)
        throws GeneralSecurityException, IOException, CipherException {
        try {
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(null, password.toCharArray());
            addEthWalletV3(ks, password, alias, v3Json, v3Password);
            saveKeyStore(ks, path, password);
            LOG.debug("Created new keystore with ETH wallet at: {}", path);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException("Failed to create keystore", e);
        }
    }

    public static void createKeyStoreWithHexPrivateKey(Path path, String password, String alias, String hexPrivateKey)
        throws GeneralSecurityException, IOException {
        try {
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(null, password.toCharArray());

            addEthWalletV3WithHexPrivateKey(ks, password, alias, hexPrivateKey);

            saveKeyStore(ks, path, password);
            LOG.debug("Created new keystore with private key at: {}", path);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException("Failed to create keystore", e);
        }
    }

    public static void addSecretKey(KeyStore ks, String password, String alias, SecretKey secretKey) throws GeneralSecurityException {
        KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(password.toCharArray());
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
        ks.setEntry(alias, entry, protection);
        LOG.debug("Added symmetric key with alias '{}' to keystore.", alias);
    }

    public static void addSecretKeyWithUtf8String(KeyStore ks, String password, String alias, String utf8Value)
        throws GeneralSecurityException {
        byte[] keyBytes = utf8Value.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM_AES);
        addSecretKey(ks, password, alias, secretKey);
    }

    public static String readSecretKeyAsUtf8StringFromBase64dKeystore(String base64Keystore, String password, String alias)
        throws GeneralSecurityException, IOException {
        try {
            KeyStore ks = loadKeyStoreFromBase64String(base64Keystore, password);
            return readSecretKeyAsUtf8String(ks, password, alias);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException("Failed to read secret key", e);
        }
    }

    public static String readSecretKeyAsUtf8String(Path path, String password, String alias) throws GeneralSecurityException, IOException {
        try {
            KeyStore ks = loadKeyStore(path, password);
            return readSecretKeyAsUtf8String(ks, password, alias);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException("Failed to read secret key", e);
        }
    }

    public static String readSecretKeyAsUtf8String(InputStream is, String password, String alias)
        throws GeneralSecurityException, IOException {
        try {
            KeyStore ks = loadKeyStore(is, password);
            return readSecretKeyAsUtf8String(ks, password, alias);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException("Failed to read secret key", e);
        }
    }

    public static String readSecretKeyAsUtf8String(KeyStore ks, String password, String alias) throws GeneralSecurityException {
        try {
            KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(password.toCharArray());

            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(alias, protection);
            if (entry == null) {
                throw new KeyStoreException("Alias '" + alias + "' not found in keystore.");
            }
            SecretKey secretKey = entry.getSecretKey();
            byte[] keyBytes = secretKey.getEncoded();
            return new String(keyBytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            throw new GeneralSecurityException("Failed to read secret key", e);
        }
    }

    public static void addEthWalletV3(KeyStore ks, String password, String alias, String v3Json, String v3Password)
        throws GeneralSecurityException, IOException, CipherException {
        WalletFile walletFile = objectMapper.readValue(v3Json, WalletFile.class);
        ECKeyPair ecKeyPair = Wallet.decrypt(v3Password, walletFile);

        KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(password.toCharArray());
        storeEthKeyAsSecret(ks, alias, ecKeyPair, protection);
        LOG.debug("Added ETH wallet with alias '{}' to keystore.", alias);
    }

    public static void addEthWalletV3FromBytes(KeyStore ks, String password, String alias, byte[] privateKeyBytes)
        throws GeneralSecurityException {
        BigInteger privateKey = new BigInteger(1, privateKeyBytes);
        addEthWalletV3FromBigInteger(ks, password, alias, privateKey);
        LOG.debug("Added ETH wallet with alias '{}' to keystore.", alias);
    }

    public static void addEthWalletV3FromBigInteger(KeyStore ks, String password, String alias, BigInteger privateKey)
        throws GeneralSecurityException {
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
        KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(password.toCharArray());
        storeEthKeyAsSecret(ks, alias, ecKeyPair, protection);
        LOG.debug("Added ETH wallet with alias '{}' to keystore.", alias);
    }

    public static void addEthWalletV3WithHexPrivateKey(KeyStore ks, String password, String alias, String hexPrivateKey)
        throws GeneralSecurityException {
        if (hexPrivateKey != null) {
            if (hexPrivateKey.startsWith("0x")) {
                hexPrivateKey = hexPrivateKey.substring(2);
            }

            BigInteger privateKey = new BigInteger(hexPrivateKey, 16);
            addEthWalletV3FromBigInteger(ks, password, alias, privateKey);

            LOG.debug("Added ETH wallet with alias '{}' to keystore.", alias);
        } else {
            LOG.error("Failed to add ETH wallet with alias '{}' to keystore because private key is null.", alias);
        }
    }

    public static KeyStore loadKeyStore(Path path, String password) throws IOException, GeneralSecurityException {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        try (InputStream fis = Files.newInputStream(path)) {
            ks.load(fis, password.toCharArray());
        } catch (NoSuchAlgorithmException e) {
            throw new GeneralSecurityException(e);
        }
        return ks;
    }

    private static KeyStore loadKeyStore(InputStream is, String password) throws IOException, GeneralSecurityException {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        ks.load(is, password.toCharArray());
        return ks;
    }

    private static KeyStore loadKeyStoreFromBase64String(String base64Encoded, String password)
        throws IOException, GeneralSecurityException {
        byte[] decodedFile = Base64.getDecoder().decode(base64Encoded);
        InputStream is = new ByteArrayInputStream(decodedFile);
        return loadKeyStore(is, password);
    }

    private static KeyStore loadKeyStoreFromUtf8EncodedString(String utf8Encoded, String password)
        throws IOException, GeneralSecurityException {
        byte[] decodedFile = utf8Encoded.getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(decodedFile);
        return loadKeyStore(is, password);
    }

    public static void saveKeyStore(KeyStore ks, Path path, String password) throws IOException, GeneralSecurityException {
        try (OutputStream os = Files.newOutputStream(path)) {
            ks.store(os, password.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException(e);
        }
    }

    private static void storeEthKeyAsSecret(KeyStore ks, String alias, ECKeyPair ecKeyPair, KeyStore.ProtectionParameter protection)
        throws KeyStoreException {
        BigInteger privateKey = ecKeyPair.getPrivateKey();
        byte[] privateKeyBytes = Numeric.toBytesPadded(privateKey, 32);
        SecretKeySpec keySpec = new SecretKeySpec(privateKeyBytes, KEY_ALGORITHM_AES);
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(keySpec);
        ks.setEntry(alias, entry, protection);
    }

    public static Credentials readEthKeyFromSecretFromBase64Keystore(String base64Keystore, String password, String alias)
        throws GeneralSecurityException, IOException {
        try {
            KeyStore ks = loadKeyStoreFromBase64String(base64Keystore, password);
            return readEthKeyFromSecret(ks, password, alias);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException e) {
            throw new GeneralSecurityException("Failed to load ETH key", e);
        }
    }

    public static Credentials readEthKeyFromSecret(Path path, String password, String alias) throws GeneralSecurityException, IOException {
        try {
            KeyStore ks = loadKeyStore(path, password);
            return readEthKeyFromSecret(ks, password, alias);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException e) {
            throw new GeneralSecurityException("Failed to load ETH key", e);
        }
    }

    public static Credentials readEthKeyFromSecret(KeyStore ks, String password, String alias) throws GeneralSecurityException {
        try {
            KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(password.toCharArray());
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(alias, protection);
            SecretKey secretKey = entry.getSecretKey();
            byte[] privateKeyBytes = secretKey.getEncoded();
            BigInteger privateKey = Numeric.toBigInt(privateKeyBytes);
            ECKeyPair keyPair = ECKeyPair.create(privateKey);
            return Credentials.create(keyPair);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new GeneralSecurityException("Failed to load ETH key", e);
        }
    }

    public static String readPrivateKeyAsHexString(Credentials credentials) {
        BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
        return Numeric.toHexStringWithPrefixZeroPadded(privateKey, 64);
    }
}
