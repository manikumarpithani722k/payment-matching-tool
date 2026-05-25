package com.paymentmatchingtool.dto;

import java.math.BigDecimal;

public record CsvPaymentRecord(
        String orderId,
        BigDecimal amount,
        String currency
) {
}
