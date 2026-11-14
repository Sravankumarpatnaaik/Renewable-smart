package com.lumenfield.station.service;

import com.lumenfield.station.exception.InvalidReadingException;
import com.lumenfield.station.exception.UnknownSourceException;
import com.lumenfield.station.model.SourceKind;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadingRulesTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-13T12:30:00Z"), ZoneId.of("Asia/Kolkata"));

    @Test
    void truncatesToTheHour() {
        LocalDateTime hour = ReadingRules.hour(LocalDateTime.of(2026, 11, 13, 18, 42), clock);
        assertEquals(LocalDateTime.of(2026, 11, 13, 18, 0), hour);
        assertEquals(SourceKind.WIND, ReadingRules.source("WIND"));
    }

    @Test
    void rejectsAFutureStampAndANegativePower() {
        assertThrows(InvalidReadingException.class,
                () -> ReadingRules.hour(LocalDateTime.of(2026, 11, 16, 1, 0), clock));
        assertThrows(InvalidReadingException.class,
                () -> ReadingRules.magnitudes(new BigDecimal("-1"), new BigDecimal("1")));
        assertThrows(UnknownSourceException.class, () -> ReadingRules.source("HYDRO"));
    }
}
