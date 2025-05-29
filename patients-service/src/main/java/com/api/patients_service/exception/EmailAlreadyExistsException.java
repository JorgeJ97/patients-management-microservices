package com.api.patients_service.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super(String.format("A patient with this email already exists %s", email));
    }
}
