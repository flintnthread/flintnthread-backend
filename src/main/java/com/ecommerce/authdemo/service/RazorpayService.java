package com.ecommerce.authdemo.service;

import org.json.JSONObject;

public interface RazorpayService {

    JSONObject createOrder(Double amount);

    boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
}