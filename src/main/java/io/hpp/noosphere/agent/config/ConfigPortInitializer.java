package io.hpp.noosphere.agent.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

public class ConfigPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // 1. Find the config.json file path from application.yml
            String configFileName = applicationContext
                .getEnvironment()
                .getProperty("application.noosphere.config-file-path", "config.json");

            // 2. Use the same search logic as NoosphereConfigLoader
            String[] searchPaths = { ".", "./config", System.getProperty("user.home"), "/etc/noosphere-agent" };
            for (String searchPath : searchPaths) {
                Path configPath = Paths.get(searchPath, configFileName);
                if (Files.exists(configPath) && Files.isReadable(configPath)) {
                    // 3. Read server.port from the found config.json
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(configPath.toFile());
                    if (root.has("server") && root.get("server").has("port")) {
                        int port = root.get("server").get("port").asInt();

                        // 4. Add the server.port property to the Spring Environment
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("server.port", port);
                        applicationContext
                            .getEnvironment()
                            .getPropertySources()
                            .addFirst(new MapPropertySource("config-json-port", properties));
                        System.out.println("Server port set to " + port + " from " + configPath.toAbsolutePath());
                    }
                    return; // Stop after finding the first valid config file
                }
            }
        } catch (IOException e) {
            // Ignore if file not found or cannot be read
        }
    }
}
