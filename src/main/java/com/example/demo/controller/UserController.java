package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Vehicle;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.VehicleService;
import com.example.demo.service.ViolationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * =====================================================================
 * USER CONTROLLER - Handles all user-only pages and operations.
 * =====================================================================
 *
 * This controller is only accessible by users with ROLE_USER
 * (configured in SecurityConfig.java).
 *
 * @RequestMapping("/user") = All endpoints start with /user/
 *
 * ENDPOINTS:
 *   GET  /user/dashboard     --> User dashboard (vehicles, violations, fines summary)
 *   GET  /user/vehicles      --> List user's vehicles + add vehicle form
 *   POST /user/vehicles/add  --> Register a new vehicle
 *   GET  /user/violations    --> List all violations for user's vehicles
 *
 * What is Authentication?
 *   Spring Security provides the Authentication object for every request.
 *   It contains the logged-in user's email (getName()) and role.
 *   We use it to find the current user in the database.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final VehicleService vehicleService;
    private final ViolationService violationService;
    private final PasswordEncoder passwordEncoder;

    /** Constructor injection */
    public UserController(UserRepository userRepository, VehicleService vehicleService,
                          ViolationService violationService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vehicleService = vehicleService;
        this.violationService = violationService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * HELPER METHOD - Gets the currently logged-in User entity from database.
     *
     * authentication.getName() returns the email (we configured email as username
     * in SecurityConfig with .usernameParameter("email")).
     *
     * This is called in every endpoint to know which user is making the request.
     */
    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    // ================================================================
    //  DASHBOARD - Shows user's personal overview
    // ================================================================

    /**
     * USER DASHBOARD PAGE
     * URL: GET /user/dashboard
     *
     * Shows:
     *   - Welcome message with user's name
     *   - Number of registered vehicles
     *   - Number of violations
     *   - Total fine amount and paid amount
     *   - List of all violations
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Vehicle> vehicles = vehicleService.getVehiclesByUser(user);

        model.addAttribute("user", user);                                          // User info for welcome msg
        model.addAttribute("totalVehicles", vehicles.size());                      // Count of user's vehicles
        model.addAttribute("totalViolations", violationService.getViolationsByUser(user).size());  // Violation count
        model.addAttribute("totalFines", violationService.getTotalFinesByUser(user));    // Total fine Rs.
        model.addAttribute("paidFines", violationService.getPaidFinesByUser(user));      // Paid amount Rs.
        model.addAttribute("recentViolations", violationService.getViolationsByUser(user)); // All violations
        return "user/dashboard";   // Renders templates/user/dashboard.html
    }

    // ================================================================
    //  VEHICLES - Register and view user's vehicles
    // ================================================================

    /**
     * MY VEHICLES PAGE
     * URL: GET /user/vehicles
     *
     * Shows list of user's registered vehicles + form to add new one.
     * "newVehicle" = empty Vehicle object for form binding
     * "vehicleTypes" = array of VehicleType enum values for dropdown
     */
    @GetMapping("/vehicles")
    public String myVehicles(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("vehicles", vehicleService.getVehiclesByUser(user));
        model.addAttribute("newVehicle", new Vehicle());                  // Empty vehicle for add form
        model.addAttribute("vehicleTypes", Vehicle.VehicleType.values()); // CAR, BIKE, TRUCK, AUTO, BUS
        return "user/vehicles";   // Renders templates/user/vehicles.html
    }

    /**
     * ADD NEW VEHICLE
     * URL: POST /user/vehicles/add
     *
     * FLOW:
     *   1. Get the current logged-in user
     *   2. Check if vehicle registration number already exists (prevent duplicates)
     *   3. Link the vehicle to the current user (vehicle.setUser(user))
     *   4. Save to database
     *   5. Redirect back to vehicles page with success/error message
     */
    @PostMapping("/vehicles/add")
    public String addVehicle(@ModelAttribute Vehicle vehicle, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);

        // Check for duplicate registration number
        if (vehicleService.existsByRegistrationNumber(vehicle.getRegistrationNumber())) {
            redirectAttributes.addFlashAttribute("error", "Vehicle with this registration number already exists!");
            return "redirect:/user/vehicles";
        }

        // Link vehicle to current user and save
        vehicle.setUser(user);
        vehicleService.saveVehicle(vehicle);
        redirectAttributes.addFlashAttribute("success", "Vehicle added successfully!");
        return "redirect:/user/vehicles";
    }

    // ================================================================
    //  VIOLATIONS - View user's traffic violations
    // ================================================================

    // ================================================================
    //  CHANGE PASSWORD - From user profile
    // ================================================================

    @GetMapping("/change-password")
    public String changePasswordPage(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("user", user);
        return "user/change-password";
    }

    /**
     * PROCESS CHANGE PASSWORD
     * Validates current password, then applies same medium-difficulty rules to new password.
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect!");
            return "redirect:/user/change-password";
        }

        // Validate new passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match!");
            return "redirect:/user/change-password";
        }

        // Validate new password is not same as email
        if (newPassword.equalsIgnoreCase(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Password cannot be the same as your email!");
            return "redirect:/user/change-password";
        }

        // Validate new password is different from current password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "New password must be different from the current password!");
            return "redirect:/user/change-password";
        }

        // Validate password medium difficulty: min 8 chars, 1 uppercase, 1 lowercase, 1 digit
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long!");
            return "redirect:/user/change-password";
        }
        if (!newPassword.matches(".*[A-Z].*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one uppercase letter!");
            return "redirect:/user/change-password";
        }
        if (!newPassword.matches(".*[a-z].*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one lowercase letter!");
            return "redirect:/user/change-password";
        }
        if (!newPassword.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute("error", "Password must contain at least one digit!");
            return "redirect:/user/change-password";
        }

        // Save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        return "redirect:/user/change-password";
    }

    /**
     * MY VIOLATIONS PAGE
     * URL: GET /user/violations
     *
     * Shows all violations for the current user's vehicles with:
     *   - Summary cards (total fines, paid, pending)
     *   - Detailed table of each violation
     */
    @GetMapping("/violations")
    public String myViolations(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("violations", violationService.getViolationsByUser(user));
        model.addAttribute("totalFines", violationService.getTotalFinesByUser(user));
        model.addAttribute("paidFines", violationService.getPaidFinesByUser(user));
        return "user/violations";   // Renders templates/user/violations.html
    }
}
