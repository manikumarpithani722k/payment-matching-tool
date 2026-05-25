package com.paymentmatchingtool.service;

import com.paymentmatchingtool.dto.CsvPaymentRecord;
import com.paymentmatchingtool.exception.BadRequestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CsvParsingService {

    private static final String EXPECTED_HEADER = "orderid,amount,currency";
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "INR", "GBP");

    public List<CsvPaymentRecord> parse(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(label + " CSV file is required");
        }

        List<CsvPaymentRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !normalizeHeader(header).equals(EXPECTED_HEADER)) {
                throw new BadRequestException(label + " CSV must contain header: orderId,amount,currency");
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                records.add(parseLine(line, label, lineNumber));
            }
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read " + label + " CSV file");
        }

        if (records.isEmpty()) {
            throw new BadRequestException(label + " CSV must contain at least one data row");
        }

        return records;
    }

    private CsvPaymentRecord parseLine(String line, String label, int lineNumber) {
        String[] columns = line.split(",", -1);
        if (columns.length != 3) {
            throw new BadRequestException(label + " CSV line " + lineNumber + " must have exactly 3 columns");
        }

        String orderId = columns[0].trim();
        String amountText = columns[1].trim();
        String currency = columns[2].trim().toUpperCase(Locale.ROOT);

        if (orderId.isBlank()) {
            throw new BadRequestException(label + " CSV line " + lineNumber + " has blank orderId");
        }
        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            throw new BadRequestException(label + " CSV line " + lineNumber + " has unsupported currency: " + currency);
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
        } catch (NumberFormatException exception) {
            throw new BadRequestException(label + " CSV line " + lineNumber + " has invalid amount: " + amountText);
        }

        return new CsvPaymentRecord(orderId, amount, currency);
    }

    private String normalizeHeader(String header) {
        return header.replace("\uFEFF", "").replace(" ", "").trim().toLowerCase(Locale.ROOT);
    }
}
