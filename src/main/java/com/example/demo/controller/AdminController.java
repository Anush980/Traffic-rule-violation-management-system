package com.example.demo.controller;

import com.example.demo.model.TrafficRule;
import com.example.demo.model.Vehicle;
import com.example.demo.model.Violation;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.TrafficRuleService;
import com.example.demo.service.VehicleService;
import com.example.demo.service.ViolationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * =====================================================================
 * ADMIN CONTROLLER - Handles all admin-only pages and operations.
 * =====================================================================
 *
 * This controller is only accessible by users with ROLE_ADMIN
 * (configured in SecurityConfig.java).
 *
 * @RequestMapping("/admin") = All endpoints in this class start with /admin/
 *   So @GetMapping("/dashboard") becomes /admin/dashboard
 *
 * ENDPOINTS:
 *   GET  /admin/dashboard         --> Admin dashboard with analytics
 *   GET  /admin/rules             --> List all traffic rules + add form
 *   POST /admin/rules/add         --> Add a new traffic rule
 *   POST /admin/rules/delete/{id} --> Delete a traffic rule
 *   GET  /admin/violations        --> List all violations
 *   GET  /admin/violations/add    --> Show "add violation" form
 *   POST /admin/violations/add    --> Record a new violation
 *   POST /admin/violations/mark-paid/{id} --> Mark a violation as paid
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    // Services and repositories needed for admin operations
    private final TrafficRuleService trafficRuleService;
    private final ViolationService violationService;
    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;


       @Value("${app.name}")
    private String appName;
    
    /** Constructor injection - Spring provides all 5 dependencies */
    public AdminController(TrafficRuleService trafficRuleService, ViolationService violationService,
                           VehicleService vehicleService, VehicleRepository vehicleRepository,
                           UserRepository userRepository) {
        this.trafficRuleService = trafficRuleService;
        this.violationService = violationService;
        this.vehicleService = vehicleService;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("appName", appName);
    }

    // ================================================================
    //  DASHBOARD - Shows overview analytics for admin
    // ================================================================

    /**
     * ADMIN DASHBOARD PAGE
     * URL: GET /admin/dashboard
     *
     * Passes various statistics to the template for display:
     *   - Total violations, pending/paid counts
     *   - Total fine amount, collected amount
     *   - Number of registered users and vehicles
     *   - 5 most recent violations
     *
     * model.addAttribute("key", value):
     *   Passes data to the Thymeleaf template. In the HTML,
     *   you access it with th:text="${key}"
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalViolations", violationService.getTotalViolations());
        model.addAttribute("pendingFines", violationService.getPendingCount());
        model.addAttribute("paidFines", violationService.getPaidCount());
        model.addAttribute("totalAmount", violationService.getTotalFinesAmount());
        model.addAttribute("collectedAmount", violationService.getTotalPaidAmount());
        model.addAttribute("totalUsers", userRepository.countByRole(com.example.demo.model.User.Role.USER));
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("recentViolations", violationService.getRecentViolations());
        return "admin/dashboard";   // Renders templates/admin/dashboard.html
    }

    // ================================================================
    //  TRAFFIC RULES CRUD - Add, List, Delete rules
    // ================================================================

    /**
     * LIST ALL RULES PAGE
     * URL: GET /admin/rules
     *
     * Shows all existing rules in a table + an "Add New Rule" form.
     * "newRule" is an empty TrafficRule object for form binding.
     */
    @GetMapping("/rules")
    public String listRules(Model model) {
        model.addAttribute("rules", trafficRuleService.getAllRules());
        model.addAttribute("newRule", new TrafficRule());  // Empty object for the add form
        return "admin/rules";   // Renders templates/admin/rules.html
    }

    /**
     * ADD NEW RULE
     * URL: POST /admin/rules/add
     *
     * @ModelAttribute TrafficRule rule:
     *   Spring fills this object from the form fields (ruleName, description, fineAmount)
     *
     * After saving, redirects back to the rules page with a success message.
     */
    @PostMapping("/rules/add")
    public String addRule(@ModelAttribute TrafficRule rule, RedirectAttributes redirectAttributes) {
        trafficRuleService.saveRule(rule);
        redirectAttributes.addFlashAttribute("success", "Traffic rule added successfully!");
        return "redirect:/admin/rules";
    }

    /**
     * DELETE A RULE
     * URL: POST /admin/rules/delete/{id}
     *
     * @PathVariable Long id:
     *   Extracts the {id} from the URL path.
     *   Example: POST /admin/rules/delete/3  -->  id = 3
     */
    @PostMapping("/rules/delete/{id}")
    public String deleteRule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        trafficRuleService.deleteRule(id);
        redirectAttributes.addFlashAttribute("success", "Traffic rule deleted!");
        return "redirect:/admin/rules";
    }

    // ================================================================
    //  VIOLATIONS - Record new violations, list all, mark as paid
    // ================================================================

    /**
     * LIST ALL VIOLATIONS PAGE
     * URL: GET /admin/violations
     *
     * Shows every violation in the system with vehicle info, rule, fine, status.
     */
    @GetMapping("/violations")
    public String listViolations(Model model) {
        model.addAttribute("violations", violationService.getAllViolations());
        return "admin/violations";   // Renders templates/admin/violations.html
    }

    /**
     * ADD VIOLATION FORM
     * URL: GET /admin/violations/add
     *
     * Shows a form to record a new violation.
     * Dropdown menus are populated with:
     *   - All registered vehicles (to select which vehicle was caught)
     *   - All traffic rules (to select which rule was violated)
     */
    @GetMapping("/violations/add")
    public String addViolationForm(Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("rules", trafficRuleService.getAllRules());
        return "admin/add-violation";   // Renders templates/admin/add-violation.html
    }

    /**
     * RECORD A NEW VIOLATION
     * URL: POST /admin/violations/add
     *
     * @RequestParam = Extracts individual form fields (instead of binding to an object)
     *   vehicleId   = ID of the vehicle selected from dropdown
     *   ruleId      = ID of the traffic rule selected from dropdown
     *   location    = Where the violation happened
     *   description = Brief description
     *
     * FLOW:
     *   1. Look up the Vehicle and TrafficRule from database by their IDs
     *   2. If either is invalid, show error and redirect back
     *   3. Create a new Violation object, set all fields
     *   4. Set date to NOW and status to PENDING
     *   5. Save to database and redirect to violations list
     */
    @PostMapping("/violations/add")
    public String addViolation(@RequestParam Long vehicleId, @RequestParam Long ruleId,
                               @RequestParam String location, @RequestParam String description,
                               RedirectAttributes redirectAttributes) {
        // Look up vehicle and rule from database
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        TrafficRule rule = trafficRuleService.getRuleById(ruleId).orElse(null);

        // Validate that both exist
        if (vehicle == null || rule == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid vehicle or rule selected!");
            return "redirect:/admin/violations/add";
        }

        // Create and populate the violation
        Violation violation = new Violation();
        violation.setVehicle(vehicle);          // Link to the vehicle
        violation.setTrafficRule(rule);          // Link to the traffic rule
        violation.setLocation(location);
        violation.setDescription(description);
        violation.setViolationDate(LocalDateTime.now());   // Current date/time
        violation.setStatus(Violation.Status.PENDING);     // Fine not yet paid

        // Save to database
        violationService.saveViolation(violation);
        redirectAttributes.addFlashAttribute("success", "Violation recorded successfully!");
        return "redirect:/admin/violations";
    }

    /**
     * MARK VIOLATION AS PAID
     * URL: POST /admin/violations/mark-paid/{id}
     *
     * Changes the violation status from PENDING to PAID.
     * ifPresent() = only execute if the violation exists (safety check)
     */
    @PostMapping("/violations/mark-paid/{id}")
    public String markAsPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        violationService.getViolationById(id).ifPresent(v -> {
            v.setStatus(Violation.Status.PAID);     // Change status to PAID
            violationService.saveViolation(v);       // Update in database
        });
        redirectAttributes.addFlashAttribute("success", "Violation marked as paid!");
        return "redirect:/admin/violations";
    }
}
