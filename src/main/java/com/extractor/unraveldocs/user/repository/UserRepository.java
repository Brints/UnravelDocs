package com.extractor.unraveldocs.user.repository;

import com.extractor.unraveldocs.user.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT CASE WHEN COUNT(u) = 0 THEN true ELSE false END FROM User u")
    boolean isFirstUserWithLock();

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") String id);

    @Query("SELECT u FROM User u WHERE u.lastLogin < :threshold AND u.deletedAt IS NULL")
    List<User> findAllByLastLoginDateBefore(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT u FROM User u WHERE u.lastLogin < :threshold AND u.deletedAt IS NULL")
    List<User> findAllByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}
