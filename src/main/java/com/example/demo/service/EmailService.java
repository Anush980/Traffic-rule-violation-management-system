package com.example.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Store OTPs with their expiry time (email -> OtpEntry)
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP - Traffic Violation Management");
        message.setText("Your OTP for password reset is: " + otp
                + "\n\nThis code is valid for " + OTP_EXPIRY_MINUTES + " minutes."
                + "\n\nIf you did not request this, please ignore this email.");

        mailSender.send(message);

        // Store OTP with expiry
        otpStore.put(toEmail, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(entry.expiryTime)) {
            otpStore.remove(email);
            return false;
        }
        if (entry.otp.equals(otp.trim())) {
            otpStore.remove(email); // OTP is single-use
            return true;
        }
        return false;
    }

    private record OtpEntry(String otp, LocalDateTime expiryTime) {}
}
