package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.request.GeneratePasswordDto;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.user.dto.response.GeneratePasswordResponse;
import com.extractor.unraveldocs.utils.userlib.UserLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GeneratePasswordImplTest {
    @Mock
    private UserLibrary userLibrary;

    @InjectMocks
    private GeneratePasswordImpl generatePasswordService;

    private GeneratePasswordDto validPasswordDto;
    private GeneratePasswordDto passwordDtoWithExcludedChars;
    private GeneratePasswordDto invalidLengthPasswordDto;

    @BeforeEach
    void setUp() {
        validPasswordDto = GeneratePasswordDto.builder()
                .passwordLength("12")
                .build();

        passwordDtoWithExcludedChars = GeneratePasswordDto.builder()
                .passwordLength("15")
                .excludedChars("aB1!")
                .build();

        invalidLengthPasswordDto = GeneratePasswordDto.builder()
                .passwordLength("6")
                .build();

    }

    @Test
    void generatePassword_WithValidLength_ReturnsSuccessResponse() {
        // Arrange
        String mockPassword = "MockPassword123!";
        when(userLibrary.generateStrongPassword(anyInt())).thenReturn(mockPassword);

        // Act
        GeneratePasswordResponse response = generatePasswordService.generatePassword(validPasswordDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("Password successfully generated.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(mockPassword, response.getData().generatedPassword());
    }

    @Test
    void generatePassword_WithExcludedChars_ReturnsSuccessResponse() {
        // Arrange
        String mockPassword = "MockP@ssw0rd";
        when(userLibrary.generateStrongPassword(anyInt(), any(char[].class))).thenReturn(mockPassword);

        // Act
        GeneratePasswordResponse response = generatePasswordService.generatePassword(passwordDtoWithExcludedChars);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("Password successfully generated.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(mockPassword, response.getData().generatedPassword());
    }

    @Test
    void generatePassword_WithLengthLessThan8_ThrowsBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> generatePasswordService.generatePassword(invalidLengthPasswordDto));

        assertEquals("Length should be greater than 8", exception.getMessage());
    }

    @Test
    void generatePassword_WithNullPasswordLength_ThrowsBadRequestException() {
        // Arrange
        GeneratePasswordDto nullLengthDto = GeneratePasswordDto.builder()
                .passwordLength(null)
                .build();

        // Act & Assert
        assertThrows(NumberFormatException.class,
                () -> generatePasswordService.generatePassword(nullLengthDto));
    }

    @Test
    void generatePassword_WithInvalidNumberFormat_ThrowsNumberFormatException() {
        // Arrange
        GeneratePasswordDto invalidFormatDto = GeneratePasswordDto.builder()
                .passwordLength("twelve")
                .build();

        // Act & Assert
        assertThrows(NumberFormatException.class,
                () -> generatePasswordService.generatePassword(invalidFormatDto));
    }

    @Test
    void generatePassword_WithEmptyExcludedChars_CallsSimpleGenerateMethod() {
        // Arrange
        String mockPassword = "MockPassword123!";
        GeneratePasswordDto emptyExcludedCharsDto = GeneratePasswordDto.builder()
                .passwordLength("12")
                .excludedChars("")
                .build();

        when(userLibrary.generateStrongPassword(anyInt())).thenReturn(mockPassword);

        // Act
        GeneratePasswordResponse response = generatePasswordService.generatePassword(emptyExcludedCharsDto);

        // Assert
        assertNotNull(response);
        assertEquals(mockPassword, response.getData().generatedPassword());
    }

    @Test
    void generatePassword_WithNullExcludedChars_CallsSimpleGenerateMethod() {
        // Arrange
        String mockPassword = "MockPassword123!";
        GeneratePasswordDto nullExcludedCharsDto = GeneratePasswordDto.builder()
                .passwordLength("12")
                .excludedChars(null)
                .build();

        when(userLibrary.generateStrongPassword(anyInt())).thenReturn(mockPassword);

        // Act
        GeneratePasswordResponse response = generatePasswordService.generatePassword(nullExcludedCharsDto);

        // Assert
        assertNotNull(response);
        assertEquals(mockPassword, response.getData().generatedPassword());
    }
}
