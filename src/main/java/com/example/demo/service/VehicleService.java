package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Vehicle;
import com.example.demo.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * =====================================================================
 * VEHICLE SERVICE - Business logic for managing vehicles.
 * =====================================================================
 *
 * Handles all vehicle-related operations:
 *   - Getting vehicles for a specific user (User Dashboard)
 *   - Getting all vehicles (Admin - Add Violation dropdown)
 *   - Saving new vehicles (User - Add Vehicle form)
 *   - Checking for duplicate registration numbers
 */
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    /** Constructor injection - Spring passes the repository automatically */
    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /** Get all vehicles belonging to a specific user - used in User Dashboard */
    public List<Vehicle> getVehiclesByUser(User user) {
        return vehicleRepository.findByUser(user);
    }

    /** Get ALL vehicles in the system - used in Admin's "Add Violation" dropdown */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /** Save a new vehicle to the database - used when user submits Add Vehicle form */
    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    /**
     * Check if a vehicle with this registration number already exists.
     * Used to prevent duplicate vehicle registration.
     * Called before saving a new vehicle.
     */
    public boolean existsByRegistrationNumber(String regNum) {
        return vehicleRepository.existsByRegistrationNumber(regNum);
    }
}
