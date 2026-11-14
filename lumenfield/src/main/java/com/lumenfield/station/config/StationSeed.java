package com.lumenfield.station.config;

import com.lumenfield.station.model.SourceKind;
import com.lumenfield.station.repository.ReadingRepository;
import com.lumenfield.station.service.WeatherModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class StationSeed implements ApplicationRunner {

    private final ReadingRepository readings;
    private final Clock clock;

    public StationSeed(ReadingRepository readings, Clock clock) {
        this.readings = readings;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (readings.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now(clock);
        int currentHour = LocalDateTime.now(clock).getHour();
        LocalDate start = today.minusDays(20);
        List<Object[]> batch = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(today); day = day.plusDays(1)) {
            int lastHour = day.equals(today) ? currentHour : 23;
            for (int hour = 0; hour <= lastHour; hour++) {
                LocalDateTime stamp = day.atTime(hour, 0);
                add(batch, SourceKind.SOLAR, stamp, WeatherModel.solarKw(day, hour));
                add(batch, SourceKind.WIND, stamp, WeatherModel.windKw(day, hour));
            }
        }
        readings.upsertAll(batch);
    }

    private static void add(List<Object[]> batch, SourceKind source, LocalDateTime stamp, BigDecimal kilowatts) {
        batch.add(new Object[] {source.name(), Timestamp.valueOf(stamp), kilowatts, kilowatts});
    }
}
