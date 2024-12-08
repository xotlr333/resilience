// File: membership/gateway/src/main/java/com/telecom/membership/gateway/filter/CircuitBreakerFilter.java
package com.telecom.membership.gateway.filter;

import com.telecom.membership.common.event.EventMessage;
import com.telecom.membership.gateway.service.EventGridService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CircuitBreakerFilter extends AbstractGatewayFilterFactory<CircuitBreakerFilter.Config> {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final EventGridService eventGridService;
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerFilter.class);

    public CircuitBreakerFilter(CircuitBreakerRegistry circuitBreakerRegistry, EventGridService eventGridService) {
        super(Config.class);
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.eventGridService = eventGridService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("point-service");

        return (exchange, chain) -> {
            return Mono.just(exchange)
                    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                    .flatMap(chain::filter)
                    .onErrorResume(throwable -> {
                        // 실패 정보 구성
                        Map<String, Object> failureData = new HashMap<>();
                        failureData.put("service", "point-service");
                        failureData.put("timestamp", LocalDateTime.now().toString());
                        failureData.put("path", exchange.getRequest().getPath().value());
                        failureData.put("method", exchange.getRequest().getMethod().name());
                        failureData.put("error", throwable.getMessage());

                        EventMessage<Map<String, Object>> message = EventMessage.<Map<String, Object>>builder()
                                .subject("point-service-circuit-opened")
                                .eventType("CircuitBreakerOpened")
                                .data(failureData)  // data() 메서드 사용
                                .build();

                        return eventGridService.publishEvent(message)
                                .then(Mono.fromRunnable(() -> {
                                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                                    exchange.getResponse().getHeaders().add("X-Circuit-Open", "true");
                                    exchange.getResponse().getHeaders()
                                            .add("X-Circuit-State", circuitBreaker.getState().name());
                                }))
                                .then(exchange.getResponse().setComplete());
                    });
        };
    }

    public static class Config {
    }
}