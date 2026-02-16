package com.example.demo.config;

import com.example.demo.model.TrafficRule;
import com.example.demo.model.User;
import com.example.demo.model.Vehicle;
import com.example.demo.model.Violation;
import com.example.demo.repository.TrafficRuleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.repository.ViolationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * =====================================================================
 * DATA INITIALIZER - Seeds the database with default data on startup.
 * =====================================================================
 *
 * What is CommandLineRunner?
 *   An interface with a run() method that Spring calls AUTOMATICALLY
 *   after the application starts. Perfect for inserting default data.
 *
 * What does this class do?
 *   1. Creates a default ADMIN account (so you can log in immediately)
 *   2. Creates 5 sample USER accounts with vehicles registered to them
 *   3. Seeds 8 common Indian traffic rules with fine amounts
 *   4. Creates 12 sample violations (mix of PENDING and PAID fines)
 *
 * Why is this needed?
 *   Since we use H2 in-memory database, ALL DATA IS LOST when the app restarts.
 *   This ensures the admin account and sample rules are always available.
 *
 * What is @Component?
 *   Marks this class as a Spring-managed bean. Spring will auto-detect it
 *   and call the run() method after startup.
 *
 * DEFAULT LOGIN CREDENTIALS:
 *   Admin  -> admin@admin.com / admin123
 *   Users  -> rajesh@gmail.com / rajesh123
 *             sita@gmail.com   / sita123
 *             bikash@gmail.com / bikash123
 *             anita@gmail.com  / anita123
 *             sujan@gmail.com  / sujan123
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TrafficRuleRepository trafficRuleRepository;
    private final VehicleRepository vehicleRepository;
    private final ViolationRepository violationRepository;
    private final PasswordEncoder passwordEncoder;

    /** Constructor injection - Spring provides all dependencies */
    public DataInitializer(UserRepository userRepository, TrafficRuleRepository trafficRuleRepository,
                           VehicleRepository vehicleRepository, ViolationRepository violationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.trafficRuleRepository = trafficRuleRepository;
        this.vehicleRepository = vehicleRepository;
        this.violationRepository = violationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * This method runs ONCE when the application starts.
     * It seeds the database with users, vehicles, traffic rules, and violations.
     */
    @Override
    public void run(String... args) {

        // ---- Step 1: Create default admin account ----
        if (!userRepository.existsByEmail("admin@admin.com")) {
            User admin = new User(
                "Admin",
                "admin@admin.com",
                passwordEncoder.encode("admin123"),
                "9999999999",
                User.Role.ADMIN
            );
            userRepository.save(admin);
        }

        // ---- Step 2: Create sample users ----
        User rahul = null, priya = null, amit = null, sneha = null, vikram = null;

        if (!userRepository.existsByEmail("shresthabidhika618@gmail.com")) {
            rahul = userRepository.save(new User("Shrestha Bhika", "shresthabidhika618@gmail.com",
                passwordEncoder.encode("Test@123"), "9812345678", User.Role.USER));
        }
        if (!userRepository.existsByEmail("sita@gmail.com")) {
            priya = userRepository.save(new User("Sita Limbu", "sita@gmail.com",
                passwordEncoder.encode("sita123"), "9812345679", User.Role.USER));
        }
        if (!userRepository.existsByEmail("bikash@gmail.com")) {
            amit = userRepository.save(new User("Bikash Karki", "bikash@gmail.com",
                passwordEncoder.encode("bikash123"), "9812345680", User.Role.USER));
        }
        if (!userRepository.existsByEmail("anita@gmail.com")) {
            sneha = userRepository.save(new User("Anita Sharma", "anita@gmail.com",
                passwordEncoder.encode("anita123"), "9812345681", User.Role.USER));
        }
        if (!userRepository.existsByEmail("sujan@gmail.com")) {
            vikram = userRepository.save(new User("Sujan Thapa", "sujan@gmail.com",
                passwordEncoder.encode("sujan123"), "9812345682", User.Role.USER));
        }

        // ---- Step 3: Seed sample traffic rules ----
        TrafficRule ruleSpeed, ruleSignal, ruleHelmet, ruleSeatBelt, ruleDrunk,
                    ruleWrongSide, ruleParking, ruleMobile;

        if (trafficRuleRepository.count() == 0) {
            ruleSpeed     = trafficRuleRepository.save(new TrafficRule("Over Speeding", "Exceeding the speed limit", 1000.0));
            ruleSignal    = trafficRuleRepository.save(new TrafficRule("Signal Jump", "Jumping a red traffic signal", 1500.0));
            ruleHelmet    = trafficRuleRepository.save(new TrafficRule("No Helmet", "Riding two-wheeler without helmet", 500.0));
            ruleSeatBelt  = trafficRuleRepository.save(new TrafficRule("No Seat Belt", "Driving without seat belt", 500.0));
            ruleDrunk     = trafficRuleRepository.save(new TrafficRule("Drunk Driving", "Driving under the influence of alcohol", 5000.0));
            ruleWrongSide = trafficRuleRepository.save(new TrafficRule("Wrong Side Driving", "Driving on the wrong side of the road", 2000.0));
            ruleParking   = trafficRuleRepository.save(new TrafficRule("No Parking", "Parking in a no-parking zone", 750.0));
            ruleMobile    = trafficRuleRepository.save(new TrafficRule("Using Mobile Phone", "Using phone while driving", 1500.0));
        } else {
            // Rules already exist, skip vehicle and violation seeding
            return;
        }

        // ---- Step 4: Register vehicles for each user ----
        // Only runs if rules were just created (first-time startup)
        if (rahul == null || priya == null || amit == null || sneha == null || vikram == null) {
            return; // Users already existed, skip vehicle/violation seeding
        }

        // Rajesh's vehicles
        Vehicle rajeshCar = new Vehicle();
        rajeshCar.setRegistrationNumber("Ko-01-PA-1234");
        rajeshCar.setVehicleType(Vehicle.VehicleType.CAR);
        rajeshCar.setModel("Maruti Swift");
        rajeshCar.setUser(rahul);
        vehicleRepository.save(rajeshCar);

        Vehicle rajeshBike = new Vehicle();
        rajeshBike.setRegistrationNumber("Ko-01-PA-5678");
        rajeshBike.setVehicleType(Vehicle.VehicleType.BIKE);
        rajeshBike.setModel("Bajaj Pulsar NS200");
        rajeshBike.setUser(rahul);
        vehicleRepository.save(rajeshBike);

        // Sita's vehicle
        Vehicle sitaCar = new Vehicle();
        sitaCar.setRegistrationNumber("Me-02-PA-9012");
        sitaCar.setVehicleType(Vehicle.VehicleType.CAR);
        sitaCar.setModel("Hyundai i20");
        sitaCar.setUser(priya);
        vehicleRepository.save(sitaCar);

        // Bikash's vehicles
        Vehicle bikashBike = new Vehicle();
        bikashBike.setRegistrationNumber("Ko-03-PA-3456");
        bikashBike.setVehicleType(Vehicle.VehicleType.BIKE);
        bikashBike.setModel("Honda Shine");
        bikashBike.setUser(amit);
        vehicleRepository.save(bikashBike);

        Vehicle bikashAuto = new Vehicle();
        bikashAuto.setRegistrationNumber("Ko-03-PA-7890");
        bikashAuto.setVehicleType(Vehicle.VehicleType.AUTO);
        bikashAuto.setModel("Bajaj RE");
        bikashAuto.setUser(amit);
        vehicleRepository.save(bikashAuto);

        // Anita's vehicle
        Vehicle anitaCar = new Vehicle();
        anitaCar.setRegistrationNumber("Me-04-PA-2345");
        anitaCar.setVehicleType(Vehicle.VehicleType.CAR);
        anitaCar.setModel("Hyundai Creta");
        anitaCar.setUser(sneha);
        vehicleRepository.save(anitaCar);

        // Sujan's vehicles
        Vehicle sujanTruck = new Vehicle();
        sujanTruck.setRegistrationNumber("Ko-05-PA-6789");
        sujanTruck.setVehicleType(Vehicle.VehicleType.TRUCK);
        sujanTruck.setModel("Tata Ace");
        sujanTruck.setUser(vikram);
        vehicleRepository.save(sujanTruck);

        Vehicle sujanBike = new Vehicle();
        sujanBike.setRegistrationNumber("Ko-05-PA-1122");
        sujanBike.setVehicleType(Vehicle.VehicleType.BIKE);
        sujanBike.setModel("KTM Duke 200");
        sujanBike.setUser(vikram);
        vehicleRepository.save(sujanBike);

        // ---- Step 5: Create sample violations (fines) ----

        // Rajesh's car - Over Speeding (PENDING)
        Violation v1 = new Violation();
        v1.setVehicle(rajeshCar);
        v1.setTrafficRule(ruleSpeed);
        v1.setViolationDate(LocalDateTime.of(2025, 6, 15, 14, 30));
        v1.setLocation("Itahari Road, Biratnagar");
        v1.setDescription("Caught doing 90 km/h in a 60 km/h zone");
        violationRepository.save(v1);

        // Rajesh's bike - No Helmet (PAID)
        Violation v2 = new Violation();
        v2.setVehicle(rajeshBike);
        v2.setTrafficRule(ruleHelmet);
        v2.setViolationDate(LocalDateTime.of(2025, 5, 20, 9, 15));
        v2.setLocation("Main Road, Biratnagar");
        v2.setDescription("Riding without helmet");
        v2.setStatus(Violation.Status.PAID);
        violationRepository.save(v2);

        // Sita's car - Signal Jump (PENDING)
        Violation v3 = new Violation();
        v3.setVehicle(sitaCar);
        v3.setTrafficRule(ruleSignal);
        v3.setViolationDate(LocalDateTime.of(2025, 7, 2, 18, 45));
        v3.setLocation("Traffic Chowk, Biratnagar");
        v3.setDescription("Jumped red signal during peak hours");
        violationRepository.save(v3);

        // Sita's car - No Seat Belt (PAID)
        Violation v4 = new Violation();
        v4.setVehicle(sitaCar);
        v4.setTrafficRule(ruleSeatBelt);
        v4.setViolationDate(LocalDateTime.of(2025, 4, 10, 11, 0));
        v4.setLocation("Dharan Road, Biratnagar");
        v4.setDescription("Driver not wearing seat belt");
        v4.setStatus(Violation.Status.PAID);
        violationRepository.save(v4);

        // Bikash's bike - Wrong Side Driving (PENDING)
        Violation v5 = new Violation();
        v5.setVehicle(bikashBike);
        v5.setTrafficRule(ruleWrongSide);
        v5.setViolationDate(LocalDateTime.of(2025, 8, 5, 7, 30));
        v5.setLocation("Rani Chowk, Biratnagar");
        v5.setDescription("Riding on wrong side of the road");
        violationRepository.save(v5);

        // Bikash's auto - No Parking (PENDING)
        Violation v6 = new Violation();
        v6.setVehicle(bikashAuto);
        v6.setTrafficRule(ruleParking);
        v6.setViolationDate(LocalDateTime.of(2025, 7, 18, 16, 20));
        v6.setLocation("Jogbani Border, Biratnagar");
        v6.setDescription("Parked in no-parking zone blocking traffic");
        violationRepository.save(v6);

        // Anita's car - Using Mobile Phone (PENDING)
        Violation v7 = new Violation();
        v7.setVehicle(anitaCar);
        v7.setTrafficRule(ruleMobile);
        v7.setViolationDate(LocalDateTime.of(2025, 9, 1, 13, 0));
        v7.setLocation("Tinpaini, Biratnagar");
        v7.setDescription("Using mobile phone while driving");
        violationRepository.save(v7);

        // Anita's car - Over Speeding (PAID)
        Violation v8 = new Violation();
        v8.setVehicle(anitaCar);
        v8.setTrafficRule(ruleSpeed);
        v8.setViolationDate(LocalDateTime.of(2025, 3, 22, 22, 30));
        v8.setLocation("Koshi Highway, Biratnagar");
        v8.setDescription("Exceeded speed limit by 30 km/h");
        v8.setStatus(Violation.Status.PAID);
        violationRepository.save(v8);

        // Sujan's truck - Drunk Driving (PENDING)
        Violation v9 = new Violation();
        v9.setVehicle(sujanTruck);
        v9.setTrafficRule(ruleDrunk);
        v9.setViolationDate(LocalDateTime.of(2025, 8, 14, 23, 45));
        v9.setLocation("Mahendra Highway, Biratnagar");
        v9.setDescription("Failed breathalyzer test, BAC above legal limit");
        violationRepository.save(v9);

        // Sujan's bike - Signal Jump (PAID)
        Violation v10 = new Violation();
        v10.setVehicle(sujanBike);
        v10.setTrafficRule(ruleSignal);
        v10.setViolationDate(LocalDateTime.of(2025, 6, 30, 8, 15));
        v10.setLocation("Baraha Chowk, Biratnagar");
        v10.setDescription("Jumped red signal at busy intersection");
        v10.setStatus(Violation.Status.PAID);
        violationRepository.save(v10);

        // Rajesh's car - Using Mobile Phone (PENDING)
        Violation v11 = new Violation();
        v11.setVehicle(rajeshCar);
        v11.setTrafficRule(ruleMobile);
        v11.setViolationDate(LocalDateTime.of(2025, 9, 10, 10, 0));
        v11.setLocation("Ganesh Chowk, Biratnagar");
        v11.setDescription("Caught talking on phone while driving");
        violationRepository.save(v11);

        // Sujan's truck - No Parking (PENDING)
        Violation v12 = new Violation();
        v12.setVehicle(sujanTruck);
        v12.setTrafficRule(ruleParking);
        v12.setViolationDate(LocalDateTime.of(2025, 7, 25, 15, 30));
        v12.setLocation("Pokhariya Chowk, Biratnagar");
        v12.setDescription("Truck parked in residential no-parking zone");
        violationRepository.save(v12);
    }
}
