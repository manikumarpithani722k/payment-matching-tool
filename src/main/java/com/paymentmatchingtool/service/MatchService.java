package com.paymentmatchingtool.service;

import com.paymentmatchingtool.dto.CsvPaymentRecord;
import com.paymentmatchingtool.dto.MatchResultDto;
import com.paymentmatchingtool.dto.MatchRunResponse;
import com.paymentmatchingtool.dto.MatchSummaryDto;
import com.paymentmatchingtool.entity.MatchResult;
import com.paymentmatchingtool.enums.MatchStatus;
import com.paymentmatchingtool.enums.ResolutionSide;
import com.paymentmatchingtool.enums.ResultFilter;
import com.paymentmatchingtool.exception.BadRequestException;
import com.paymentmatchingtool.exception.ResourceNotFoundException;
import com.paymentmatchingtool.repository.MatchResultRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MatchService {

    private final CsvParsingService csvParsingService;
    private final MatchResultRepository matchResultRepository;

    public MatchService(CsvParsingService csvParsingService, MatchResultRepository matchResultRepository) {
        this.csvParsingService = csvParsingService;
        this.matchResultRepository = matchResultRepository;
    }

    @Transactional
    public MatchRunResponse runMatch(MultipartFile systemFile, MultipartFile providerFile) {
        List<CsvPaymentRecord> systemRecords = csvParsingService.parse(systemFile, "System");
        List<CsvPaymentRecord> providerRecords = csvParsingService.parse(providerFile, "Provider");

        Map<PaymentKey, CsvPaymentRecord> systemByKey = indexByKey(systemRecords, "System");
        Map<PaymentKey, CsvPaymentRecord> providerByKey = indexByKey(providerRecords, "Provider");

        Set<PaymentKey> keys = new HashSet<>();
        keys.addAll(systemByKey.keySet());
        keys.addAll(providerByKey.keySet());

        List<MatchResult> results = keys.stream()
                .sorted(Comparator.comparing(PaymentKey::orderId).thenComparing(PaymentKey::currency))
                .map(key -> toMatchResult(key, systemByKey.get(key), providerByKey.get(key)))
                .toList();

        matchResultRepository.deleteAllInBatch();
        List<MatchResult> savedResults = matchResultRepository.saveAll(results);

        List<MatchResultDto> resultDtos = savedResults.stream()
                .map(MatchResultDto::fromEntity)
                .toList();

        return new MatchRunResponse(buildSummary(savedResults), resultDtos);
    }

    @Transactional(readOnly = true)
    public List<MatchResultDto> getResults(ResultFilter filter) {
        ResultFilter effectiveFilter = filter == null ? ResultFilter.UNRESOLVED : filter;

        List<MatchResult> results = switch (effectiveFilter) {
            case RESOLVED -> matchResultRepository.findByResolvedOrderByOrderIdAscCurrencyAsc(true);
            case UNRESOLVED -> matchResultRepository.findByResolvedOrderByOrderIdAscCurrencyAsc(false);
            case ALL -> matchResultRepository.findAllByOrderByOrderIdAscCurrencyAsc();
        };

        return results.stream()
                .map(MatchResultDto::fromEntity)
                .toList();
    }

    @Transactional
    public MatchResultDto resolve(Long id, ResolutionSide resolutionSide) {
        MatchResult result = matchResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match result not found for id: " + id));

        result.resolve(resolutionSide);
        return MatchResultDto.fromEntity(result);
    }

    private Map<PaymentKey, CsvPaymentRecord> indexByKey(List<CsvPaymentRecord> records, String label) {
        Map<PaymentKey, CsvPaymentRecord> indexed = new HashMap<>();
        List<String> duplicateKeys = new ArrayList<>();

        for (CsvPaymentRecord record : records) {
            PaymentKey key = new PaymentKey(record.orderId(), record.currency());
            if (indexed.putIfAbsent(key, record) != null) {
                duplicateKeys.add(record.orderId() + "/" + record.currency());
            }
        }

        if (!duplicateKeys.isEmpty()) {
            throw new BadRequestException(label + " CSV contains duplicate orderId + currency keys: " + String.join(", ", duplicateKeys));
        }

        return indexed;
    }

    private MatchResult toMatchResult(PaymentKey key, CsvPaymentRecord systemRecord, CsvPaymentRecord providerRecord) {
        BigDecimal systemAmount = systemRecord == null ? null : systemRecord.amount();
        BigDecimal providerAmount = providerRecord == null ? null : providerRecord.amount();
        MatchStatus status;

        if (systemRecord == null) {
            status = MatchStatus.ONLYPROVIDER;
        } else if (providerRecord == null) {
            status = MatchStatus.ONLYSYSTEM;
        } else if (systemAmount.compareTo(providerAmount) == 0) {
            status = MatchStatus.MATCHED;
        } else {
            status = MatchStatus.AMOUNTMISMATCH;
        }

        return new MatchResult(key.orderId(), systemAmount, providerAmount, key.currency(), status);
    }

    private MatchSummaryDto buildSummary(List<MatchResult> results) {
        long matched = countByStatus(results, MatchStatus.MATCHED);
        long onlySystem = countByStatus(results, MatchStatus.ONLYSYSTEM);
        long onlyProvider = countByStatus(results, MatchStatus.ONLYPROVIDER);
        long amountMismatch = countByStatus(results, MatchStatus.AMOUNTMISMATCH);

        return new MatchSummaryDto(results.size(), matched, onlySystem, onlyProvider, amountMismatch);
    }

    private long countByStatus(List<MatchResult> results, MatchStatus status) {
        return results.stream()
                .filter(result -> result.getStatus() == status)
                .count();
    }

    private record PaymentKey(String orderId, String currency) {
    }
}
