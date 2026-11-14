package com.lumenfield.station.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class HourLog {

    @NotBlank(message = "Choose a source.")
    @Pattern(regexp = "SOLAR|WIND", message = "Source must be SOLAR or WIND.")
    private String source;

    @NotNull(message = "A day is required.")
    private LocalDate day;

    @NotNull(message = "An hour is required.")
    @Min(value = 0, message = "Hour must be from 0 to 23.")
    @Max(value = 23, message = "Hour must be from 0 to 23.")
    private Integer hour;

    @NotNull(message = "Power is required.")
    @DecimalMin(value = "0.000", message = "Power cannot be negative.")
    @DecimalMax(value = "80.000", message = "Power above 80 kW is not plausible at this station.")
    private BigDecimal powerKw;

    @NotNull(message = "Energy is required.")
    @DecimalMin(value = "0.000", message = "Energy cannot be negative.")
    @DecimalMax(value = "80.000", message = "One hour cannot hold more than 80 kWh here.")
    private BigDecimal energyKwh;

    public ReadingForm toReading() {
        ReadingForm form = new ReadingForm();
        form.setSource(source);
        form.setRecordedAt(LocalDateTime.of(day, LocalTime.of(hour, 0)));
        form.setPowerKw(powerKw);
        form.setEnergyKwh(energyKwh);
        return form;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public BigDecimal getPowerKw() {
        return powerKw;
    }

    public void setPowerKw(BigDecimal powerKw) {
        this.powerKw = powerKw;
    }

    public BigDecimal getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(BigDecimal energyKwh) {
        this.energyKwh = energyKwh;
    }
}
