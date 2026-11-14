package com.lumenfield.station.web;

import com.lumenfield.station.model.Reading;
import com.lumenfield.station.service.ReadingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ReadingController {

    private final ReadingService readings;

    public ReadingController(ReadingService readings) {
        this.readings = readings;
    }

    @PostMapping("/api/readings")
    public Reading create(@Valid @RequestBody ReadingForm form) {
        return readings.record(form);
    }

    @GetMapping("/api/readings")
    public List<Reading> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return readings.between(from, to);
    }
}
