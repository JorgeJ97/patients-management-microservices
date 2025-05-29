package com.api.patients_service.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String error,
        LocalDateTime timestamp
) {
}
