package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.Enum.Role;
import com.ecommerce.authdemo.dto.LoginRequestDTO;
import com.ecommerce.authdemo.dto.VerifyOtpDTO;
import com.ecommerce.authdemo.dto.AuthResponseDTO;
import com.ecommerce.authdemo.entity.Otp;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.entity.Seller;
import com.ecommerce.authdemo.entity.AdminUser;
import com.ecommerce.authdemo.exception.*;
import com.ecommerce.authdemo.repository.OtpRepository;
import com.ecommerce.authdemo.repository.UserRepository;
import com.ecommerce.authdemo.repository.SellerRepository;
import com.ecommerce.authdemo.repository.AdminUserRepository;
import com.ecommerce.authdemo.security.JwtUtil;
import com.ecommerce.authdemo.service.AuthService;
import com.ecommerce.authdemo.service.EmailService;
import com.ecommerce.authdemo.service.SmsService;
import com.ecommerce.authdemo.util.OtpUtil;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final AdminUserRepository adminUserRepository;
    private final OtpRepository otpRepository;
    private final JwtUtil jwtUtil;
    private final OtpUtil otpUtil;
    private final SmsService smsService;
    private final EmailService emailService;

    // 🔥 SEND OTP
    @Override
    @Transactional
    public String sendOtp(LoginRequestDTO dto) {

        String identifier;

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            identifier = dto.getEmail();
        } else if (dto.getMobile() != null && !dto.getMobile().isEmpty()) {

            if (!dto.getMobile().matches("^[6-9]\\d{9}$")) {
                throw new InvalidMobileException("Invalid mobile number");
            }

            identifier = dto.getMobile();
        } else {
            throw new InvalidIdentifierException("Email or Mobile is required");
        }

        Optional<Otp> existingOtp =
                otpRepository.findTopByIdentifierOrderByExpiryTimeDesc(identifier);

        if (existingOtp.isPresent() &&
                existingOtp.get().getExpiryTime().minusMinutes(4).isAfter(LocalDateTime.now())) {
            throw new TooManyRequestsException("Please wait before requesting another OTP");
        }

        String otp = otpUtil.generateOtp();

        otpRepository.deleteByIdentifier(identifier);

        Otp otpEntity = new Otp();
        otpEntity.setIdentifier(identifier);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpEntity.setAttempts(0);
        otpRepository.save(otpEntity);

        if (identifier.contains("@")) {
            try {
                log.info("Sending OTP {} to email {}", otp, identifier);
                emailService.sendOtpEmail(identifier, otp);
            } catch (MailException e) {
                throw new EmailSendException("Unable to send OTP to email");
            }
        } else {
            String message = "<#> Your FlintnThread OTP is " + otp;
            smsService.sendSms("+91" + identifier, message);
        }

        return identifier.contains("@") ? "EMAIL" : "SMS";
    }

    // 🔥 VERIFY OTP
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

        Optional<Otp> otpOptional =
                otpRepository.findTopByIdentifierOrderByExpiryTimeDesc(identifier);

        if (otpOptional.isEmpty()) {
            throw new OtpNotFoundException("OTP not found or expired");
        }

        Otp otpEntity = otpOptional.get();

        if (otpEntity.getAttempts() >= 5) {
            throw new TooManyAttemptsException("Too many attempts. Try again later.");
        }

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP expired");
        }

        // ✅ SAFE OTP VALIDATION
        if (dto.getOtp() == null) {
            throw new RuntimeException("OTP is required");
        }

        if (!otpEntity.getOtp().equals(String.valueOf(dto.getOtp()))) {

            otpEntity.setAttempts(otpEntity.getAttempts() + 1);
            otpRepository.save(otpEntity);

            throw new RuntimeException("Invalid OTP");
        }

        Role role;

        Optional<AdminUser> admin =
                adminUserRepository.findByEmail(identifier);

        if (admin.isPresent()) {
            role = Role.ADMIN;
        } else {

            Optional<Seller> seller =
                    sellerRepository.findByEmail(identifier)
                            .or(() -> sellerRepository.findByMobileNumber(identifier));

            if (seller.isPresent()) {
                role = Role.SELLER;
            } else {

                Optional<User> user =
                        userRepository.findByEmail(identifier)
                                .or(() -> userRepository.findByContactNumber(identifier));

                if (user.isPresent()) {
                    role = Role.USER;
                } else {

                    User newUser = new User();

                    if (identifier.contains("@")) {
                        newUser.setEmail(identifier);
                        newUser.setUsername(identifier);
                    } else {
                        newUser.setContactNumber(identifier);
                        newUser.setUsername(identifier);
                    }

                    newUser.setVerified(true);
                    newUser.setRole(Role.USER);

                    userRepository.save(newUser);

                    role = Role.USER;
                }
            }
        }

        otpRepository.deleteByIdentifier(identifier);

        String token = jwtUtil.generateToken(identifier, role.name());

        return new AuthResponseDTO(token, role.name());
    }
}