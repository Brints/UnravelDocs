package com.extractor.unraveldocs.auth.controller;

import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.dto.request.VerifyEmailDto;
import com.extractor.unraveldocs.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> register(@Valid @RequestBody SignUpRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login a registered user")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.loginUser(request));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify user email")
    public ResponseEntity<?> verifyEmail(
           @RequestParam String email,
           @RequestParam String token) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.verifyEmail(email, token));
    }
}
