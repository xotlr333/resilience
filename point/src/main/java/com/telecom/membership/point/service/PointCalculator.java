package com.telecom.membership.point.service;

import com.telecom.membership.common.dto.PointRequest;
import com.telecom.membership.common.exception.PointException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class PointCalculator {
    private static final Map<String, BigDecimal> PARTNER_RATES = Map.of(
            "MART", new BigDecimal("0.01"),
            "CONVENIENCE", new BigDecimal("0.005"),
            "ONLINE", new BigDecimal("0.02")
    );

    public BigDecimal calculate(PointRequest request) {
        BigDecimal rate = Optional.ofNullable(PARTNER_RATES.get(request.getPartnerType()))
                .orElseThrow(() -> {
                    log.warn("Unknown partner type: {}", request.getPartnerType());
                    return new PointException("Invalid partner type: " + request.getPartnerType());
                });

        return request.getAmount()
                .multiply(rate)
                .setScale(0, RoundingMode.FLOOR);
    }
}