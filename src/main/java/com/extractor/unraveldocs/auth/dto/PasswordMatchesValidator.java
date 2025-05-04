package com.extractor.unraveldocs.auth.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.PropertyAccessorFactory;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    String passwordField;
    String confirmPasswordField;

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Object password = PropertyAccessorFactory.forBeanPropertyAccess(value)
                    .getPropertyValue(passwordField);
            Object confirmPassword = PropertyAccessorFactory.forBeanPropertyAccess(value)
                    .getPropertyValue(confirmPasswordField);

            if (password == null || confirmPassword == null) {
                return false;
            }

            return password.toString().trim().equals(confirmPassword.toString().trim());
        } catch (Exception e) {
            return false;
        }
    }
}
