package com.api.auth_service.controller;

import com.api.auth_service.dto.AuthRequestDTO;
import com.api.auth_service.dto.AuthResponseDTO;
import com.api.auth_service.dto.UserInfoDTO;
import com.api.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static com.api.auth_service.util.ErrorMessages.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;


    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO loginRequest) {
        try {
        final var response = authService.authenticateAndGenerateToken(loginRequest);
        return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }
    }

    @Operation(summary = "Validate token and return user information")
    @GetMapping("/validate")
    public ResponseEntity<UserInfoDTO> validate(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        final var userInfoDTO = authService.validateTokenAndGetUserInfo(authHeader);
        return ResponseEntity.ok(userInfoDTO);

    }



}
