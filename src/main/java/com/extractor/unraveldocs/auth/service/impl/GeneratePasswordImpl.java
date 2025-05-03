package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.request.GeneratePasswordDto;
import com.extractor.unraveldocs.auth.interfaces.GeneratePasswordService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.user.dto.GeneratedPassword;
import com.extractor.unraveldocs.user.dto.response.GeneratePasswordResponse;
import com.extractor.unraveldocs.utils.userlib.UserLibrary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeneratePasswordImpl implements GeneratePasswordService {
    private final UserLibrary userLibrary;

    public GeneratePasswordResponse generatePassword(GeneratePasswordDto passwordDto) {
        int convertedLength = Integer.parseInt(passwordDto.passwordLength());
        if (convertedLength < 8) {
            throw new BadRequestException("Length should be greater than 8");
        }

        // Check if user provides a string of excluded characters
        String excludedChars = passwordDto.excludedChars();
        String generatedPassword;
        if (excludedChars != null && !excludedChars.isEmpty()) {
            char[] excludedCharsArray = excludedChars.toCharArray();
            generatedPassword =
                    userLibrary.generateStrongPassword(convertedLength, excludedCharsArray);
        } else {
            generatedPassword = userLibrary.generateStrongPassword(convertedLength);
        }

        GeneratePasswordResponse response = new GeneratePasswordResponse();
        response.setStatusCode(HttpStatus.OK.value());
        response.setStatus("success");
        response.setMessage("Password successfully generated.");
        response.setData(GeneratedPassword.builder()
                .generatedPassword(generatedPassword)
                .build());

        return response;
    }
}
