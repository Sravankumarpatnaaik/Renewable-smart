package com.lumenfield.station.web;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.lumenfield.station.model.Reading;
import com.lumenfield.station.service.AlertService;
import com.lumenfield.station.service.DashboardService;
import com.lumenfield.station.service.ForecastService;
import com.lumenfield.station.service.ReadingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class StationController {

    private final DashboardService dashboard;
    private final ForecastService forecasts;
    private final AlertService alerts;
    private final ReadingService readings;
    private final ObjectMapper json;
    private final Clock clock;

    public StationController(DashboardService dashboard, ForecastService forecasts, AlertService alerts,
                             ReadingService readings, ObjectMapper json, Clock clock) {
        this.dashboard = dashboard;
        this.forecasts = forecasts;
        this.alerts = alerts;
        this.readings = readings;
        this.json = json;
        this.clock = clock;
    }

    @GetMapping("/")
    public String dashboard(Model model) throws JacksonException {
        DashboardService.DailySeries today = dashboard.today();
        model.addAttribute("section", "today");
        model.addAttribute("today", today);
        model.addAttribute("alerts", alerts.recent());
        model.addAttribute("tomorrow", forecasts.tomorrow());
        model.addAttribute("dailyJson", json.writeValueAsString(series(today)));
        model.addAttribute("weeklyJson", json.writeValueAsString(week(dashboard.week())));
        return "dashboard";
    }

    @GetMapping("/forecast")
    public String forecast(Model model) throws JacksonException {
        ForecastService.DayForecast tomorrow = forecasts.tomorrow();
        List<ForecastService.DayComparison> rows = forecasts.recentComparison();
        model.addAttribute("section", "forecast");
        model.addAttribute("tomorrow", tomorrow);
        model.addAttribute("rows", rows);
        model.addAttribute("forecastJson", json.writeValueAsString(series(tomorrow)));
        model.addAttribute("comparisonJson", json.writeValueAsString(comparison(rows)));
        return "forecast";
    }

    @GetMapping("/readings")
    public String readings(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", blankHour());
        }
        model.addAttribute("section", "readings");
        model.addAttribute("recent", readings.recent(12));
        return "reading";
    }

    @PostMapping("/readings")
    public String record(@Valid @ModelAttribute("form") HourLog form, BindingResult errors,
                         Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            model.addAttribute("section", "readings");
            model.addAttribute("recent", readings.recent(12));
            return "reading";
        }
        try {
            Reading saved = readings.record(form.toReading());
            redirect.addFlashAttribute("saved", saved);
            return "redirect:/readings";
        } catch (RuntimeException ex) {
            errors.reject("reading", ex.getMessage());
            model.addAttribute("section", "readings");
            model.addAttribute("recent", readings.recent(12));
            return "reading";
        }
    }

    private HourLog blankHour() {
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.HOURS);
        HourLog form = new HourLog();
        form.setSource("SOLAR");
        form.setDay(now.toLocalDate());
        form.setHour(now.getHour());
        return form;
    }

    private static Series series(DashboardService.DailySeries today) {
        return new Series(today.labels(), list(today.solarKwh()), list(today.windKwh()));
    }

    private static Series series(ForecastService.DayForecast forecast) {
        List<String> labels = new java.util.ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            labels.add(String.format("%02d:00", hour));
        }
        return new Series(labels, list(forecast.solarKwh()), list(forecast.windKwh()));
    }

    private static Series week(DashboardService.WeeklySeries week) {
        return new Series(week.labels(), week.solarKwh(), week.windKwh());
    }

    private static Bars comparison(List<ForecastService.DayComparison> rows) {
        List<String> labels = new java.util.ArrayList<>();
        List<java.math.BigDecimal> predicted = new java.util.ArrayList<>();
        List<java.math.BigDecimal> actual = new java.util.ArrayList<>();
        for (ForecastService.DayComparison row : rows) {
            labels.add(row.getDate().getDayOfMonth() + " " + row.getDate().getMonth().name().substring(0, 3));
            predicted.add(row.getPredictedTotal());
            actual.add(row.getActualTotal());
        }
        return new Bars(labels, predicted, actual);
    }

    private static List<java.math.BigDecimal> list(java.math.BigDecimal[] values) {
        return java.util.Arrays.asList(values);
    }

    public record Series(List<String> labels, List<java.math.BigDecimal> solarKwh, List<java.math.BigDecimal> windKwh) {
    }

    public record Bars(List<String> labels, List<java.math.BigDecimal> predictedKwh, List<java.math.BigDecimal> actualKwh) {
    }
}
