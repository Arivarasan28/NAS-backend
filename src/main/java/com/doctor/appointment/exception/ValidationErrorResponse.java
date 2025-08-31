package com.doctor.appointment.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> validationErrors;

    public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, String message, Map<String, String> validationErrors) {
        super(timestamp, status, error, message, null);
        this.validationErrors = validationErrors;
    }
}
