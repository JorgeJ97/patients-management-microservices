package com.api.auth_service.service;

import com.api.auth_service.config.CustomUserDetails;
import com.api.auth_service.dto.AuthRequestDTO;
import com.api.auth_service.dto.AuthResponseDTO;
import com.api.auth_service.dto.UserInfoDTO;
import com.api.auth_service.exception.JwtAuthenticationException;
import com.api.auth_service.model.User;

import static com.api.auth_service.util.AuthConstants.BEARER_SCHEME;
import static com.api.auth_service.util.ErrorMessages.*;
import com.api.auth_service.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    public AuthResponseDTO authenticateAndGenerateToken(AuthRequestDTO request) {
       var authentication =  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        var token = jwtUtil.generateToken(user);

        return new AuthResponseDTO(token);

    }
    public UserInfoDTO validateTokenAndGetUserInfo(String authHeader) {
        log.info("Auth header: {}", authHeader);
        if(authHeader == null || !authHeader.startsWith(BEARER_SCHEME)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, AUTHENTICATION_FAILED);
        }
        try{
            validateToken(authHeader.substring(7));
            return getUserInfoFromToken(authHeader.substring(7));

        } catch (JwtAuthenticationException | JwtException | AuthenticationException e){
            log.warn("JWT token validation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, AUTHENTICATION_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error during token validation",e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_ERROR);
        }

    }

    private void validateToken(String token) {
        try{
        jwtUtil.isTokenValid(token);

        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException(INVALID_TOKEN_FORMAT);
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new JwtAuthenticationException(INVALID_TOKEN_SIGNATURE);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new JwtAuthenticationException(INVALID_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new JwtAuthenticationException(INVALID_TOKEN_DEFAULT + e.getMessage());
        }
    }

    private UserInfoDTO getUserInfoFromToken(String token) {
        return new UserInfoDTO(
                jwtUtil.getUserIdFromToken(token),
                jwtUtil.getEmailFromToken(token),
                jwtUtil.getRoleFromToken(token)
        );
    }


}
