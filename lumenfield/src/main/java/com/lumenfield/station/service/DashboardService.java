package com.lumenfield.station.service;

import com.lumenfield.station.model.Reading;
import com.lumenfield.station.model.SourceKind;
import com.lumenfield.station.repository.ReadingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM");

    private final ReadingRepository readings;
    private final Clock clock;

    public DashboardService(ReadingRepository readings, Clock clock) {
        this.readings = readings;
        this.clock = clock;
    }

    public DailySeries today() {
        return daily(LocalDate.now(clock));
    }

    public DailySeries daily(LocalDate date) {
        BigDecimal[] solar = new BigDecimal[24];
        BigDecimal[] wind = new BigDecimal[24];
        for (Reading reading : readings.between(date.atStartOfDay(), date.plusDays(1).atStartOfDay())) {
            int hour = reading.recordedAt().getHour();
            if (reading.source() == SourceKind.SOLAR) {
                solar[hour] = reading.energyKwh();
            } else {
                wind[hour] = reading.energyKwh();
            }
        }
        List<String> labels = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            labels.add(String.format("%02d:00", hour));
        }
        int peak = -1;
        BigDecimal peakKwh = BigDecimal.ZERO;
        for (int hour = 0; hour < 24; hour++) {
            if (solar[hour] == null && wind[hour] == null) {
                continue;
            }
            BigDecimal combined = nz(solar[hour]).add(nz(wind[hour]));
            if (peak < 0 || combined.compareTo(peakKwh) > 0) {
                peak = hour;
                peakKwh = combined;
            }
        }
        return new DailySeries(date, labels, solar, wind, peak < 0 ? null : String.format("%02d:00", peak),
                peak < 0 ? BigDecimal.ZERO : peakKwh.setScale(3), MovingAverage.total(solar), MovingAverage.total(wind));
    }

    public WeeklySeries week() {
        LocalDate end = LocalDate.now(clock);
        LocalDate start = end.minusDays(6);
        List<String> labels = new ArrayList<>();
        List<BigDecimal> solar = new ArrayList<>();
        List<BigDecimal> wind = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            DailySeries series = daily(day);
            labels.add(DAY.format(day));
            solar.add(series.solarTotal());
            wind.add(series.windTotal());
        }
        return new WeeklySeries(labels, solar, wind);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record DailySeries(
            LocalDate date,
            List<String> labels,
            BigDecimal[] solarKwh,
            BigDecimal[] windKwh,
            String peakHour,
            BigDecimal peakKwh,
            BigDecimal solarTotal,
            BigDecimal windTotal) {

        public BigDecimal combinedTotal() {
            return MovingAverage.scale(solarTotal.add(windTotal));
        }
    }

    public record WeeklySeries(List<String> labels, List<BigDecimal> solarKwh, List<BigDecimal> windKwh) {
    }
}
