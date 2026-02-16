package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Violation;
import com.example.demo.repository.ViolationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * VIOLATION SERVICE - Business logic for managing traffic violations.
 * =====================================================================
 *
 * This is the most important service - it provides data for both
 * the Admin Dashboard (system-wide stats) and User Dashboard (per-user stats).
 *
 * Methods are grouped into:
 *   1. CRUD operations (get, save violations)
 *   2. Admin Dashboard stats (total counts, total amounts)
 *   3. User Dashboard stats (per-user counts, per-user amounts)
 */
@Service
public class ViolationService {

    private final ViolationRepository violationRepository;

    /** Constructor injection */
    public ViolationService(ViolationRepository violationRepository) {
        this.violationRepository = violationRepository;
    }

    // ============================
    // CRUD OPERATIONS
    // ============================

    /** Get all violations in the system - used in Admin's "All Violations" page */
    public List<Violation> getAllViolations() {
        return violationRepository.findAll();
    }

    /** Get all violations for a specific user's vehicles - used in User Dashboard */
    public List<Violation> getViolationsByUser(User user) {
        return violationRepository.findByVehicleUser(user);
    }

    /** Get 5 most recent violations - used in Admin Dashboard's "Recent Violations" table */
    public List<Violation> getRecentViolations() {
        return violationRepository.findTop5ByOrderByViolationDateDesc();
    }

    /** Get a single violation by ID - used when marking a violation as "PAID" */
    public Optional<Violation> getViolationById(Long id) {
        return violationRepository.findById(id);
    }

    /** Save a new violation or update existing - used in Admin's Add Violation form */
    public Violation saveViolation(Violation violation) {
        return violationRepository.save(violation);
    }

    // ============================
    // ADMIN DASHBOARD STATISTICS
    // ============================

    /** Total number of violations in the system */
    public long getTotalViolations() {
        return violationRepository.count();
    }

    /** Count of violations with status = PENDING (unpaid fines) */
    public long getPendingCount() {
        return violationRepository.countByStatus(Violation.Status.PENDING);
    }

    /** Count of violations with status = PAID */
    public long getPaidCount() {
        return violationRepository.countByStatus(Violation.Status.PAID);
    }

    /** Sum of ALL fine amounts (both pending + paid) - for "Total Fine Amount" card */
    public Double getTotalFinesAmount() {
        return violationRepository.getTotalFinesAmount();
    }

    /** Sum of PAID fine amounts only - for "Collected Amount" card */
    public Double getTotalPaidAmount() {
        return violationRepository.getTotalPaidAmount();
    }

    // ============================
    // USER DASHBOARD STATISTICS
    // ============================

    /** Total fines for a specific user's vehicles - for "Total Fines" card */
    public Double getTotalFinesByUser(User user) {
        return violationRepository.getTotalFinesByUser(user);
    }

    /** Paid fines for a specific user's vehicles - for "Paid Amount" card */
    public Double getPaidFinesByUser(User user) {
        return violationRepository.getPaidFinesByUser(user);
    }
}
