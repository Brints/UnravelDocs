package com.extractor.unraveldocs.auth.dto.response;

import lombok.Builder;

@Builder
public record VerifyEmailResponse(
        int status_code,
        String status,
        String message
) {
}
