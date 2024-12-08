// File: membership/gateway/src/main/java/com/telecom/membership/gateway/config/BulkheadConfiguration.java
package com.telecom.membership.gateway.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class BulkheadConfiguration {

    @Value("${bulkhead.default.maxConcurrentCalls:50}")
    private int defaultMaxConcurrentCalls;

    @Value("${bulkhead.default.maxWaitDuration:500}")
    private long defaultMaxWaitDuration;

    @Value("${bulkhead.mart.maxConcurrentCalls:100}")
    private int martMaxConcurrentCalls;

    @Value("${bulkhead.mart.maxWaitDuration:500}")
    private long martMaxWaitDuration;

    @Value("${bulkhead.convenience.maxConcurrentCalls:200}")
    private int convenienceMaxConcurrentCalls;

    @Value("${bulkhead.convenience.maxWaitDuration:300}")
    private long convenienceMaxWaitDuration;

    @Value("${bulkhead.online.maxConcurrentCalls:50}")
    private int onlineMaxConcurrentCalls;

    @Value("${bulkhead.online.maxWaitDuration:1000}")
    private long onlineMaxWaitDuration;

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        // 기본 설정
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(defaultMaxConcurrentCalls)
                .maxWaitDuration(Duration.ofMillis(defaultMaxWaitDuration))
                .build();

        // Registry 생성
        BulkheadRegistry registry = BulkheadRegistry.of(defaultConfig);

        // 파트너별 설정 생성
        Map<String, BulkheadConfig> configs = Map.of(
                "mart", BulkheadConfig.custom()
                        .maxConcurrentCalls(martMaxConcurrentCalls)
                        .maxWaitDuration(Duration.ofMillis(martMaxWaitDuration))
                        .build(),
                "convenience", BulkheadConfig.custom()
                        .maxConcurrentCalls(convenienceMaxConcurrentCalls)
                        .maxWaitDuration(Duration.ofMillis(convenienceMaxWaitDuration))
                        .build(),
                "online", BulkheadConfig.custom()
                        .maxConcurrentCalls(onlineMaxConcurrentCalls)
                        .maxWaitDuration(Duration.ofMillis(onlineMaxWaitDuration))
                        .build()
        );

        // 파트너별 Bulkhead 생성
        configs.forEach((key, config) ->
                registry.bulkhead(key, config)
        );

        return registry;
    }
}