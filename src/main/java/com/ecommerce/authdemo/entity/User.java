package com.ecommerce.authdemo.entity;

import com.ecommerce.authdemo.dto.Enum.Role;
import com.ecommerce.authdemo.dto.Enum.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Basic Info
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "contact_number")
    private String contactNumber;

    // 🔐 Auth
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean verified = false;

    // 🔹 Profile (NEW for mobile)
    @Column(name = "profile_image")
    private String profileImage;

    // 🔹 Status (FIXED)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.active;

    // 🔹 Optional (future ready)
    @Column(name = "company_reference_id", unique = true)
    private String companyReferenceId;

    // 🔹 Timestamps
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔁 Auto timestamps
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}