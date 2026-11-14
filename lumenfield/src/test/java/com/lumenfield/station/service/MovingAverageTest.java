package com.lumenfield.station.service;

import com.lumenfield.station.model.Reading;
import com.lumenfield.station.model.SourceKind;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MovingAverageTest {

    @Test
    void averagesTheSameHourAndIgnoresTheTargetDay() {
        LocalDate target = LocalDate.of(2026, 11, 14);
        List<Reading> history = new ArrayList<>();
        history.add(hour(SourceKind.SOLAR, target.minusDays(1), 12, "10.000"));
        history.add(hour(SourceKind.SOLAR, target.minusDays(2), 12, "30.000"));
        history.add(hour(SourceKind.SOLAR, target, 12, "99.000"));
        history.add(hour(SourceKind.WIND, target.minusDays(1), 12, "50.000"));

        BigDecimal[] solar = MovingAverage.hourlyEnergy(history, SourceKind.SOLAR, target, 7);

        assertEquals(new BigDecimal("20.000"), solar[12]);
        assertEquals(new BigDecimal("0.000"), solar[0]);
        assertEquals(new BigDecimal("20.000"), MovingAverage.total(solar));
    }

    @Test
    void windowDropsDaysOlderThanSeven() {
        LocalDate target = LocalDate.of(2026, 11, 14);
        List<Reading> history = List.of(
                hour(SourceKind.WIND, target.minusDays(8), 3, "90.000"),
                hour(SourceKind.WIND, target.minusDays(1), 3, "6.000"));

        BigDecimal[] wind = MovingAverage.hourlyEnergy(history, SourceKind.WIND, target, 7);

        assertEquals(new BigDecimal("6.000"), wind[3]);
    }

    private static Reading hour(SourceKind source, LocalDate day, int hour, String kwh) {
        BigDecimal value = new BigDecimal(kwh);
        return new Reading(null, source, LocalDateTime.of(day, java.time.LocalTime.of(hour, 0)), value, value);
    }
}
