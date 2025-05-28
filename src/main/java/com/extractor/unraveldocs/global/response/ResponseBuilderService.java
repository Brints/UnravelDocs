package com.extractor.unraveldocs.global.response;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ResponseBuilderService {
    public <T> UserResponse<T> buildUserResponse(T data, HttpStatus statusCode, String message) {
        UserResponse<T> userResponse = new UserResponse<>();
        userResponse.setStatusCode(statusCode.value());
        userResponse.setStatus("success");
        userResponse.setMessage(message);
        userResponse.setData(data);

        return userResponse;
    }
}
