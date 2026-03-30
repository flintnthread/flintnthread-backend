package com.ecommerce.authdemo.entity;

import com.ecommerce.authdemo.dto.Enum.UserStatus;
import jakarta.persistence.*;
import com.ecommerce.authdemo.dto.Enum.Role;
import lombok.Data;


@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    @Column(name = "contact_number")
    private String contactNumber;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean verified;

    public void setStatus(UserStatus inactive) {
    }

    // getters setters
}