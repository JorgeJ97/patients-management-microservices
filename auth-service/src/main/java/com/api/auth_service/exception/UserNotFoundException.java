package com.api.auth_service.exception;

import com.api.auth_service.util.ErrorMessages;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserNotFoundException extends UsernameNotFoundException {
    public UserNotFoundException(){
        super(ErrorMessages.USER_NOT_FOUND);
    }
}
