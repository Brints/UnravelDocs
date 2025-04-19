package com.extractor.unraveldocs.user.model;

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

    @Column(nullable = true, name = "profile_picture")
    private String profilePicture;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, name = "last_login")
    private LocalDateTime lastLogin;

    @Column(nullable = false, name = "is_active")
    private boolean isActive = false;

    @Column(nullable = false, name = "is_verified")
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
