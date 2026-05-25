package com.paymentmatchingtool.dto;

import java.util.List;

public record MatchRunResponse(
        MatchSummaryDto summary,
        List<MatchResultDto> results
) {
}
