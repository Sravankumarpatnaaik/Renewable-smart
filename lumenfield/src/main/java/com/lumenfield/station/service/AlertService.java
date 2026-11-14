package com.lumenfield.station.service;

import com.lumenfield.station.model.Reading;
import com.lumenfield.station.model.SourceKind;
import com.lumenfield.station.repository.ReadingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AlertService {

    private final ReadingRepository readings;
    private final ForecastService forecasts;
    private final Clock clock;

    public AlertService(ReadingRepository readings, ForecastService forecasts, Clock clock) {
        this.readings = readings;
        this.forecasts = forecasts;
        this.clock = clock;
    }

    public List<LowGeneration> recent() {
        LocalDate today = LocalDate.now(clock);
        List<LowGeneration> found = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            found.addAll(forDate(today.minusDays(offset)));
        }
        found.sort(Comparator.comparing(LowGeneration::recordedAt).reversed());
        if (found.size() > 8) {
            return found.subList(0, 8);
        }
        return found;
    }

    public List<LowGeneration> forDate(LocalDate date) {
        ForecastService.DayForecast baseline = forecasts.forecast(date);
        BigDecimal[] solar = hours(date, SourceKind.SOLAR);
        BigDecimal[] wind = hours(date, SourceKind.WIND);
        List<LowGeneration> alerts = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            if (solar[hour] == null && wind[hour] == null) {
                continue;
            }
            consider(alerts, date, hour, SourceKind.SOLAR, solar[hour], baseline.solarKwh()[hour], 8, 16, new BigDecimal("1.500"), new BigDecimal("0.45"));
            consider(alerts, date, hour, SourceKind.WIND, wind[hour], baseline.windKwh()[hour], 0, 23, new BigDecimal("3.000"), new BigDecimal("0.40"));
        }
        return alerts;
    }

    private void consider(List<LowGeneration> alerts, LocalDate date, int hour, SourceKind source,
                          BigDecimal actual, BigDecimal baseline, int fromHour, int toHour,
                          BigDecimal minimumBaseline, BigDecimal fraction) {
        if (hour < fromHour || hour > toHour || actual == null) {
            return;
        }
        if (baseline.compareTo(minimumBaseline) < 0) {
            return;
        }
        if (actual.compareTo(baseline.multiply(fraction)) >= 0) {
            return;
        }
        String severity = actual.compareTo(baseline.multiply(new BigDecimal("0.25"))) < 0 ? "low" : "watch";
        String asset = source.getAsset();
        String message = asset + " produced " + actual.setScale(1, RoundingMode.HALF_UP)
                + " kWh at " + String.format("%02d:00", hour)
                + ", against a " + baseline.setScale(1, RoundingMode.HALF_UP) + " kWh baseline for that hour.";
        alerts.add(new LowGeneration(source, date.atTime(hour, 0), severity, actual, baseline, message));
    }

    private BigDecimal[] hours(LocalDate date, SourceKind source) {
        BigDecimal[] values = new BigDecimal[24];
        LocalDateTime from = date.atStartOfDay();
        for (Reading reading : readings.between(from, date.plusDays(1).atStartOfDay())) {
            if (reading.source() == source) {
                values[reading.recordedAt().getHour()] = reading.energyKwh();
            }
        }
        return values;
    }

    public record LowGeneration(
            SourceKind source,
            LocalDateTime recordedAt,
            String severity,
            BigDecimal actualKwh,
            BigDecimal baselineKwh,
            String message) {
    }
}
