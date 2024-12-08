package com.telecom.membership.point.domain;

import com.telecom.membership.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("point_transactions")
public class PointTransaction {
    @Id
    private Long id;
    private String memberId;
    private String partnerId;
    private String partnerType;
    private BigDecimal amount;
    private BigDecimal points;
    private LocalDateTime transactionTime;
    private String status;

    // Builder에서 TransactionStatus를 받아 String으로 변환
    public static class PointTransactionBuilder {
        public PointTransactionBuilder status(TransactionStatus status) {
            this.status = status.name();
            return this;
        }
    }
}


