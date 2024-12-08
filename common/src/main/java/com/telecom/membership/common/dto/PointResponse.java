package com.telecom.membership.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// File: membership/common/src/main/java/com/telecom/membership/common/dto/PointResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointResponse {
    private Long transactionId;
    private String memberId;
    private String partnerId;
    private String partnerType;
    private BigDecimal amount;          // 거래 금액
    private BigDecimal points;          // 적립 포인트
    private String status;              // 상태 (COMPLETED, FAILED)
    private LocalDateTime processedAt;   // 처리 시간
    private String message;             // 처리 결과 메시지
}