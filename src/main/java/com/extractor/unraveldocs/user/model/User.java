package com.extractor.unraveldocs.user.model;

import com.extractor.unraveldocs.auth.enums.RoleName;
import com.extractor.unraveldocs.auth.model.Role;
import com.extractor.unraveldocs.auth.model.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private boolean isActive = false;

    @Column(nullable = false)
    private boolean isVerified = false;

    @OneToOne
    @JoinColumn(name = "verification_status_id", referencedColumnName = "id")
    private VerificationStatus verificationStatus;

    @ManyToMany(fetch =  FetchType.EAGER)
    private Set<Role> roles;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}
