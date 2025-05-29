package com.api.auth_service.util;

public class ErrorMessages {
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String DEFAULT_ERROR = "An error occurred";
    public static final String INVALID_TOKEN_FORMAT = "Invalid JWT token format";
    public static final String INVALID_TOKEN_SIGNATURE = "Invalid JWT signature";
    public static final String INVALID_TOKEN_EXPIRED = "Invalid JWT token expired";
    public static final String INVALID_TOKEN_DEFAULT = "Could not validate JWT: ";
    public static final String USER_NOT_FOUND= "user not found";

    private ErrorMessages() {}
}
