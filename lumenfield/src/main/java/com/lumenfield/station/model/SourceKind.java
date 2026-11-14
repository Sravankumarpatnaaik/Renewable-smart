package com.lumenfield.station.model;

import java.math.BigDecimal;

public enum SourceKind {
    SOLAR("East Array", new BigDecimal("48.000")),
    WIND("Ridge Turbine", new BigDecimal("30.000"));

    private final String asset;
    private final BigDecimal nameplateKw;

    SourceKind(String asset, BigDecimal nameplateKw) {
        this.asset = asset;
        this.nameplateKw = nameplateKw;
    }

    public String getAsset() {
        return asset;
    }

    public BigDecimal getNameplateKw() {
        return nameplateKw;
    }
}
