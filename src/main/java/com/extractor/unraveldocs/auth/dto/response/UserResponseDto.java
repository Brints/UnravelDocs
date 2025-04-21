package com.extractor.unraveldocs.auth.dto.response;

import com.extractor.unraveldocs.auth.dto.UserData;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record UserResponseDto(
        int status_code,
        String status,
        String message,
        UserData data
) {
}
