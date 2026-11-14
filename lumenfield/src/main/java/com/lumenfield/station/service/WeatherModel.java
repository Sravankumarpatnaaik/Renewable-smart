package com.lumenfield.station.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Deterministic stand-in for the East Array and Ridge Turbine sensors. */
public final class WeatherModel {

    private WeatherModel() {
    }

    public static BigDecimal solarKw(LocalDate date, int hour) {
        if (hour < 6 || hour > 18) {
            return scale(0);
        }
        double curve = Math.sin(Math.PI * ((hour - 6) / 12.0));
        long day = date.toEpochDay();
        double noise = Math.abs(Math.sin(day * 12.9898));
        double cloud = day % 6 == 2 ? 0.26 + 0.10 * noise : 0.64 + 0.36 * noise;
        return scale(48.0 * curve * cloud);
    }

    public static BigDecimal windKw(LocalDate date, int hour) {
        long day = date.toEpochDay();
        double gust = 0.5 + 0.5 * Math.sin(day * 2.1 + hour * 1.7);
        double swell = 0.5 + 0.5 * Math.sin(day * 0.7 + hour * 0.35);
        double factor = day % 7 == 4 ? 0.06 + 0.08 * gust : 0.20 + 0.72 * swell * gust;
        factor = Math.min(1.0, Math.max(0, factor));
        return scale(30.0 * factor);
    }

    private static BigDecimal scale(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP);
    }
}
