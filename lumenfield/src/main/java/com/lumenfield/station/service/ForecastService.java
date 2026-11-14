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
import java.util.List;

@Service
public class ForecastService {

    public static final int WINDOW_DAYS = 7;

    private final ReadingRepository readings;
    private final Clock clock;

    public ForecastService(ReadingRepository readings, Clock clock) {
        this.readings = readings;
        this.clock = clock;
    }

    public DayForecast tomorrow() {
        return forecast(LocalDate.now(clock).plusDays(1));
    }

    public DayForecast forecast(LocalDate target) {
        List<Reading> history = historyBefore(target);
        BigDecimal[] solar = MovingAverage.hourlyEnergy(history, SourceKind.SOLAR, target, WINDOW_DAYS);
        BigDecimal[] wind = MovingAverage.hourlyEnergy(history, SourceKind.WIND, target, WINDOW_DAYS);
        return new DayForecast(target, WINDOW_DAYS, solar, wind);
    }

    public List<DayComparison> recentComparison() {
        LocalDate end = LocalDate.now(clock).minusDays(1);
        List<DayComparison> rows = new ArrayList<>();
        for (int offset = WINDOW_DAYS - 1; offset >= 0; offset--) {
            LocalDate day = end.minusDays(offset);
            DayForecast forecast = forecast(day);
            BigDecimal[] actualSolar = actualHours(day, SourceKind.SOLAR);
            BigDecimal[] actualWind = actualHours(day, SourceKind.WIND);
            rows.add(new DayComparison(
                    day,
                    MovingAverage.total(forecast.solarKwh()),
                    MovingAverage.total(actualSolar),
                    MovingAverage.total(forecast.windKwh()),
                    MovingAverage.total(actualWind)));
        }
        return rows;
    }

    private List<Reading> historyBefore(LocalDate target) {
        LocalDateTime from = target.minusDays(WINDOW_DAYS).atStartOfDay();
        LocalDateTime to = target.atStartOfDay();
        return readings.between(from, to);
    }

    private BigDecimal[] actualHours(LocalDate day, SourceKind source) {
        BigDecimal[] hours = new BigDecimal[24];
        for (Reading reading : readings.between(day.atStartOfDay(), day.plusDays(1).atStartOfDay())) {
            if (reading.source() == source) {
                hours[reading.recordedAt().getHour()] = reading.energyKwh();
            }
        }
        return hours;
    }

    public record DayForecast(LocalDate date, int windowDays, BigDecimal[] solarKwh, BigDecimal[] windKwh) {
        public BigDecimal solarTotal() {
            return MovingAverage.total(solarKwh);
        }

        public BigDecimal windTotal() {
            return MovingAverage.total(windKwh);
        }

        public BigDecimal combinedTotal() {
            return MovingAverage.scale(solarTotal().add(windTotal()));
        }
    }

    public static final class DayComparison {
        private final LocalDate date;
        private final BigDecimal predictedSolar;
        private final BigDecimal actualSolar;
        private final BigDecimal predictedWind;
        private final BigDecimal actualWind;

        public DayComparison(LocalDate date, BigDecimal predictedSolar, BigDecimal actualSolar,
                             BigDecimal predictedWind, BigDecimal actualWind) {
            this.date = date;
            this.predictedSolar = predictedSolar;
            this.actualSolar = actualSolar;
            this.predictedWind = predictedWind;
            this.actualWind = actualWind;
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getPredictedSolar() {
            return predictedSolar;
        }

        public BigDecimal getActualSolar() {
            return actualSolar;
        }

        public BigDecimal getPredictedWind() {
            return predictedWind;
        }

        public BigDecimal getActualWind() {
            return actualWind;
        }

        public BigDecimal getPredictedTotal() {
            return predictedSolar.add(predictedWind).setScale(3, RoundingMode.HALF_UP);
        }

        public BigDecimal getActualTotal() {
            return actualSolar.add(actualWind).setScale(3, RoundingMode.HALF_UP);
        }

        public BigDecimal getGap() {
            return getActualTotal().subtract(getPredictedTotal()).setScale(3, RoundingMode.HALF_UP);
        }
    }
}
