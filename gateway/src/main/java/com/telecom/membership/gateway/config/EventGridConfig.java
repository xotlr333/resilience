// File: membership/common/src/main/java/com/telecom/membership/common/config/EventGridConfig.java
package com.telecom.membership.gateway.config;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventGridConfig {
    @Value("${azure.eventgrid.endpoint}")
    private String endpoint;

    @Value("${azure.eventgrid.key}")
    private String key;

    @Bean
    public EventGridPublisherClient eventGridPublisherClient() {
        return new EventGridPublisherClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildEventGridEventPublisherClient();
    }
}
