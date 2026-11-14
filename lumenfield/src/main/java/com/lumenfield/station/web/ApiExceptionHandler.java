package com.lumenfield.station.web;

import com.lumenfield.station.exception.StationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(StationException.class)
    public ResponseEntity<Map<String, String>> station(StationException ex) {
        HttpStatus status = ex instanceof com.lumenfield.station.exception.UnknownSourceException
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(body(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("The reading could not be accepted.");
        return body("invalid_reading", message);
    }

    private static Map<String, String> body(String code, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", code);
        body.put("message", message);
        return body;
    }
}
