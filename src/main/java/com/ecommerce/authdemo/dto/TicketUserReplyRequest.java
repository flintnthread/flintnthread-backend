package com.ecommerce.authdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketUserReplyRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Message is required")
    private String message;
}
