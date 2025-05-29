package com.api.api_gateway.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Authorization failed");
    }
}
