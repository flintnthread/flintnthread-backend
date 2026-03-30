package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.AuthResponseDTO;
import com.ecommerce.authdemo.dto.LoginRequestDTO;
import com.ecommerce.authdemo.dto.VerifyOtpDTO;
import com.ecommerce.authdemo.exception.*;
import com.ecommerce.authdemo.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

        Map<String, Object> response = new HashMap<>();

        try {
            // Generate OTP
            String deliveryChannel = authService.sendOtp(dto); // returns "SMS" or "EMAIL"

            response.put("success", true);
            response.put("message", "OTP sent successfully via " + deliveryChannel);

            return ResponseEntity.ok(response);

        } catch (InvalidMobileException | InvalidEmailException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (TooManyRequestsException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Something went wrong: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpDTO dto) {

        try {

            AuthResponseDTO authResponse = authService.verifyOtp(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", authResponse.getToken());
            response.put("role", authResponse.getRole());

            return ResponseEntity.ok(response);

        } catch (OtpNotFoundException e) {

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (OtpExpiredException e) {

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.GONE).body(error);

        } catch (TooManyAttemptsException e) {

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);

        } catch (Exception e) {

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Something went wrong");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}