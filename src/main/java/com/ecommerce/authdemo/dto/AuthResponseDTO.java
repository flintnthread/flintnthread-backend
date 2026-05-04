package com.ecommerce.authdemo.dto;


import lombok.Data;

@Data
public class AuthResponseDTO {

        private String token;

        private String role;

        private Long userId;

        public AuthResponseDTO(String token, String role, Long userId) {
            this.token = token;
            this.role = role;
            this.userId = userId;

        }

        // getters setters
    }

