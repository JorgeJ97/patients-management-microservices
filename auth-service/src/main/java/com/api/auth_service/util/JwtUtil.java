package com.api.auth_service.util;

import com.api.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    @Value("${jwt.expiration.time}")
    private String expirationTime;

    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        byte [] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .id(user.getId().toString())
                .claims(Map.of("role", user.getRole()))
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Long.parseLong(expirationTime)))
                .signWith(getSignKey())
                .compact();
    }

    public Date getExpirationTimeFromToken(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public String getEmailFromToken(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public String getUserIdFromToken(String token) {return extractClaims(token, Claims::getId);}

    public String getRoleFromToken(String token) {
        return extractClaims(token, claims ->
                claims.get("role", String.class));
    }

    public boolean isTokenValid(String token){
        var email = getEmailFromToken(token);
        return (email !=null && !isTokenExpired(token));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        var email = getEmailFromToken(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return getExpirationTimeFromToken(token).before(new Date());
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claim = extractAllClaims(token);
        return claimsResolver.apply(claim);

    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        return this.secretKey;
    }
}
