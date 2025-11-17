package io.hpp.noosphere.agent.service.blockchain;

import static io.hpp.noosphere.agent.config.Constants.KEYSTORE_TYPE;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.service.util.CommonUtil;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;

@Service
public class KeystoreService {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoreService.class);

    private final ApplicationProperties applicationProperties;
    private KeyStore keyStore;

    public KeystoreService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        try {
            this.keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            try (
                InputStream fis = Files.newInputStream(
                    Path.of(applicationProperties.getNoosphere().getChain().getWallet().getKeystore().getPath())
                )
            ) {
                keyStore.load(fis, applicationProperties.getNoosphere().getChain().getWallet().getKeystore().getPassword().toCharArray());
            }
        } catch (Exception e) {
            LOG.error("Failed to load the keystore. This is a fatal error for KeystoreService.", e);
            throw new IllegalStateException("Could not initialize KeystoreService", e);
        }
    }

    private String getKeyPassword(String keyAlias) {
        return applicationProperties.getNoosphere().getChain().getWallet().getKeystore().getPassword();
    }

    public String getSecretKey(String keyAlias) {
        try {
            String keyPassword = getKeyPassword(keyAlias);
            if (CommonUtil.isValid(keyPassword) && keyStore != null) {
                KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(keyPassword.toCharArray());

                KeyStore.Entry entry = keyStore.getEntry(keyAlias, entryPassword);

                if (!(entry instanceof KeyStore.SecretKeyEntry)) {
                    LOG.debug("Error: Entry with alias '" + keyAlias + "' is not a SecretKeyEntry.");
                    return null;
                }

                KeyStore.SecretKeyEntry skEntry = (KeyStore.SecretKeyEntry) entry;
                SecretKey secretKey = skEntry.getSecretKey();

                byte[] keyBytes = secretKey.getEncoded();
                return new String(Base64.getDecoder().decode(keyBytes), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOG.error("Failed to getSecretKey " + keyAlias, e);
        }
        return null;
    }

    /**
     * Retrieves the private key for a given alias and creates Web3j Credentials.
     *
     * @param keyAlias The alias of the key in the keystore.
     * @return The {@link Credentials} object for the given key.
     * @throws IllegalStateException if the key is not found or is not valid.
     */
    @NonNull
    public Credentials getCredentials(String keyAlias) {
        try {
            if (keyAlias == null || keyAlias.trim().isEmpty()) {
                throw new IllegalArgumentException("Key alias cannot be null or empty");
            }

            // Check if the alias exists in the keystore
            if (!keyStore.containsAlias(keyAlias)) {
                throw new IllegalStateException("Key alias '" + keyAlias + "' not found in keystore");
            }

            String privateKey = this.getSecretKey(keyAlias);
            if (privateKey == null || privateKey.trim().isEmpty()) {
                throw new IllegalStateException("Failed to retrieve private key for alias: " + keyAlias);
            }

            // Validate private key format (should be 64 hex characters or start with 0x)
            String cleanKey = privateKey.trim();
            if (cleanKey.startsWith("0x")) {
                cleanKey = cleanKey.substring(2);
            }

            if (cleanKey.length() != 64) {
                throw new IllegalStateException(
                    "Invalid private key format for alias: " + keyAlias + ". Expected 64 hex characters, got: " + cleanKey.length()
                );
            }

            return Credentials.create(privateKey);
        } catch (Exception e) {
            LOG.error("Failed to create credentials for alias: {}", keyAlias, e);
            throw new RuntimeException("Failed to load credentials from keystore for alias: " + keyAlias, e);
        }
    }
}
