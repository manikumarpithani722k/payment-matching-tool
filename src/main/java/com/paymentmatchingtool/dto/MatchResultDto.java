package com.paymentmatchingtool.dto;

import com.paymentmatchingtool.entity.MatchResult;
import com.paymentmatchingtool.enums.MatchStatus;
import com.paymentmatchingtool.enums.ResolutionSide;
import java.math.BigDecimal;
import java.time.Instant;

public record MatchResultDto(
        Long id,
        String orderId,
        BigDecimal systemAmount,
        BigDecimal providerAmount,
        String currency,
        MatchStatus status,
        boolean resolved,
        ResolutionSide resolutionSide,
        Instant createdAt
) {
    public static MatchResultDto fromEntity(MatchResult result) {
        return new MatchResultDto(
                result.getId(),
                result.getOrderId(),
                result.getSystemAmount(),
                result.getProviderAmount(),
                result.getCurrency(),
                result.getStatus(),
                result.isResolved(),
                result.getResolutionSide(),
                result.getCreatedAt()
        );
    }
}
