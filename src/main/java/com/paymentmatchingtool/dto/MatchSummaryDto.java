package com.paymentmatchingtool.dto;

public record MatchSummaryDto(
        long total,
        long matched,
        long onlySystem,
        long onlyProvider,
        long amountMismatch
) {
}
