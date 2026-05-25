package com.paymentmatchingtool.dto;

import com.paymentmatchingtool.enums.ResolutionSide;
import jakarta.validation.constraints.NotNull;

public record ResolveRequest(
        @NotNull ResolutionSide resolutionSide
) {
}
