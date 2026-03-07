package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.AuthResponseDTO;
import com.ecommerce.authdemo.dto.LoginRequestDTO;
import com.ecommerce.authdemo.dto.VerifyOtpDTO;
import com.ecommerce.authdemo.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody LoginRequestDTO dto) {

        String otp = authService.sendOtp(dto);

        Map<String,Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent successfully");
        response.put("otp", otp);   // returning OTP for testing

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpDTO dto) {

        AuthResponseDTO authResponse = authService.verifyOtp(dto);

        Map<String,Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", authResponse.getToken());
        response.put("role", authResponse.getRole());

        return ResponseEntity.ok(response);
    }
}