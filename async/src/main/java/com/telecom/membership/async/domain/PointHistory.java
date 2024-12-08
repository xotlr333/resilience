package com.telecom.membership.async.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "point_history")
@Data
@Builder
public class PointHistory {
    @Id
    private String id;
    private String memberId;
    private String partnerId;
    private String partnerType;
    private BigDecimal amount;
    private BigDecimal points;
    private LocalDateTime transactionTime;
    private String status;
    private String processType;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime lastRetryTime;
}
