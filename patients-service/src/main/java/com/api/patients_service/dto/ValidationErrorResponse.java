package com.api.patients_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        String message,
        List<String> errors,
        LocalDateTime timestamp
) {
}
