package com.lumenfield.station.service;

import com.lumenfield.station.model.Reading;
import com.lumenfield.station.model.SourceKind;
import com.lumenfield.station.repository.ReadingRepository;
import com.lumenfield.station.web.ReadingForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReadingService {

    private final ReadingRepository readings;
    private final Clock clock;

    public ReadingService(ReadingRepository readings, Clock clock) {
        this.readings = readings;
        this.clock = clock;
    }

    @Transactional
    public Reading record(ReadingForm form) {
        SourceKind source = ReadingRules.source(form.getSource());
        LocalDateTime hour = ReadingRules.hour(form.getRecordedAt(), clock);
        BigDecimal power = form.getPowerKw();
        BigDecimal energy = form.getEnergyKwh();
        ReadingRules.magnitudes(power, energy);
        readings.upsert(source, hour, power, energy);
        return new Reading(null, source, hour, power, energy);
    }

    public List<Reading> between(LocalDateTime from, LocalDateTime to) {
        return readings.between(from, to);
    }

    public List<Reading> recent(int limit) {
        return readings.recent(limit);
    }
}
