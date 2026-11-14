package com.lumenfield.station.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Reading(
        Long id,
        SourceKind source,
        LocalDateTime recordedAt,
        BigDecimal powerKw,
        BigDecimal energyKwh) {
}
