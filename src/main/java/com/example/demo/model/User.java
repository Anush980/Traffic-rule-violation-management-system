package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

/**
 * =====================================================================
 * USER ENTITY - Represents a user in the system (Admin or regular User).
 * =====================================================================
 *
 * What is an Entity?
 *   An Entity is a Java class that maps to a DATABASE TABLE.
 *   Each field in this class becomes a COLUMN in the "users" table.
 *   Each object of this class represents one ROW in that table.
 *
 * What is @Entity?
 *   Tells JPA (Java Persistence API) that this class should be stored in the database.
 *
 * What is @Table(name = "users")?
 *   Specifies the actual table name in the database. Without it,
 *   the table name would default to "User" (the class name).
 *   We use "users" because "user" is a reserved word in some databases.
 *
 * DATABASE TABLE STRUCTURE (auto-created by Hibernate):
 *   +----+----------+-------------------+----------+-------+------+-----------+
 *   | id | fullName | email             | password | phone | role | createdAt |
 *   +----+----------+-------------------+----------+-------+------+-----------+
 *   | 1  | Admin    | admin@admin.com   | (hashed) | 999.. | ADMIN| 2025-01-01|
 *   | 2  | John     | john@example.com  | (hashed) | 888.. | USER | 2025-01-02|
 *   +----+----------+-------------------+----------+-------+------+-----------+
 *
 * RELATIONSHIPS:
 *   One User can own MANY Vehicles  -->  @OneToMany relationship
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * PRIMARY KEY - Unique identifier for each user.
     * @Id           = Marks this field as the primary key
     * @GeneratedValue(strategy = GenerationType.IDENTITY) = Database auto-generates
     *                 the ID (1, 2, 3, ...) so we don't have to set it manually.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @NotBlank = Validation: this field cannot be null or empty string.
     *             If someone tries to save a User with blank fullName,
     *             Spring will reject it with a validation error.
     */
    @NotBlank
    private String fullName;

    /**
     * @Email    = Validates that the string is a proper email format (has @, domain, etc.)
     * @NotBlank = Cannot be empty
     * @Column(unique = true) = No two users can have the same email (database constraint)
     */
    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    /**
     * Stores the HASHED password (not plain text!).
     * We use BCryptPasswordEncoder to hash passwords before saving.
     * Example: "admin123" gets stored as "$2a$10$dXJ3SW6G7P50lG..."
     */
    @NotBlank
    private String password;

    /** Phone number of the user - must be exactly 10 digits */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    /**
     * Role determines what pages the user can access:
     *   ADMIN - Can manage rules, add violations, see all data
     *   USER  - Can only see their own vehicles and violations
     *
     * @Enumerated(EnumType.STRING) = Stores the role as text in DB ("ADMIN" or "USER")
     *                                instead of numbers (0 or 1).
     */
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;   // Default role is USER

    /** Timestamp of when the account was created */
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * ONE-TO-MANY RELATIONSHIP: One User can have many Vehicles.
     *
     * @OneToMany = This user has a List of vehicles
     * mappedBy = "user" = The "user" field in Vehicle.java owns this relationship
     *                     (Vehicle table has the foreign key "user_id")
     * cascade = CascadeType.ALL = If we delete a User, all their vehicles get deleted too
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Vehicle> vehicles;

    /**
     * ENUM: Defines the possible roles a user can have.
     * Used by Spring Security to control access to pages.
     */
    public enum Role {
        ADMIN, USER
    }

    // --- CONSTRUCTORS ---

    /** Default constructor - Required by JPA (it creates objects using this) */
    public User() {}

    /** Parameterized constructor - Used in DataInitializer to create admin account */
    public User(String fullName, String email, String password, String phone, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // --- GETTERS AND SETTERS ---
    // These are required by JPA and Thymeleaf to read/write field values.
    // JPA uses them to map database columns to Java fields.
    // Thymeleaf uses them in templates like th:text="${user.fullName}"

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Vehicle> getVehicles() { return vehicles; }
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }
}
