package com.extractor.unraveldocs.auth.model;

import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Table(name = "verification_status")
@NoArgsConstructor
@AllArgsConstructor
public class VerificationStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column
    private boolean emailVerified = false;

    @Column
    private String emailVerificationToken;

    @Enumerated(EnumType.STRING)
    @Column
    private VerifiedStatus status = VerifiedStatus.PENDING;

    @Column
    private Date emailVerificationTokenExpiry;

    @Column
    private String passwordResetToken;

    @Column
    private Date passwordResetTokenExpiry;
}
