package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * =====================================================================
 * AUTH CONTROLLER - Handles Login, Registration, and Forgot Password.
 * =====================================================================
 *
 * ENDPOINTS:
 *   GET  /                --> Redirects to /login
 *   GET  /login           --> Shows the login page
 *   GET  /register        --> Shows the registration form
 *   POST /register        --> Processes registration form submission
 *   GET  /forgot-password --> Shows forgot password form
 *   POST /forgot-password --> Verifies email + phone
 *   GET  /reset-password  --> Shows reset password form
 *   POST /reset-password  --> Saves new password
 */
@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.name}")
    private String appName;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("appName", appName);
    }

    @GetMapping("/")
    public String home() {
        
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * PROCESS REGISTRATION - Validates and saves a new user.
     *
     * Validations:
     *   1. Email must not already be registered
     *   2. Phone must be exactly 10 digits
     *   3. Password must meet medium difficulty (8+ chars, 1 uppercase, 1 lowercase, 1 digit)
     *   4. Password must not be the same as email
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        // Check if email is already taken
        if (userRepository.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already registered!");
            return "redirect:/register";
        }

        // Validate phone number - must be exactly 10 digits
        if (user.getPhone() == null || !user.getPhone().matches("^\\d{10}$")) {
            redirectAttributes.addFlashAttribute("error", "Phone number must be exactly 10 digits!");
            return "redirect:/register";
        }

        // Validate password is not same as email
        if (user.getPassword().equalsIgnoreCase(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Password cannot be the same as your email!");
            return "redirect:/register";
        }

        // Validate password medium difficulty: min 8 chars, 1 uppercase, 1 lowercase, 1 digit
        String password = user.getPassword();
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long!");
            return "redirect:/register";
        }
        if (!password.matches(".*[A-Z].*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one uppercase letter!");
            return "redirect:/register";
        }
        if (!password.matches(".*[a-z].*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one lowercase letter!");
            return "redirect:/register";
        }
        if (!password.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one digit!");
            return "redirect:/register";
        }

        // All validations passed - hash password, set role, and save
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.USER);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login";
    }

    // ================================================================
    //  FORGOT PASSWORD - 3-step flow:
    //  Step 1: Enter email OR phone to find account
    //  Step 2: Enter verification code (demo code: 122333)
    //  Step 3: Set new password
    // ================================================================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    /**
     * STEP 1: Find account by email OR phone.
     * If found, generate OTP and send it to the user's email.
     */
    @PostMapping("/forgot-password")
    public String findAccount(@RequestParam String identifier,
                              RedirectAttributes redirectAttributes) {
        String trimmed = identifier.trim();

        // Try to find user by email or phone
        java.util.Optional<User> userOpt;
        if (trimmed.matches("^\\d{10}$")) {
            userOpt = userRepository.findByPhone(trimmed);
        } else if (trimmed.contains("@")) {
            userOpt = userRepository.findByEmail(trimmed);
        } else {
            redirectAttributes.addFlashAttribute("error", "Please enter a valid email address or a 10-digit phone number!");
            return "redirect:/forgot-password";
        }

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No account found with this email or phone number!");
            return "redirect:/forgot-password";
        }

        // Account found - generate OTP and send via email
        String userEmail = userOpt.get().getEmail();
        try {
            String otp = emailService.generateOtp();
            emailService.sendOtpEmail(userEmail, otp);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send OTP email. Please try again later.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("userEmail", userEmail);
        redirectAttributes.addFlashAttribute("success", "A verification code has been sent to your email!");
        return "redirect:/verify-code";
    }

    /**
     * STEP 2: Show verification code entry page.
     */
    @GetMapping("/verify-code")
    public String verifyCodePage(Model model) {
        if (!model.containsAttribute("userEmail")) {
            return "redirect:/forgot-password";
        }
        return "verify-code";
    }

    /**
     * STEP 2: Verify the entered OTP code.
     */
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String email, @RequestParam String code,
                             RedirectAttributes redirectAttributes) {
        if (!emailService.verifyOtp(email, code)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired verification code! Please try again.");
            redirectAttributes.addFlashAttribute("userEmail", email);
            return "redirect:/verify-code";
        }

        // Code verified - go to reset password page
        redirectAttributes.addFlashAttribute("verifiedEmail", email);
        return "redirect:/reset-password";
    }

    /**
     * STEP 3: Show reset password form.
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(Model model) {
        if (!model.containsAttribute("verifiedEmail")) {
            return "redirect:/forgot-password";
        }
        return "reset-password";
    }

    /**
     * STEP 3: Save new password after code verification.
     */
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
            redirectAttributes.addFlashAttribute("verifiedEmail", email);
            return "redirect:/reset-password";
        }

        // Validate password not same as email
        if (newPassword.equalsIgnoreCase(email)) {
            redirectAttributes.addFlashAttribute("error", "Password cannot be the same as your email!");
            redirectAttributes.addFlashAttribute("verifiedEmail", email);
            return "redirect:/reset-password";
        }

        // Validate password medium difficulty
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long!");
            redirectAttributes.addFlashAttribute("verifiedEmail", email);
            return "redirect:/reset-password";
        }
        if (!newPassword.matches(".*[A-Z].*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one uppercase letter!");
            redirectAttributes.addFlashAttribute("verifiedEmail", email);
            return "redirect:/reset-password";
        }
        if (!newPassword.matches(".*[a-z].*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one lowercase letter!");
            redirectAttributes.addFlashAttribute("verifiedEmail", email);
            return "redirect:/reset-password";
        }
        if (!newPassword.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one digit!");
            redirectAttributes.addFlashAttribute("verifiedEmail", email);
            return "redirect:/reset-password";
        }

        // Find user and update password
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/forgot-password";
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Password reset successful! Please login with your new password.");
        return "redirect:/login";
    }
}
