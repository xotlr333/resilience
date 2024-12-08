// File: membership/gateway/src/main/java/com/telecom/membership/gateway/filter/BulkheadFilter.java
package com.telecom.membership.gateway.filter;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkheadFilter implements WebFilter {

    private final BulkheadRegistry bulkheadRegistry;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String partnerType = exchange.getRequest().getHeaders()
                .getFirst("X-Partner-Type");

        // 파트너 타입 헤더가 없으면 기본 처리
        if (partnerType == null) {
            log.warn("Partner type header is missing");
            return chain.filter(exchange);
        }

        // 파트너 타입에 맞는 Bulkhead 가져오기
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(partnerType.toLowerCase());

        return Mono.just(exchange)
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .flatMap(chain::filter)
                .onErrorResume(BulkheadFullException.class, e -> {
                    log.warn("Bulkhead capacity full for partner type: {}", partnerType);
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                });
    }
}
