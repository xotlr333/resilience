// File: membership/async/src/main/java/com/telecom/membership/async/controller/EventGridWebhookController.java
package com.telecom.membership.async.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.membership.async.dto.ValidationResponse;
import com.telecom.membership.async.service.PointHistoryManager;
import com.telecom.membership.common.dto.PointRequest;
import com.telecom.membership.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event Grid Webhook API", description = "Event Grid에서 전송된 이벤트를 처리하는 API")
public class EventGridWebhookController {

    private final ObjectMapper objectMapper;
    private final PointHistoryManager historyManager;

    @PostMapping("/point")
    @Operation(summary = "이벤트 수신", description = "Event Grid에서 전송된 포인트 관련 이벤트를 처리합니다.")
    public Mono<ResponseEntity<?>> handleEvent(
            @RequestHeader(value = "aeg-event-type", required = false) String aegEventType,
            @RequestBody String requestBody) {

        return Mono.defer(() -> {
            try {
                log.info("Received event type: {}", aegEventType);
                log.debug("Request body: {}", requestBody);

                // Subscription Validation 처리
                if ("SubscriptionValidation".equals(aegEventType)) {
                    JsonNode[] events = objectMapper.readValue(requestBody, JsonNode[].class);
                    String validationCode = events[0].get("data").get("validationCode").asText();
                    log.info("Handling validation request with code: {}", validationCode);
                    return Mono.just(ResponseEntity.ok().body(new ValidationResponse(validationCode)));
                }

                // 실제 이벤트 처리
                JsonNode[] events = objectMapper.readValue(requestBody, JsonNode[].class);
                JsonNode eventNode = events[0];
                String eventType = eventNode.get("eventType").asText();
                JsonNode data = eventNode.get("data");

                log.info("Processing {} event. Data: {}", eventType, data);

                // 이벤트 타입별 로깅
                switch (eventType) {
                    case "CircuitBreakerOpened":
                        log.warn("Circuit Breaker opened for service: {}, path: {}",
                                data.get("service").asText(),
                                data.get("path").asText());
                        break;
                    case "RetryExhausted":
                        log.warn("Retry exhausted for request. Path: {}", data.get("path").asText());
                        break;
                    case "ProcessingFailed":
                        log.warn("Processing failed. Error: {}", data.get("error").asText());
                        break;
                    default:
                        log.warn("Unknown event type received: {}", eventType);
                }

                // point 서비스 호출을 위한 요청 생성
                PointRequest pointRequest;
                if (data.has("originalRequest")) {
                    // originalRequest가 있는 경우 (retry, processing failed)
                    pointRequest = objectMapper.treeToValue(data.get("originalRequest"), PointRequest.class);
                } else {
                    // originalRequest가 없는 경우 (circuit breaker)
                    Map<String, String> pathParams = extractPathParams(data.get("path").asText());
                    pointRequest = PointRequest.builder()
                            .memberId(pathParams.getOrDefault("memberId", "unknown"))
                            .partnerId(pathParams.getOrDefault("partnerId", "unknown"))
                            .partnerType(pathParams.getOrDefault("partnerType", "UNKNOWN"))
                            .amount(new BigDecimal(pathParams.getOrDefault("amount", "0")))
                            .build();
                }

                // point 적립 처리
                return historyManager.processPointAccumulation(pointRequest)
                        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                        .onErrorResume(error -> {
                            log.error("Failed to process point accumulation for event type: {}", eventType, error);
                            return Mono.just(ResponseEntity.internalServerError()
                                    .body(ApiResponse.error("이벤트 처리 중 오류가 발생했습니다: " + error.getMessage())));
                        });

            } catch (Exception e) {
                log.error("Error processing event", e);
                return Mono.just(ResponseEntity.internalServerError()
                        .body(ApiResponse.error("이벤트 처리 중 오류가 발생했습니다: " + e.getMessage())));
            }
        });
    }

    /**
     * URL 경로에서 파라미터 추출
     */
    private Map<String, String> extractPathParams(String path) {
        Map<String, String> params = new HashMap<>();
        // URL path를 파싱하여 필요한 파라미터 추출
        // 예: /api/points/accumulate/{memberId}/{partnerId} 형식 가정
        try {
            String[] parts = path.split("/");
            for (String part : parts) {
                if (part.contains("=")) {
                    String[] keyValue = part.split("=");
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract path parameters from: {}", path);
        }
        return params;
    }

    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "서비스의 상태를 확인합니다.")
    public Mono<ResponseEntity<ApiResponse<String>>> healthCheck() {
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Healthy - " + LocalDateTime.now())));
    }
}