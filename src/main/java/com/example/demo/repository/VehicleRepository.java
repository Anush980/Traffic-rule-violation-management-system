package com.example.demo.repository;

import com.example.demo.model.User;
import com.example.demo.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * =====================================================================
 * VEHICLE REPOSITORY - Database operations for the "vehicles" table.
 * =====================================================================
 *
 * Extends JpaRepository<Vehicle, Long> which gives us built-in methods:
 *   save(), findById(), findAll(), deleteById(), count(), etc.
 *
 * We add custom finder methods below. Spring auto-generates the SQL
 * by reading the method name. No implementation code needed!
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Get all vehicles owned by a specific user.
     * Used in User Dashboard to show "My Vehicles" list.
     *
     * Generated SQL: SELECT * FROM vehicles WHERE user_id = ?
     */
    List<Vehicle> findByUser(User user);

    /**
     * Find a single vehicle by its registration number.
     *
     * Generated SQL: SELECT * FROM vehicles WHERE registration_number = ?
     */
    Vehicle findByRegistrationNumber(String registrationNumber);

    /**
     * Check if a vehicle with the given registration number already exists.
     * Used when a user tries to add a vehicle - prevents duplicate entries.
     *
     * Generated SQL: SELECT COUNT(*) > 0 FROM vehicles WHERE registration_number = ?
     */
    boolean existsByRegistrationNumber(String registrationNumber);
}
