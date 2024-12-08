// File: membership/common/src/main/java/com/telecom/membership/common/exception/GlobalErrorHandler.java를 수정

package com.telecom.membership.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.*;

@Component
@Slf4j
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse;
        if (ex instanceof PointException.RateLimitExceededException) {
            errorResponse = buildError(TOO_MANY_REQUESTS, "처리 한도를 초과했습니다.");
        } else if (ex instanceof PointException.BulkheadFullException) {
            errorResponse = buildError(SERVICE_UNAVAILABLE, "시스템이 과부하 상태입니다.");
        } else if (ex instanceof PointException.CircuitBreakerOpenException) {
            errorResponse = buildError(SERVICE_UNAVAILABLE, "서비스가 일시적으로 불가합니다.");
        } else if (ex instanceof PointException.DatabaseException) {
            errorResponse = buildError(INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다.");
        } else {
            errorResponse = buildError(INTERNAL_SERVER_ERROR, "시스템 오류가 발생했습니다.");
        }

        byte[] bytes = toJson(errorResponse);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private ErrorResponse buildError(HttpStatus status, String message) {
        return new ErrorResponse(status.name(), message);
    }

    private byte[] toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object).getBytes();
        } catch (Exception e) {
            log.error("JSON 변환 실패", e);
            return "{}".getBytes();
        }
    }
}