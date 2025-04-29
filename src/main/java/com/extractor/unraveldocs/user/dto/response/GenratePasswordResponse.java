package com.extractor.unraveldocs.user.dto.response;

import com.extractor.unraveldocs.user.dto.GeneratedPassword;
import lombok.Data;

import java.util.Map;

@Data
public class GenratePasswordResponse {
    int statusCode;
    String status;
    String message;
    GeneratedPassword data;
}
