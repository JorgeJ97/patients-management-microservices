package com.api.patients_service.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String id) {
        super(String.format("Patient not found with ID: %s", id));
    }
}
