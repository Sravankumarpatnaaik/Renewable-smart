package com.lumenfield.station.exception;

public class InvalidReadingException extends StationException {

    public InvalidReadingException(String message) {
        super("invalid_reading", message);
    }
}
