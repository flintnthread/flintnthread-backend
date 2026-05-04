package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.AuthResponseDTO;
import com.ecommerce.authdemo.dto.Enum.Role;
import com.ecommerce.authdemo.dto.LoginRequestDTO;
import com.ecommerce.authdemo.dto.VerifyOtpDTO;
import com.ecommerce.authdemo.entity.AdminUser;
import com.ecommerce.authdemo.entity.Otp;
import com.ecommerce.authdemo.entity.Seller;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.exception.EmailSendException;
import com.ecommerce.authdemo.exception.InvalidIdentifierException;
import com.ecommerce.authdemo.exception.InvalidMobileException;
import com.ecommerce.authdemo.exception.OtpExpiredException;
import com.ecommerce.authdemo.exception.OtpNotFoundException;
import com.ecommerce.authdemo.exception.TooManyAttemptsException;
import com.ecommerce.authdemo.exception.TooManyRequestsException;
import com.ecommerce.authdemo.repository.AdminUserRepository;
import com.ecommerce.authdemo.repository.OtpRepository;
import com.ecommerce.authdemo.repository.SellerRepository;
import com.ecommerce.authdemo.repository.UserRepository;
import com.ecommerce.authdemo.security.JwtUtil;
import com.ecommerce.authdemo.service.AuthService;
import com.ecommerce.authdemo.service.EmailService;
import com.ecommerce.authdemo.service.ReferralService;
import com.ecommerce.authdemo.service.SmsService;
import com.ecommerce.authdemo.util.OtpUtil;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final AdminUserRepository adminUserRepository;
    private final OtpRepository otpRepository;
    private final JwtUtil jwtUtil;
    private final OtpUtil otpUtil;
    private final SmsService smsService;
    private final EmailService emailService;
    private final ReferralService referralService;

    /* ======================================================
       SEND OTP
    ====================================================== */
    @Override
    @Transactional
    public String sendOtp(LoginRequestDTO dto) {

        String identifier;

        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            identifier = dto.getEmail().trim().toLowerCase();

        } else if (dto.getMobile() != null &&
                !dto.getMobile().trim().isEmpty()) {

            if (!dto.getMobile().matches("^[6-9]\\d{9}$")) {
                throw new InvalidMobileException("Invalid mobile number");
            }

            identifier = dto.getMobile().trim();

        } else {
            throw new InvalidIdentifierException(
                    "Email or Mobile is required");
        }

        Optional<Otp> oldOtp =
                otpRepository.findTopByIdentifierOrderByExpiryTimeDesc(
                        identifier);

        if (oldOtp.isPresent() &&
                oldOtp.get()
                        .getExpiryTime()
                        .minusMinutes(4)
                        .isAfter(LocalDateTime.now())) {

            throw new TooManyRequestsException(
                    "Please wait before requesting another OTP");
        }

        String otp = otpUtil.generateOtp();

        otpRepository.deleteByIdentifier(identifier);

        Otp entity = new Otp();
        entity.setIdentifier(identifier);
        entity.setOtp(otp);
        entity.setAttempts(0);
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.saveAndFlush(entity);

        if (identifier.contains("@")) {

            try {
                emailService.sendOtpEmail(identifier, otp);

            } catch (MailException e) {
                throw new EmailSendException(
                        "Unable to send OTP to email");
            }

        } else {

            smsService.sendSms(
                    "+91" + identifier,
                    "<#> Your FlintnThread OTP is " + otp
            );
        }

        return identifier.contains("@") ? "EMAIL" : "SMS";
    }

    /* ======================================================
       VERIFY OTP
    ====================================================== */

    @Override
    @Transactional
    public AuthResponseDTO verifyOtp(VerifyOtpDTO dto) {

        log.info("========== VERIFY OTP START ==========");

        String identifier;

        if (dto.getEmail() != null &&
                !dto.getEmail().trim().isEmpty()) {

            identifier = dto.getEmail()
                    .trim()
                    .toLowerCase();

        } else if (dto.getMobile() != null &&
                !dto.getMobile().trim().isEmpty()) {

            identifier = dto.getMobile().trim();

        } else {
            throw new RuntimeException(
                    "Email or Mobile required");
        }

        log.info("Identifier: {}", identifier);

        Otp otpEntity =
                otpRepository
                        .findTopByIdentifierOrderByExpiryTimeDesc(
                                identifier)
                        .orElseThrow(() ->
                                new OtpNotFoundException(
                                        "OTP not found"));

        log.info("OTP row found");

        if (otpEntity.getAttempts() >= 5) {
            throw new TooManyAttemptsException(
                    "Too many attempts. Try again later.");
        }

        if (otpEntity.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            throw new OtpExpiredException(
                    "OTP expired");
        }

        String enteredOtp =
                String.valueOf(dto.getOtp()).trim();

        String savedOtp =
                String.valueOf(otpEntity.getOtp()).trim();

        log.info("Saved OTP: {}", savedOtp);
        log.info("Entered OTP: {}", enteredOtp);

        if (!savedOtp.equals(enteredOtp)) {

            otpEntity.setAttempts(
                    otpEntity.getAttempts() + 1
            );

            otpRepository.saveAndFlush(otpEntity);

            throw new RuntimeException(
                    "Invalid OTP");
        }

        log.info("OTP matched");

        Role role = Role.USER;

        if (adminUserRepository
                .findByEmail(identifier)
                .isPresent()) {

            role = Role.ADMIN;

        } else if (
                sellerRepository
                        .findByEmail(identifier)
                        .isPresent()

                        ||

                        sellerRepository
                                .findByMobileNumber(identifier)
                                .isPresent()
        ) {

            role = Role.SELLER;

        } else {

            Optional<User> user =
                    userRepository
                            .findByEmail(identifier)
                            .or(() ->
                                    userRepository
                                            .findByContactNumber(identifier)
                            );

            if (user.isEmpty()) {

                log.info("Creating new user");

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

                User savedUser = userRepository.saveAndFlush(newUser);

                // ✅ Step 2: Generate referral code
                referralService.generateCodes(
                        savedUser.getId(),
                        savedUser.getUsername()
                );
            }        }

        log.info("Deleting old OTP");

        otpRepository.deleteByIdentifier(identifier);
        otpRepository.flush();

        log.info("Generating JWT");

        String token =
                jwtUtil.generateToken(
                        identifier,
                        role.name()
                );

        log.info("========== VERIFY OTP SUCCESS ==========");

        User finalUser = userRepository
                .findByEmail(identifier)
                .or(() -> userRepository.findByContactNumber(identifier))
                .orElseThrow(() -> new RuntimeException("User not found after OTP"));

        return new AuthResponseDTO(
                token,
                role.name(),
                finalUser.getId()
        );    }
}