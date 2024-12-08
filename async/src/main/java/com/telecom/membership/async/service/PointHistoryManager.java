// File: membership/async/src/main/java/com/telecom/membership/async/service/PointHistoryManager.java
package com.telecom.membership.async.service;

import com.telecom.membership.async.domain.PointHistory;
import com.telecom.membership.async.repository.ReactivePointHistoryRepository;
import com.telecom.membership.common.dto.PointRequest;
import com.telecom.membership.common.dto.PointResponse;
import com.telecom.membership.common.service.PointProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointHistoryManager {
    private final ReactivePointHistoryRepository historyRepository;
    private final PointProcessor pointProcessor;

    public Mono<PointResponse> processPointAccumulation(PointRequest request) {
        return pointProcessor.processPoints(request)
                .flatMap(response -> {
                    // PointHistory 생성
                    PointHistory history = PointHistory.builder()
                            .memberId(request.getMemberId())
                            .partnerId(request.getPartnerId())
                            .partnerType(request.getPartnerType())
                            .amount(request.getAmount())
                            .points(response.getPoints())
                            .transactionTime(LocalDateTime.now())
                            .status(response.getStatus())
                            .processType("ACCUMULATION")
                            .retryCount(0)
                            .build();

                    // PointHistory를 저장하고 원본 response를 유지
                    return historyRepository.save(history)
                            .map(savedHistory -> {
                                // savedHistory 정보를 response에 반영
                                return PointResponse.builder()
                                        .transactionId(response.getTransactionId())
                                        .memberId(savedHistory.getMemberId())
                                        .partnerId(savedHistory.getPartnerId())
                                        .partnerType(savedHistory.getPartnerType())
                                        .amount(savedHistory.getAmount())
                                        .points(savedHistory.getPoints())
                                        .status(savedHistory.getStatus())
                                        .processedAt(savedHistory.getTransactionTime())
                                        .message("포인트가 정상적으로 처리되었습니다.")
                                        .build();
                            });
                })
                .doOnError(error -> {
                    log.error("Point accumulation failed for memberId={}, amount={}, error={}",
                            request.getMemberId(),
                            request.getAmount(),
                            error.getMessage(),
                            error);
                });
    }
}