package com.extractor.unraveldocs.auth.repository;

import com.extractor.unraveldocs.auth.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVerificationRepository extends JpaRepository<UserVerification, String> {
}
