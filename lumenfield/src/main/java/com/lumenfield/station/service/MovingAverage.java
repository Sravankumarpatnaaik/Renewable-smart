package com.lumenfield.station.service;

import com.lumenfield.station.model.Reading;
import com.lumenfield.station.model.SourceKind;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public final class MovingAverage {

    private MovingAverage() {
    }

    /**
     * Mean energy for each clock hour, using only readings on the {@code windowDays}
     * dates before {@code target}. The target date itself is excluded.
     */
    public static BigDecimal[] hourlyEnergy(List<Reading> history, SourceKind source, LocalDate target, int windowDays) {
        LocalDate start = target.minusDays(windowDays);
        BigDecimal[] sum = new BigDecimal[24];
        int[] count = new int[24];
        for (int hour = 0; hour < 24; hour++) {
            sum[hour] = BigDecimal.ZERO;
        }
        for (Reading reading : history) {
            if (reading.source() != source) {
                continue;
            }
            LocalDate day = reading.recordedAt().toLocalDate();
            if (day.isBefore(start) || !day.isBefore(target)) {
                continue;
            }
            int hour = reading.recordedAt().getHour();
            sum[hour] = sum[hour].add(reading.energyKwh());
            count[hour]++;
        }
        BigDecimal[] average = new BigDecimal[24];
        for (int hour = 0; hour < 24; hour++) {
            average[hour] = count[hour] == 0
                    ? scale(BigDecimal.ZERO)
                    : sum[hour].divide(BigDecimal.valueOf(count[hour]), 3, RoundingMode.HALF_UP);
        }
        return average;
    }

    public static BigDecimal total(BigDecimal[] hours) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal hour : hours) {
            if (hour != null) {
                sum = sum.add(hour);
            }
        }
        return scale(sum);
    }

    public static BigDecimal scale(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}
