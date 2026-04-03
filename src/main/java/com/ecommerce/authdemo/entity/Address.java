package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private String name;
    private String email;
    private String phone;

    private String addressLine1;
    private String addressLine2;

    private String city;
    private String state;
    private String country;

    private String pincode;

    private String addressType;

    private Boolean isDefault;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}