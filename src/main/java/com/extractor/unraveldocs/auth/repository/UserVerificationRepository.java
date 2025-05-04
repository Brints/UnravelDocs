package com.extractor.unraveldocs.auth.repository;

import com.extractor.unraveldocs.auth.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVerificationRepository extends JpaRepository<UserVerification, String> {
    @Query("SELECT uv FROM UserVerification uv WHERE uv.deletedAt IS NULL")
    List<UserVerification> findAllActive();

    @Query("SELECT uv FROM UserVerification uv WHERE uv.id = :id AND uv.deletedAt IS NULL")
    Optional<UserVerification> findActiveById(@Param("id") String id);
}
