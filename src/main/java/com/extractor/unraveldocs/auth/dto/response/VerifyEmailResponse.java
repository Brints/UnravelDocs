package com.extractor.unraveldocs.auth.dto.response;

import lombok.Builder;

@Builder
public record VerifyEmailResponse(
        int statusCode,
        String status,
        String message
) {
}
