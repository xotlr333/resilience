// File: membership/gateway/src/main/java/com/telecom/membership/gateway/filter/RateLimitingFilter.java
package com.telecom.membership.gateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements WebFilter {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 파트너 타입에 따라 적절한 rate limiter 선택
        String partnerType = exchange.getRequest().getHeaders()
                .getFirst("X-Partner-Type");

        // 파트너 타입 헤더가 없으면 기본 처리
        if (partnerType == null) {
            return chain.filter(exchange);
        }

        // 파트너 타입에 해당하는 rate limiter 가져오기
        RateLimiter limiter = rateLimiterRegistry.rateLimiter(partnerType.toLowerCase());

        // rate limiter를 적용하여 요청 처리
        return Mono.just(exchange)
                .transformDeferred(RateLimiterOperator.of(limiter))
                .flatMap(chain::filter)
                .onErrorResume(RequestNotPermitted.class, e -> {
                    log.warn("Rate limit exceeded for partner type: {}", partnerType);
                    // 요청 거부 시 429 Too Many Requests 응답
                    exchange.getResponse().setStatusCode(TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                });
    }
}
