package io.hpp.noosphere.agent.config;

import io.hpp.noosphere.agent.service.NoosphereConfigService;
import io.hpp.noosphere.agent.service.blockchain.KeystoreService;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

@Configuration
public class Web3jConfig {

    private final NoosphereConfigService noosphereConfigService;

    // 기본 timeout 값들 정의
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000; // 30초
    private static final int DEFAULT_READ_TIMEOUT = 30000; // 30초
    private static final int DEFAULT_WRITE_TIMEOUT = 30000; // 30초

    public Web3jConfig(NoosphereConfigService noosphereConfigService) {
        this.noosphereConfigService = noosphereConfigService;
    }

    @Bean
    public Web3j web3j() {
        ApplicationProperties.NoosphereConfig.Chain chainConfig = noosphereConfigService.getActiveConfig().getChain();
        String rpcUrl = chainConfig.getRpcUrl();

        // null-safe한 방식으로 timeout 값들 가져오기
        ApplicationProperties.NoosphereConfig.Chain.ConnectionConfig connection = chainConfig.getConnection();

        int connectTimeout = getTimeoutValue(connection != null ? connection.getTimeout() : null, DEFAULT_CONNECT_TIMEOUT);
        int readTimeout = getTimeoutValue(connection != null ? connection.getReadTimeout() : null, DEFAULT_READ_TIMEOUT);
        int writeTimeout = getTimeoutValue(connection != null ? connection.getWriteTimeout() : null, DEFAULT_WRITE_TIMEOUT);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);

        HttpService httpService = new HttpService(rpcUrl, clientBuilder.build());

        // 이벤트 폴링 간격 설정 (processing-interval 설정 사용)
        long pollingInterval = chainConfig.getProcessingInterval() != null ? chainConfig.getProcessingInterval() : 5000L; // 기본값 5초

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        return Web3j.build(httpService, pollingInterval, scheduledExecutorService);
    }

    /**
     * timeout 값이 null인 경우 기본값을 반환하는 헬퍼 메서드
     */
    private int getTimeoutValue(Integer timeout, int defaultValue) {
        return timeout != null ? timeout : defaultValue;
    }

    @Bean
    public Credentials credentials(KeystoreService keystoreService) {
        String keyAlias = noosphereConfigService.getActiveConfig().getChain().getWallet().getKeystore().getKeys().getEth();
        if (keyAlias == null || keyAlias.isBlank()) {
            throw new IllegalStateException("Ethereum key alias 'application.chain.wallet.keystore.keys.eth' is not configured.");
        }
        // Delegate credential loading to the centralized KeystoreService
        return keystoreService.getCredentials(keyAlias);
    }

    @Bean
    public CustomGasProvider gasProvider() {
        return new CustomGasProvider(noosphereConfigService.getActiveConfig().getChain().getGasConfig());
    }

    public static class CustomGasProvider extends DefaultGasProvider {

        private final ApplicationProperties.NoosphereConfig.Chain.GasConfig gasConfig;

        public CustomGasProvider(ApplicationProperties.NoosphereConfig.Chain.GasConfig gasConfig) {
            this.gasConfig = gasConfig;
        }

        @Override
        public BigInteger getGasPrice(String contractFunc) {
            BigInteger basePrice = super.getGasPrice(contractFunc);
            return basePrice.multiply(BigInteger.valueOf((long) (gasConfig.getPriceMultiplier() * 100))).divide(BigInteger.valueOf(100));
        }

        @Override
        public BigInteger getGasLimit(String contractFunc) {
            BigInteger baseLimit = super.getGasLimit(contractFunc);
            return baseLimit.multiply(BigInteger.valueOf((long) (gasConfig.getLimitMultiplier() * 100))).divide(BigInteger.valueOf(100));
        }
    }

    @Bean
    public BigInteger chainId(Web3j web3j) {
        try {
            return web3j.ethChainId().send().getChainId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get chain ID", e);
        }
    }
}
