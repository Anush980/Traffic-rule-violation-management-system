package com.example.demo.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

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
        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP - Traffic Violation Management");

            String htmlContent = """
                    <div style="font-family: Arial, sans-serif; background:#f4f6f8; padding:30px;">
                        <div style="max-width:520px; margin:auto; background:white; padding:30px;
                                    border-radius:12px; box-shadow:0 4px 14px rgba(0,0,0,0.08);">

                            <h2 style="color:#2c3e50; text-align:center; margin-bottom:10px;">
                                Traffic Violation Management
                            </h2>

                            <p style="text-align:center; color:#666; font-size:14px;">
                                Password Reset Request
                            </p>

                            <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">

                            <p style="font-size:14px; color:#555;">
                                Hello,
                            </p>

                            <p style="font-size:14px; color:#555;">
                                Use the following OTP to reset your password:
                            </p>

                            <div style="text-align:center; margin:30px 0;">
                                <span style="
                                        display:inline-block;
                                        font-size:32px;
                                        letter-spacing:8px;
                                        font-weight:bold;
                                        color:#1a73e8;
                                        background:#f1f6ff;
                                        padding:12px 20px;
                                        border-radius:8px;">
                                        """ + otp + """
                                </span>
                            </div>

                            <p style="font-size:14px; color:#555;">
                                This OTP will expire in 5 minutes</b>.
                            </p>

                            <p style="font-size:13px; color:#888;">
                                If you didn’t request this password reset, you can safely ignore this email.
                            </p>

                            <hr style="border:none; border-top:1px solid #eee; margin:25px 0;">

                            <p style="font-size:12px; color:#999; text-align:center;">
                                Traffic Violation Management System
                            </p>

                        </div>
                    </div>
                    """;

            helper.setText(htmlContent, true);

            mailSender.send(message);

            // Store OTP with expiry
            otpStore.put(toEmail,
                    new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
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