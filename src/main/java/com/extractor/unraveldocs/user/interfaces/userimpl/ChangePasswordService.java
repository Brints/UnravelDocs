package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;

public interface ChangePasswordService {
    UnravelDocsDataResponse<Void> changePassword(ChangePasswordDto request);
}
