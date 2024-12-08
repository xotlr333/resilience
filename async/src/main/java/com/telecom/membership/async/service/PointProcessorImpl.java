package com.telecom.membership.async.service;

import com.telecom.membership.common.dto.PointRequest;
import com.telecom.membership.common.dto.PointResponse;
import com.telecom.membership.common.event.EventMessage;
import com.telecom.membership.common.service.PointProcessor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PointProcessorImpl implements PointProcessor {

    private final WebClient webClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final EventGridService eventGridService;

    @Value("${services.point.url}")
    private String pointServiceUrl;

    public PointProcessorImpl(WebClient webClient,
                              CircuitBreakerRegistry circuitBreakerRegistry,
                              RetryRegistry retryRegistry,
                              EventGridService eventGridService) {
        this.webClient = webClient;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.eventGridService = eventGridService;
    }

    @Override
    public Mono<PointResponse> processPoints(PointRequest request) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("async-processor");
        Retry retry = retryRegistry.retry("async-retry");

        return webClient.post()
                .uri(pointServiceUrl + "/api/points/accumulate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PointResponse.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .doOnSuccess(response ->
                        log.info("Point accumulation processed successfully for memberId={}",
                                request.getMemberId()))
                .onErrorResume(throwable -> {
                    log.error("Error processing point accumulation for memberId={}",
                            request.getMemberId(), throwable);

                    // 실패 정보를 포함한 데이터 구성
                    Map<String, Object> failureData = new HashMap<>();
                    failureData.put("originalRequest", request);
                    failureData.put("error", throwable.getMessage());
                    failureData.put("timestamp", LocalDateTime.now().toString());
                    failureData.put("service", "point-service");

                    // Circuit Breaker 상태 정보 추가
                    if (circuitBreaker != null) {
                        failureData.put("circuitBreakerState", circuitBreaker.getState().name());
                        failureData.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
                    }

                    // Retry 정보 추가
                    if (retry != null) {
                        failureData.put("retryCount", retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
                    }

                    // Event Grid로 메시지 발행
                    EventMessage<Map<String, Object>> message = EventMessage.<Map<String, Object>>builder()
                            .subject("point-processing-failed")
                            .eventType("ProcessingFailed")
                            .data(failureData)
                            .build();

                    return eventGridService.publishEvent(message)
                            .then(Mono.error(throwable)); // 원본 에러를 전파하여 재시도 로직이 동작하도록 함
                });
    }
}