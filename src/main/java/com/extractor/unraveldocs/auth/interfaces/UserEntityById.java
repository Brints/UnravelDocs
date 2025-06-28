package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.user.model.User;

public interface UserEntityById {
    User loadUserEntityById(String userId);
}
