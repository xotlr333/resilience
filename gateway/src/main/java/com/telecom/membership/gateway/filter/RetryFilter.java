// File: membership/gateway/src/main/java/com/telecom/membership/gateway/filter/RetryFilter.java
package com.telecom.membership.gateway.filter;

import com.telecom.membership.common.event.EventMessage;
import com.telecom.membership.gateway.service.EventGridService;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public class RetryFilter extends AbstractGatewayFilterFactory<RetryFilter.Config> {

    private final RetryRegistry retryRegistry;
    private final EventGridService eventGridService;

    public RetryFilter(RetryRegistry retryRegistry, EventGridService eventGridService) {
        super(Config.class);
        this.retryRegistry = retryRegistry;
        this.eventGridService = eventGridService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Retry retry = retryRegistry.retry("point-service");

            return Mono.just(exchange)
                    .transformDeferred(RetryOperator.of(retry))
                    .flatMap(chain::filter)
                    .onErrorResume(throwable -> {
                        // 최대 재시도 후에도 실패하면 Event Grid로 발행
                        EventMessage<Object> message = EventMessage.builder()
                                .subject("point-service-retry-exhausted")
                                .eventType("RetryExhausted")
                                .data(exchange.getRequest().getBody())
                                .build();

                        return eventGridService.publishEvent(message)
                                .then(Mono.fromRunnable(() -> {
                                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                                    exchange.getResponse().getHeaders().add("X-Retry-Exhausted", "true");
                                }))
                                .then(exchange.getResponse().setComplete());
                    });
        };
    }

    public static class Config {
        private int retries;
        private List<HttpStatus> statuses;
        private List<HttpMethod> methods;
        private BackoffConfig backoff;
        // getters, setters ...
    }

    public static class BackoffConfig {
        private Duration firstBackoff;
        private Duration maxBackoff;
        private int factor;
        private boolean basedOnPreviousValue;
        // getters, setters ...
    }
}
