package com.api.auth_service.mapper;

import com.api.auth_service.dto.AuthRequestDTO;
import com.api.auth_service.model.User;

public class UserMapper {
    public static User toModel(AuthRequestDTO requestDTO) {
        return User.builder()
                .email(requestDTO.email())
                .password(requestDTO.password())
                .build();
    }
}
