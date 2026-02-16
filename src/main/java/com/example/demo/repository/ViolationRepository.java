package com.example.demo.repository;

import com.example.demo.model.User;
import com.example.demo.model.Vehicle;
import com.example.demo.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * =====================================================================
 * VIOLATION REPOSITORY - Database operations for the "violations" table.
 * =====================================================================
 *
 * This is the most complex repository because the Admin Dashboard needs
 * various statistics (totals, sums, counts) and the User Dashboard needs
 * filtered data for the logged-in user only.
 *
 * Two types of queries used here:
 *   1. DERIVED QUERIES  - Spring generates SQL from method name
 *      Example: findByVehicle(vehicle) --> SELECT * FROM violations WHERE vehicle_id = ?
 *
 *   2. CUSTOM JPQL QUERIES - We write the query ourselves using @Query
 *      Used when the query is too complex for a method name (like SUM calculations)
 *      JPQL = Java Persistence Query Language (similar to SQL but uses entity names)
 */
public interface ViolationRepository extends JpaRepository<Violation, Long> {

    /**
     * Get all violations for a specific vehicle.
     * Generated SQL: SELECT * FROM violations WHERE vehicle_id = ?
     */
    List<Violation> findByVehicle(Vehicle vehicle);

    /**
     * Get violations for multiple vehicles at once.
     * "In" = SQL's "IN" clause.
     * Generated SQL: SELECT * FROM violations WHERE vehicle_id IN (?, ?, ?)
     */
    List<Violation> findByVehicleIn(List<Vehicle> vehicles);

    /**
     * Get all violations for vehicles belonging to a specific user.
     * Spring understands "VehicleUser" as: violation -> vehicle -> user
     * Generated SQL: SELECT v.* FROM violations v
     *                JOIN vehicles vh ON v.vehicle_id = vh.id
     *                WHERE vh.user_id = ?
     *
     * Used in: User Dashboard to show "My Violations"
     */
    List<Violation> findByVehicleUser(User user);

    /**
     * Count violations by their payment status (PENDING or PAID).
     * Used in Admin Dashboard for the stats cards.
     *
     * Example: countByStatus(Status.PENDING) --> how many unpaid fines
     * Generated SQL: SELECT COUNT(*) FROM violations WHERE status = ?
     */
    long countByStatus(Violation.Status status);

    // ============================================================
    // CUSTOM JPQL QUERIES (for complex calculations like SUM)
    // ============================================================
    // @Query annotation lets us write JPQL (like SQL, but uses Java entity names)
    // COALESCE(value, 0) = if value is NULL (no violations), return 0 instead

    /**
     * Calculate total fine amount across ALL violations in the system.
     * Used in Admin Dashboard: "Total Fine Amount" card.
     *
     * JPQL explained:
     *   SUM(v.trafficRule.fineAmount)  = add up fineAmount from each violation's linked rule
     *   COALESCE(..., 0)               = return 0 if there are no violations
     */
    @Query("SELECT COALESCE(SUM(v.trafficRule.fineAmount), 0) FROM Violation v")
    Double getTotalFinesAmount();

    /**
     * Calculate total amount from PAID violations only.
     * Used in Admin Dashboard: "Collected Amount" card.
     */
    @Query("SELECT COALESCE(SUM(v.trafficRule.fineAmount), 0) FROM Violation v WHERE v.status = 'PAID'")
    Double getTotalPaidAmount();

    /**
     * Calculate total fine amount for a specific user's vehicles.
     * Used in User Dashboard: "Total Fines" card.
     *
     * :user = The method parameter (Spring binds it automatically)
     */
    @Query("SELECT COALESCE(SUM(v.trafficRule.fineAmount), 0) FROM Violation v WHERE v.vehicle.user = :user")
    Double getTotalFinesByUser(User user);

    /**
     * Calculate paid amount for a specific user's vehicles.
     * Used in User Dashboard: "Paid Amount" card.
     */
    @Query("SELECT COALESCE(SUM(v.trafficRule.fineAmount), 0) FROM Violation v WHERE v.vehicle.user = :user AND v.status = 'PAID'")
    Double getPaidFinesByUser(User user);

    /**
     * Get the 5 most recent violations (sorted by date, newest first).
     * Used in Admin Dashboard: "Recent Violations" table.
     *
     * Method name breakdown:
     *   findTop5                    = limit to 5 results
     *   ByOrderByViolationDateDesc  = ORDER BY violation_date DESC
     */
    List<Violation> findTop5ByOrderByViolationDateDesc();
}
