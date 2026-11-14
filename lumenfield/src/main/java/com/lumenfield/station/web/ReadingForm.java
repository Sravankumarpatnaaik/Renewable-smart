package com.lumenfield.station.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReadingForm {

    @NotBlank(message = "Choose a source.")
    @Pattern(regexp = "SOLAR|WIND", message = "Source must be SOLAR or WIND.")
    private String source;

    @NotNull(message = "A timestamp is required.")
    private LocalDateTime recordedAt;

    @NotNull(message = "Power is required.")
    @DecimalMin(value = "0.000", message = "Power cannot be negative.")
    @DecimalMax(value = "80.000", message = "Power above 80 kW is not plausible at this station.")
    private BigDecimal powerKw;

    @NotNull(message = "Energy is required.")
    @DecimalMin(value = "0.000", message = "Energy cannot be negative.")
    @DecimalMax(value = "80.000", message = "One hour cannot hold more than 80 kWh here.")
    private BigDecimal energyKwh;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
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
