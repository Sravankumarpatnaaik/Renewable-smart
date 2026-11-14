package com.lumenfield.station.service;

import com.lumenfield.station.exception.InvalidReadingException;
import com.lumenfield.station.exception.UnknownSourceException;
import com.lumenfield.station.model.SourceKind;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class ReadingRules {

    private static final BigDecimal CEILING = new BigDecimal("80");

    private ReadingRules() {
    }

    public static SourceKind source(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new UnknownSourceException(raw == null ? "" : raw);
        }
        try {
            return SourceKind.valueOf(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new UnknownSourceException(raw.trim());
        }
    }

    public static LocalDateTime hour(LocalDateTime recordedAt, Clock clock) {
        if (recordedAt == null) {
            throw new InvalidReadingException("A timestamp is required.");
        }
        if (recordedAt.isAfter(LocalDateTime.now(clock).plusHours(2))) {
            throw new InvalidReadingException("Readings cannot be dated more than two hours ahead.");
        }
        return recordedAt.truncatedTo(ChronoUnit.HOURS);
    }

    public static void magnitudes(BigDecimal powerKw, BigDecimal energyKwh) {
        if (powerKw == null || energyKwh == null || powerKw.signum() < 0 || energyKwh.signum() < 0) {
            throw new InvalidReadingException("Power and energy must be zero or positive.");
        }
        if (powerKw.compareTo(CEILING) > 0 || energyKwh.compareTo(CEILING) > 0) {
            throw new InvalidReadingException("A single hour cannot exceed 80 kW at this station.");
        }
    }
}
