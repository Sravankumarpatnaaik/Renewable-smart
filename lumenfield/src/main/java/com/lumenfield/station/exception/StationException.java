package com.lumenfield.station.exception;

public class StationException extends RuntimeException {

    private final String code;

    public StationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
