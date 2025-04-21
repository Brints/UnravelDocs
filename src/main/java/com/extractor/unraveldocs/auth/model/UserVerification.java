package com.extractor.unraveldocs.auth.model;

import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_verification")
@NoArgsConstructor
@AllArgsConstructor
public class UserVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column
    private boolean emailVerified = false;

    @Column
    private String emailVerificationToken;

    //@Enumerated(EnumType.STRING)
    @Column
    private VerifiedStatus status = VerifiedStatus.PENDING;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime emailVerificationTokenExpiry;

    @Column
    private String passwordResetToken;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime passwordResetTokenExpiry;

    @CreationTimestamp
    @Column(
            nullable = false,
            updatable = false,
            name = "created_at"
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            nullable = false,
            name = "updated_at"
    )
    private LocalDateTime updatedAt;
}
