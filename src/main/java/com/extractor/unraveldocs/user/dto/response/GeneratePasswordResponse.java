package com.extractor.unraveldocs.user.dto.response;

import com.extractor.unraveldocs.user.dto.GeneratedPassword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneratePasswordResponse {
    int statusCode;
    String status;
    String message;
    GeneratedPassword data;
}
