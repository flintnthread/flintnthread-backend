package com.ecommerce.authdemo.dto;

import lombok.Data;

@Data
    public class PlaceOrderRequestDTO {

        private Long addressId;
        private String paymentMethod; // COD / RAZORPAY
        private String orderNotes;

    }

