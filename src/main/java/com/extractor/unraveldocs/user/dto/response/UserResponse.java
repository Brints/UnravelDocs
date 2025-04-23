package com.extractor.unraveldocs.user.dto.response;

import com.extractor.unraveldocs.user.dto.UserData;
import lombok.Data;

@Data
public class UserResponse {
    int status_code;
    String status;
    String message;
    UserData data;
}
