package io.hpp.noosphere.agent.service.blockchain;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.security.KeystoreManager;
import java.nio.file.Path;
import java.security.KeyStore;
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
            this.keyStore = KeystoreManager.loadKeyStore(
                Path.of(applicationProperties.getNoosphere().getChain().getWallet().getKeystore().getPath()),
                applicationProperties.getNoosphere().getChain().getWallet().getKeystore().getPassword()
            );
        } catch (Exception e) {
            LOG.error("Failed to load the keystore. This is a fatal error for KeystoreService.", e);
            throw new IllegalStateException("Could not initialize KeystoreService", e);
        }
    }

    private String getKeystorePassword() {
        return applicationProperties.getNoosphere().getChain().getWallet().getKeystore().getPassword();
    }

    public String getSecretKey(String keyAlias) {
        try {
            return KeystoreManager.readSecretKeyAsUtf8String(this.keyStore, getKeystorePassword(), keyAlias);
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
            return KeystoreManager.readEthKeyFromSecret(this.keyStore, getKeystorePassword(), keyAlias);
        } catch (Exception e) {
            LOG.error("Failed to getCredentials " + keyAlias, e);
        }
        return null;
    }
}
