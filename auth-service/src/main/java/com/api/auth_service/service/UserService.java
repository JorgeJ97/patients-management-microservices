package com.api.auth_service.service;

import com.api.auth_service.model.User;
import com.api.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);

    }

}
