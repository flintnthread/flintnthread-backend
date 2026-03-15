package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.Enum.Role;
import com.ecommerce.authdemo.dto.LoginRequestDTO;
import com.ecommerce.authdemo.dto.VerifyOtpDTO;
import com.ecommerce.authdemo.dto.AuthResponseDTO;
import com.ecommerce.authdemo.entity.Otp;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.entity.Seller;
import com.ecommerce.authdemo.entity.AdminUser;
import com.ecommerce.authdemo.repository.OtpRepository;
import com.ecommerce.authdemo.repository.UserRepository;
import com.ecommerce.authdemo.repository.SellerRepository;
import com.ecommerce.authdemo.repository.AdminUserRepository;
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
    private SellerRepository sellerRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpUtil otpUtil;

    // SEND OTP
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

        return otp;
    }

    // VERIFY OTP
    @Override
    @Transactional
    public AuthResponseDTO verifyOtp(VerifyOtpDTO dto) {

        String identifier;

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            identifier = dto.getEmail();
        }
        else if (dto.getMobile() != null && !dto.getMobile().isEmpty()) {
            identifier = dto.getMobile();
        }
        else {
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