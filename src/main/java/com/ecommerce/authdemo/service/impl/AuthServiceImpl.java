package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.Enum.Role;
import com.ecommerce.authdemo.dto.LoginRequestDTO;
import com.ecommerce.authdemo.dto.OtpResponseDTO;
import com.ecommerce.authdemo.dto.VerifyOtpDTO;
import com.ecommerce.authdemo.dto.AuthResponseDTO;
import com.ecommerce.authdemo.entity.Otp;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.repository.OtpRepository;
import com.ecommerce.authdemo.repository.UserRepository;
import com.ecommerce.authdemo.security.JwtUtil;
import com.ecommerce.authdemo.service.AuthService;
import com.ecommerce.authdemo.util.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpUtil otpUtil;

    @Override
    @Transactional
    public String sendOtp(LoginRequestDTO dto) {

        String identifier;

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            identifier = dto.getEmail();
        }
        else if (dto.getMobile() != null && !dto.getMobile().isEmpty()) {

            if (!dto.getMobile().matches("^[6-9]\\d{9}$")) {
                throw new RuntimeException("Invalid mobile number");
            }

            identifier = dto.getMobile();
        }
        else {
            throw new RuntimeException("Email or Mobile required");
        }

        User user = userRepository.findByEmail(identifier)
                .orElse(userRepository.findByContactNumber(identifier).orElse(null));

        if (user == null) {

            user = new User();
            user.setEmail(dto.getEmail());
            user.setContactNumber(dto.getMobile());
            user.setVerified(false);
            user.setRole(Role.USER);

            userRepository.save(user);
        }

        Optional<Otp> existingOtp =
                otpRepository.findTopByIdentifierOrderByExpiryTimeDesc(identifier);

        if (existingOtp.isPresent()) {

            if (existingOtp.get()
                    .getExpiryTime()
                    .minusMinutes(4)
                    .isAfter(LocalDateTime.now())) {

                throw new RuntimeException("Please wait before requesting another OTP");
            }
        }

        String otp = otpUtil.generateOtp();

        otpRepository.deleteByIdentifier(identifier);

        Otp otpEntity = new Otp();
        otpEntity.setIdentifier(identifier);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpEntity.setAttempts(0);

        otpRepository.save(otpEntity);

        System.out.println("Generated OTP: " + otp);

        // RETURN OTP
        return otp;
    }

    @Override
    @Transactional
    public AuthResponseDTO verifyOtp(VerifyOtpDTO dto) {

        String identifier;

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            identifier = dto.getEmail();
        } else if (dto.getMobile() != null && !dto.getMobile().isEmpty()) {
            identifier = dto.getMobile();
        } else {
            throw new RuntimeException("Email or Mobile required");
        }

        Otp otpEntity = otpRepository
                .findTopByIdentifierOrderByExpiryTimeDesc(identifier)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otpEntity.getAttempts() >= 5) {
            throw new RuntimeException("Too many attempts. Try again later.");
        }

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP Expired");
        }

        if (!otpEntity.getOtp().equals(dto.getOtp())) {

            otpEntity.setAttempts(otpEntity.getAttempts() + 1);
            otpRepository.save(otpEntity);

            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(identifier)
                .orElse(userRepository.findByContactNumber(identifier)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        user.setVerified(true);

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        userRepository.save(user);

        otpRepository.deleteByIdentifier(identifier);

        String token = jwtUtil.generateToken(user);

        return new AuthResponseDTO(token, user.getRole().name());
    }
}


