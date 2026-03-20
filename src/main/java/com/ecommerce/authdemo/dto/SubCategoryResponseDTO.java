package com.ecommerce.authdemo.dto;

import lombok.Data;

@Data
public class SubCategoryResponseDTO {

        private Long id;
        private String name;
        private String image;

        public SubCategoryResponseDTO(Long id, String name, String image) {
            this.id = id;
            this.name = name;
            this.image = image;
        }

        // getters
    }

