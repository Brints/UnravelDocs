package com.extractor.unraveldocs.auth.dto;

import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, SignUpRequestDto> {
    @Override
    public boolean isValid(SignUpRequestDto request, ConstraintValidatorContext context) {
        if (request.password() == null || request.confirmPassword() == null) {
            return false;
        }
        return request.password().trim().equals(request.confirmPassword().trim());
    }
}
