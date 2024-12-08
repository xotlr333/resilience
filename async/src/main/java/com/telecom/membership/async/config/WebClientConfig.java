// File: membership/async/src/main/java/com/telecom/membership/async/config/WebClientConfig.java
package com.telecom.membership.async.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }
}
