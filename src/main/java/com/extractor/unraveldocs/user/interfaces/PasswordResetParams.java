package com.extractor.unraveldocs.user.interfaces;

import lombok.Builder;

@Builder
public record PasswordResetParams(
        String email,
        String token
) implements IPasswordReset {
    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getToken() {
        return token;
    }
}
