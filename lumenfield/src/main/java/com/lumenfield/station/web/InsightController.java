package com.lumenfield.station.web;

import com.lumenfield.station.service.AlertService;
import com.lumenfield.station.service.DashboardService;
import com.lumenfield.station.service.ForecastService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InsightController {

    private final DashboardService dashboard;
    private final ForecastService forecasts;
    private final AlertService alerts;

    public InsightController(DashboardService dashboard, ForecastService forecasts, AlertService alerts) {
        this.dashboard = dashboard;
        this.forecasts = forecasts;
        this.alerts = alerts;
    }

    @GetMapping("/api/dashboard/daily")
    public DashboardService.DailySeries daily() {
        return dashboard.today();
    }

    @GetMapping("/api/dashboard/weekly")
    public DashboardService.WeeklySeries weekly() {
        return dashboard.week();
    }

    @GetMapping("/api/predictions/tomorrow")
    public ForecastService.DayForecast tomorrow() {
        return forecasts.tomorrow();
    }

    @GetMapping("/api/predictions/comparison")
    public List<ForecastService.DayComparison> comparison() {
        return forecasts.recentComparison();
    }

    @GetMapping("/api/alerts")
    public List<AlertService.LowGeneration> alerts() {
        return alerts.recent();
    }
}
