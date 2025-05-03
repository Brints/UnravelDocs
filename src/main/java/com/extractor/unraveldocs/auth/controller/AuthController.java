package com.extractor.unraveldocs.auth.controller;

import com.extractor.unraveldocs.auth.dto.request.GeneratePasswordDto;
import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.dto.request.ResendEmailVerificationDto;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.dto.response.SignupUserResponse;
import com.extractor.unraveldocs.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Register a new user with optional profile picture.
     *
     * @param request The sign-up request containing user details.
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping(value = "/signup",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE})
    @Operation(
            summary = "Register a new user",
            description = "Register a new user with optional profile picture",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = SignupUserResponse.class))
                    )
            }
    )
    public ResponseEntity<?> register(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Sign-up request containing user details",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SignUpRequestDto.class)
                                    ),
                            @Content(
                                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                    schema = @Schema(implementation = SignUpRequestDto.class)
                            )
                    }
            )
            @RequestBody SignUpRequestDto request
    ) {
        if (request.profilePicture() != null && !request.profilePicture().isEmpty()) {
            String contentType = request.profilePicture().getContentType();
            if (!MediaType.IMAGE_JPEG_VALUE.equals(contentType) &&
                    !MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Profile picture must be a JPEG or PNG image");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                authService.registerUser(request)
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
