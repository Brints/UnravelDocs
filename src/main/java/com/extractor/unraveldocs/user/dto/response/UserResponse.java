package com.extractor.unraveldocs.user.dto.response;

import com.extractor.unraveldocs.user.dto.UserData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    int statusCode;
    String status;
    String message;
    UserData data;
}
