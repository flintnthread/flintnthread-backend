package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sellers")
@Data
public class Seller {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String firstName;
        private String lastName;

        @Column(unique = true)
        private String mobileNumber;

        @Column(unique = true)
        private String email;

        private String password;
    }



