package com.lumenfield.station.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherModelTest {

    @Test
    void solarIsDarkAtNightAndProducingAtNoon() {
        LocalDate day = LocalDate.of(2026, 11, 13);
        assertEquals(new java.math.BigDecimal("0.000"), WeatherModel.solarKw(day, 2));
        assertTrue(WeatherModel.solarKw(day, 12).signum() > 0);
        assertTrue(WeatherModel.windKw(day, 12).signum() > 0);
    }
}
