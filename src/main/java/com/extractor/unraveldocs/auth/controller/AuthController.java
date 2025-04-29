package com.extractor.unraveldocs.auth.controller;

import com.extractor.unraveldocs.auth.dto.request.GeneratePasswordDto;
import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.dto.request.ResendEmailVerificationDto;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/generate-password")
    @Operation(summary = "Generate a Strong Password.")
    public ResponseEntity<?> generatePassword(
            @Valid @RequestBody GeneratePasswordDto password
            ) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.generatePassword(password));
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> register(
            @Valid @RequestPart("request") SignUpRequestDto request,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
    ) {
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String contentType = profilePicture.getContentType();
            if (!MediaType.IMAGE_JPEG_VALUE.equals(contentType) &&
                    !MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Profile picture must be a JPEG or PNG image");
            }
        } else {
            // Explicitly set profilePicture to null if it's empty or not provided
            profilePicture = null;
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                authService.registerUser(request, profilePicture)
        );
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

    /**
     * Resend verification email to the user.
     *
     * @param request The email address of the user to resend the verification email to.
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/resend-verification-email")
    @Operation(summary = "Resend verification email")
    public ResponseEntity<?> resendVerificationEmail(
            @Valid @RequestBody ResendEmailVerificationDto request
            ) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.resendEmailVerification(request));
    }
}
