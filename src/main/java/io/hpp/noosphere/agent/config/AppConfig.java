package io.hpp.noosphere.agent.config;

import io.hpp.noosphere.agent.service.NoosphereConfigService;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(NoosphereConfigService noosphereConfigService) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
            Collections.singletonList((request, body, execution) -> {
                request.getHeaders().set("x-api-key", noosphereConfigService.getActiveConfig().getAgent().getApiKey());
                return execution.execute(request, body);
            })
        );
        return restTemplate;
    }
}
