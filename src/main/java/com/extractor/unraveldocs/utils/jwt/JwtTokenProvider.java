package com.extractor.unraveldocs.utils.jwt;

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
                .add("isVerified", user.isVerified())
                .add("userId", user.getId())
                .build();


        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
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

    public void parseToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            //log.warn("Expired JWT token: {}", ex.getMessage());
            throw new JwtAuthenticationException("Token expired", "EXPIRED_TOKEN");
        } catch (UnsupportedJwtException ex) {
            //log.warn("Unsupported JWT token: {}", ex.getMessage());
            throw new JwtAuthenticationException("Unsupported token", "UNSUPPORTED_TOKEN");
        } catch (MalformedJwtException ex) {
            //log.warn("Invalid JWT token: {}", ex.getMessage());
            throw new JwtAuthenticationException("Invalid token", "INVALID_TOKEN");
        } catch (SignatureException ex) {
            //log.warn("JWT signature validation failed: {}", ex.getMessage());
            throw new JwtAuthenticationException("Token signature invalid", "INVALID_SIGNATURE");
        } catch (IllegalArgumentException ex) {
            //log.warn("JWT claims string is empty: {}", ex.getMessage());
            throw new JwtAuthenticationException("Token claims empty", "EMPTY_CLAIMS");
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtAuthenticationException e) {
            return false;
        }
    }


//    public void validateToken(String token) {
//        try {
//            Jwts.parser()
//                    .verifyWith(key())
//                    .build()
//                    .parse(token);
//        } catch (JwtException | IllegalArgumentException ignored) {
//        }
//    }
}
