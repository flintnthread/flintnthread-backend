package com.ecommerce.authdemo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnImageResponse {
    private Integer id;
    private Integer returnId;
    private String imagePath;
    private LocalDateTime createdAt;
}
