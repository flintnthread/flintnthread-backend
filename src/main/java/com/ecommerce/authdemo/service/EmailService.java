package com.ecommerce.authdemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ecommerce.authdemo.entity.EmailLogStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogService emailLogService;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("flintandthread@gmail.com");
        message.setTo(toEmail);
        message.setSubject("FlintnThread Login OTP");
        message.setText(
                "Hello,\n\n" +
                        "Your OTP for FlintnThread login is: " + otp +
                        "\n\nThis OTP will expire in 5 minutes." +
                        "\n\nDo not share this OTP with anyone." +
                        "\n\nWebsite: https://flintnthread.in" +
                        "\n\nThanks,\nFlintnThread Team"
        );

        try {
            mailSender.send(message);
            emailLogService.createLog(
                    null,
                    "otp",
                    toEmail,
                    message.getSubject(),
                    EmailLogStatus.sent,
                    null
            );
            log.info("OTP Email sent to: {}", toEmail);
        } catch (MailException e) {
            emailLogService.createLog(
                    null,
                    "otp",
                    toEmail,
                    message.getSubject(),
                    EmailLogStatus.failed,
                    e.getMessage()
            );
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Unable to send OTP email");
        }
    }
}