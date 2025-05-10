package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.dto.response.UserLoginResponse;
import com.extractor.unraveldocs.auth.interfaces.LoginUserService;
import com.extractor.unraveldocs.auth.service.AuthResponseBuilderService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.security.JwtTokenProvider;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginUserImpl implements LoginUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuthResponseBuilderService responseBuilder;

    public UserLoginResponse loginUser(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ForbiddenException("Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ForbiddenException("Invalid credentials.");
        }

        if (!user.isVerified()) {
            throw new BadRequestException("User is not yet verified. Please check your email for verification.");
        }

        String jwtToken = jwtTokenProvider.generateToken(user);
        user.setLastLogin(LocalDateTime.now());

        userRepository.save(user);

        return responseBuilder.buildUserLoginResponse(user, jwtToken);
    }
}
