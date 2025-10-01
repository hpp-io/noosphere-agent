package io.hpp.noosphere.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * Properties specific to Noosphere Agent.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Validated
public class ApplicationProperties {

    // 기존 JHipster Liquibase 설정
    private final Liquibase liquibase = new Liquibase();

    @Valid
    @NestedConfigurationProperty
    private NoosphereConfig noosphere = new NoosphereConfig();

    // jhipster-needle-application-properties-property

    // Getter/Setter methods
    public Liquibase getLiquibase() {
        return liquibase;
    }

    public NoosphereConfig getNoosphere() {
        return noosphere;
    }

    public void setNoosphere(NoosphereConfig noosphere) {
        this.noosphere = noosphere;
    }

    // 내부 클래스들
    public static class Liquibase {

        private Boolean asyncStart = true;

        public Boolean getAsyncStart() {
            return asyncStart;
        }

        public void setAsyncStart(Boolean asyncStart) {
            this.asyncStart = asyncStart;
        }
    }

    @Validated
    public static class NoosphereConfig {

        @NotBlank
        private String configFilePath = "config/application-runtime.json";

        @NotNull
        @JsonProperty("forwardStats")
        private Boolean forwardStats = true;

        @NotNull
        @JsonProperty("manageContainers")
        private Boolean manageContainers = true;

        @Positive
        @JsonProperty("startupWait")
        private Double startupWait = 5.0;

        @Valid
        @NotNull
        @JsonProperty("server")
        @NestedConfigurationProperty
        private NoosphereServer server = new NoosphereServer();

        @Valid
        @NotNull
        @JsonProperty("chain")
        @NestedConfigurationProperty
        private Chain chain = new Chain();

        @Valid
        @JsonProperty("docker")
        @NestedConfigurationProperty
        private Docker docker;

        @Valid
        @NotNull
        @JsonProperty("containers")
        private List<NoosphereContainer> containers = new ArrayList<>();

        public String getConfigFilePath() {
            return configFilePath;
        }

        public void setConfigFilePath(String configFilePath) {
            this.configFilePath = configFilePath;
        }

        public Boolean getForwardStats() {
            return forwardStats;
        }

        public void setForwardStats(Boolean forwardStats) {
            this.forwardStats = forwardStats;
        }

        public Boolean getManageContainers() {
            return manageContainers;
        }

        public void setManageContainers(Boolean manageContainers) {
            this.manageContainers = manageContainers;
        }

        public Double getStartupWait() {
            return startupWait;
        }

        public void setStartupWait(Double startupWait) {
            this.startupWait = startupWait;
        }

        public NoosphereServer getServer() {
            return server;
        }

        public void setServer(NoosphereServer server) {
            this.server = server;
        }

        public Chain getChain() {
            return chain;
        }

        public void setChain(Chain chain) {
            this.chain = chain;
        }

        public Docker getDocker() {
            return docker;
        }

        public void setDocker(Docker docker) {
            this.docker = docker;
        }

        public List<NoosphereContainer> getContainers() {
            return containers;
        }

        public void setContainers(List<NoosphereContainer> containers) {
            this.containers = containers;
        }
    }

    @Validated
    public static class NoosphereServer {

        @NotNull
        @Min(1)
        @Max(65535)
        private Integer port = 8080;

        @Valid
        @NotNull
        @NestedConfigurationProperty
        private RateLimit rateLimit = new RateLimit();

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public RateLimit getRateLimit() {
            return rateLimit;
        }

        public void setRateLimit(RateLimit rateLimit) {
            this.rateLimit = rateLimit;
        }
    }

    @Validated
    public static class RateLimit {

        @NotNull
        @Positive
        private Integer numRequests = 100;

        @NotNull
        @Positive
        private Integer period = 60;

        public Integer getNumRequests() {
            return numRequests;
        }

        public void setNumRequests(Integer numRequests) {
            this.numRequests = numRequests;
        }

        public Integer getPeriod() {
            return period;
        }

        public void setPeriod(Integer period) {
            this.period = period;
        }
    }

    @Validated
    public static class Chain {

        @NotNull
        private Boolean enabled = true;

        private String rpcUrl = "http://localhost:8545";

        @NotNull
        private Integer trailHeadBlocks = 10;

        private String routerAddress;

        @Valid
        @NestedConfigurationProperty
        private Wallet wallet;

        @Valid
        @NotNull
        @NestedConfigurationProperty
        private SnapshotSync snapshotSync = new SnapshotSync();

        private ConnectionConfig connection = new ConnectionConfig();

        private GasConfig gasConfig = new GasConfig();

        public GasConfig getGasConfig() {
            return gasConfig;
        }

        public void setGasConfig(GasConfig gasConfig) {
            this.gasConfig = gasConfig;
        }

        public ConnectionConfig getConnection() {
            return connection;
        }

        public void setConnection(ConnectionConfig connection) {
            this.connection = connection;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getRpcUrl() {
            return rpcUrl;
        }

        public void setRpcUrl(String rpcUrl) {
            this.rpcUrl = rpcUrl;
        }

        public Integer getTrailHeadBlocks() {
            return trailHeadBlocks;
        }

        public void setTrailHeadBlocks(Integer trailHeadBlocks) {
            this.trailHeadBlocks = trailHeadBlocks;
        }

        public String getRouterAddress() {
            return routerAddress;
        }

        public void setRouterAddress(String routerAddress) {
            this.routerAddress = routerAddress;
        }

        public Wallet getWallet() {
            return wallet;
        }

        public void setWallet(Wallet wallet) {
            this.wallet = wallet;
        }

        public SnapshotSync getSnapshotSync() {
            return snapshotSync;
        }

        public void setSnapshotSync(SnapshotSync snapshotSync) {
            this.snapshotSync = snapshotSync;
        }
    }

    @Validated
    public static class Wallet {

        @NotNull
        @Positive
        private Integer maxGasLimit = 1000000;

        private String privateKey;
        private String paymentAddress;

        @NotNull
        private List<String> allowedSimErrors = new ArrayList<>();

        public Integer getMaxGasLimit() {
            return maxGasLimit;
        }

        public void setMaxGasLimit(Integer maxGasLimit) {
            this.maxGasLimit = maxGasLimit;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getPaymentAddress() {
            return paymentAddress;
        }

        public void setPaymentAddress(String paymentAddress) {
            this.paymentAddress = paymentAddress;
        }

        public List<String> getAllowedSimErrors() {
            return allowedSimErrors;
        }

        public void setAllowedSimErrors(List<String> allowedSimErrors) {
            this.allowedSimErrors = allowedSimErrors;
        }
    }

    @Validated
    public static class SnapshotSync {

        private Double sleep = 1.0;
        private Integer batchSize = 100;
        private Integer startingSubId = 0;
        private Double syncPeriod = 300.0;

        public Double getSleep() {
            return sleep;
        }

        public void setSleep(Double sleep) {
            this.sleep = sleep;
        }

        public Integer getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(Integer batchSize) {
            this.batchSize = batchSize;
        }

        public Integer getStartingSubId() {
            return startingSubId;
        }

        public void setStartingSubId(Integer startingSubId) {
            this.startingSubId = startingSubId;
        }

        public Double getSyncPeriod() {
            return syncPeriod;
        }

        public void setSyncPeriod(Double syncPeriod) {
            this.syncPeriod = syncPeriod;
        }
    }

    @Validated
    public static class ConnectionConfig {

        private Integer timeout;
        private Integer readTimeout;
        private Integer writeTimeout;

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Integer getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
        }

        public Integer getWriteTimeout() {
            return writeTimeout;
        }

        public void setWriteTimeout(Integer writeTimeout) {
            this.writeTimeout = writeTimeout;
        }
    }

    @Validated
    public static class GasConfig {

        private Double priceMultiplier;
        private Double limitMultiplier;

        public Double getPriceMultiplier() {
            return priceMultiplier;
        }

        public void setPriceMultiplier(Double priceMultiplier) {
            this.priceMultiplier = priceMultiplier;
        }

        public Double getLimitMultiplier() {
            return limitMultiplier;
        }

        public void setLimitMultiplier(Double limitMultiplier) {
            this.limitMultiplier = limitMultiplier;
        }
    }

    @Validated
    public static class Docker {

        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Validated
    public static class NoosphereContainer {

        @NotBlank
        private String id;

        @NotBlank
        private String image;

        @NotBlank
        private String url;

        @NotBlank
        private String bearer;

        @NotNull
        @Min(1)
        @Max(65535)
        private Integer port;

        @NotNull
        private Boolean external = false;

        @NotNull
        private Boolean gpu = false;

        @NotNull
        private Map<String, Integer> acceptedPayments = new HashMap<>();

        @NotNull
        private List<String> allowedIps = new ArrayList<>();

        @NotNull
        private List<String> allowedAddresses = new ArrayList<>();

        @NotNull
        private List<String> allowedDelegateAddresses = new ArrayList<>();

        private String description;
        private String command;

        @NotNull
        private Map<String, Object> env = new HashMap<>();

        @NotNull
        private Boolean generatesProofs = false;

        @NotNull
        private List<String> volumes = new ArrayList<>();

        // Getter/Setter methods
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getBearer() {
            return bearer;
        }

        public void setBearer(String bearer) {
            this.bearer = bearer;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Boolean getExternal() {
            return external;
        }

        public void setExternal(Boolean external) {
            this.external = external;
        }

        public Boolean getGpu() {
            return gpu;
        }

        public void setGpu(Boolean gpu) {
            this.gpu = gpu;
        }

        public Map<String, Integer> getAcceptedPayments() {
            return acceptedPayments;
        }

        public void setAcceptedPayments(Map<String, Integer> acceptedPayments) {
            this.acceptedPayments = acceptedPayments;
        }

        public List<String> getAllowedIps() {
            return allowedIps;
        }

        public void setAllowedIps(List<String> allowedIps) {
            this.allowedIps = allowedIps;
        }

        public List<String> getAllowedAddresses() {
            return allowedAddresses;
        }

        public void setAllowedAddresses(List<String> allowedAddresses) {
            this.allowedAddresses = allowedAddresses;
        }

        public List<String> getAllowedDelegateAddresses() {
            return allowedDelegateAddresses;
        }

        public void setAllowedDelegateAddresses(List<String> allowedDelegateAddresses) {
            this.allowedDelegateAddresses = allowedDelegateAddresses;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public Map<String, Object> getEnv() {
            return env;
        }

        public void setEnv(Map<String, Object> env) {
            this.env = env;
        }

        public Boolean getGeneratesProofs() {
            return generatesProofs;
        }

        public void setGeneratesProofs(Boolean generatesProofs) {
            this.generatesProofs = generatesProofs;
        }

        public List<String> getVolumes() {
            return volumes;
        }

        public void setVolumes(List<String> volumes) {
            this.volumes = volumes;
        }
    }
    // jhipster-needle-application-properties-property-getter
}
