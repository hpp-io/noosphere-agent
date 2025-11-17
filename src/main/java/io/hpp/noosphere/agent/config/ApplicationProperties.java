package io.hpp.noosphere.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
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

    @Setter
    @Getter
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

        @Valid
        @JsonProperty("agent")
        @NestedConfigurationProperty
        private Agent agent;

        @Valid
        @JsonProperty("Hub")
        @NestedConfigurationProperty
        private Hub hub;

        @Setter
        @Getter
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

            @Setter
            @Getter
            @Validated
            public static class RateLimit {

                @NotNull
                @Positive
                private Integer numRequests = 100;

                @NotNull
                @Positive
                private Integer period = 60;
            }
        }

        @Setter
        @Getter
        @Validated
        public static class Chain {

            @NotNull
            private Boolean enabled = true;

            private String rpcUrl = "http://localhost:8545";

            @NotNull
            private Long trailHeadBlocks = 10L;

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

            @Setter
            @Getter
            @Validated
            public static class Wallet {

                @NotNull
                @Positive
                private Integer maxGasLimit = 1000000;

                private String paymentAddress;

                @NotNull
                private final Keystore keystore = new Keystore();

                private List<String> allowedSimErrors = new ArrayList<>();

                @Getter
                @Setter
                public static class Keystore {

                    private String path;
                    private String password;
                    private final Keys keys = new Keys();

                    @Getter
                    @Setter
                    public static class Keys {

                        private String eth;
                    }
                }
            }

            @Setter
            @Getter
            @Validated
            public static class SnapshotSync {

                private Long sleep = (long) 1.0;
                private Integer batchSize = 100;
                private Integer startingSubId = 0;
                private String syncPeriod = "3000";
            }

            @Setter
            @Getter
            @Validated
            public static class ConnectionConfig {

                private Integer timeout;
                private Integer readTimeout;
                private Integer writeTimeout;
            }

            @Setter
            @Getter
            @Validated
            public static class GasConfig {

                private Double priceMultiplier;
                private Double limitMultiplier;
            }
        }

        @Setter
        @Getter
        @Validated
        public static class Docker {

            private String username;
            private String password;
        }

        @Setter
        @Getter
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
            private Boolean gpu = false;

            @NotNull
            private Map<String, Integer> acceptedPayments = new HashMap<>();

            private String description;
            private String command;

            private String verifierAddress;

            @NotNull
            private Map<String, Object> env = new HashMap<>();

            @NotNull
            private List<String> volumes = new ArrayList<>();
        }

        @Setter
        @Getter
        public static class Hub {

            @Getter
            private Boolean register;

            private String url;

            @Valid
            @NestedConfigurationProperty
            private KeepAlive keepAlive = new KeepAlive();

            @Setter
            @Getter
            public static class KeepAlive {

                private boolean enabled;
                private long intervalMs = 60000; // Default to 1 minute
                private long batchSize = 100;
            }
        }

        @Setter
        @Getter
        public static class Agent {

            private String name;
            private String publicEndpoint;
            private String apiKey;
            private String email;
        }
    }
}
