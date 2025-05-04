package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;
import com.extractor.unraveldocs.user.interfaces.userimpl.ChangePasswordService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.ResponseBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangePasswordImpl implements ChangePasswordService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailTemplateService userEmailTemplateService;
    private final ResponseBuilderService responseBuilder;

    @Override
    public UserResponse changePassword(IPasswordReset params, ChangePasswordDto request) {
        String email = params.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        if (!user.isVerified()) {
            throw new ForbiddenException("Account not verified. Please verify your account first.");
        }

        boolean oldPassword = passwordEncoder.matches(request.oldPassword(), user.getPassword());
        if (!oldPassword) {
            throw new BadRequestException("Old password is incorrect.");
        }

        boolean isOldPassword =
                passwordEncoder.matches(request.newPassword(), user.getPassword());
        if (isOldPassword) {
            throw new BadRequestException("New password cannot be the same as the old password.");
        }

        // Update password logic (implementation not shown)
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        userEmailTemplateService.sendSuccessfulPasswordChange(email, user.getFirstName(), user.getLastName());

        return responseBuilder.buildResponseWithoutData("Password changed successfully.");
    }
}
