package com.telecom.membership.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PointRequest {
    @NotNull
    private String memberId;
    
    @NotNull
    private String partnerId;
    
    @NotNull
    @Pattern(regexp = "MART|CONVENIENCE|ONLINE")
    private String partnerType;
    
    @NotNull
    @Positive
    private BigDecimal amount;
}
