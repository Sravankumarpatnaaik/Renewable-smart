package com.lumenfield.station.exception;

public class UnknownSourceException extends StationException {

    public UnknownSourceException(String source) {
        super("unknown_source", "Unknown source \"" + source + "\". Use SOLAR or WIND.");
    }
}
