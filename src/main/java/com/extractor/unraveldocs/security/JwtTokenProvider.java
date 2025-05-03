package com.extractor.unraveldocs.security;

import com.extractor.unraveldocs.exceptions.custom.JwtAuthenticationException;
import com.extractor.unraveldocs.user.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private Long jwtExpirationInMs;

    public String generateToken(User user) {
        Claims claims = Jwts.claims()
                .subject(user.getEmail())
                .add("roles", user.getRole().name())
                .add("isVerified", user.isVerified())
                .add("userId", user.getId())
                .build();


        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(jwtExpirationInMs)))
                .signWith(key())
                .compact();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtAuthenticationException e) {
            return false;
        }
    }
}
