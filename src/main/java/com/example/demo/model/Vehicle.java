package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * =====================================================================
 * VEHICLE ENTITY - Represents a vehicle registered by a user.
 * =====================================================================
 *
 * This is an Entity class (see User.java for full explanation of what an Entity is).
 * In short: each field = a database column, each object = a database row.
 *
 * Each user can register multiple vehicles (Car, Bike, Truck, etc.).
 * When an admin records a traffic violation, it is linked to a specific vehicle.
 *
 * DATABASE TABLE STRUCTURE (auto-created by Hibernate):
 *   +----+--------------------+-------------+------------+---------+
 *   | id | registrationNumber | vehicleType | model      | user_id |
 *   +----+--------------------+-------------+------------+---------+
 *   | 1  | KA-01-AB-1234      | CAR         | Honda City | 2       |
 *   | 2  | KA-02-CD-5678      | BIKE        | Pulsar 150 | 2       |
 *   | 3  | MH-04-EF-9012      | TRUCK       | Tata Ace   | 3       |
 *   +----+--------------------+-------------+------------+---------+
 *
 *   Notice: "user_id" is a FOREIGN KEY that points to the "id" column in the "users" table.
 *           Vehicles with user_id=2 both belong to the user whose id is 2.
 *
 * RELATIONSHIPS:
 *   Many Vehicles belong to One User      -->  @ManyToOne  (user_id is the foreign key)
 *   One Vehicle can have Many Violations  -->  @OneToMany  (vehicle_id in violations table)
 *
 *   Example:
 *     User "John" (id=2) owns Vehicle "KA-01-AB-1234" (id=1) and "KA-02-CD-5678" (id=2)
 *     Vehicle "KA-01-AB-1234" (id=1) has 3 violations linked to it
 */

/**
 * @Entity = Tells JPA that this class maps to a database table.
 *           Without this, Hibernate would ignore this class completely.
 *
 * @Table(name = "vehicles") = The actual table name in the database will be "vehicles".
 *                              Without this, the table name would default to "Vehicle" (class name).
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    /**
     * PRIMARY KEY - Unique identifier for each vehicle.
     *
     * @Id = Marks this field as the primary key of the "vehicles" table.
     *       Every database table needs a primary key to uniquely identify each row.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY) =
     *       The database auto-generates the ID (1, 2, 3, ...).
     *       We never set this manually; the database handles it when a new vehicle is saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The vehicle's registration number (license plate), e.g., "KA-01-AB-1234".
     *
     * @NotBlank = Validation: this field cannot be null, empty, or just whitespace.
     *             If someone tries to register a vehicle without a registration number,
     *             Spring will reject it with a validation error BEFORE it reaches the database.
     *
     * @Column(unique = true) = Database constraint: no two vehicles can have the same
     *                          registration number. If you try to insert a duplicate,
     *                          the database throws an error.
     *                          This makes sense because in real life, every vehicle
     *                          has a unique license plate!
     */
    @NotBlank
    @Column(unique = true)
    private String registrationNumber;

    /**
     * The type of vehicle (CAR, BIKE, TRUCK, AUTO, or BUS).
     *
     * @Enumerated(EnumType.STRING) = Stores the enum value as a TEXT string in the database.
     *
     *   Without this annotation (or with EnumType.ORDINAL):
     *     CAR would be stored as 0, BIKE as 1, TRUCK as 2, etc.
     *     Problem: If you reorder the enum values, all existing data becomes WRONG!
     *
     *   With EnumType.STRING:
     *     CAR is stored as "CAR", BIKE as "BIKE", TRUCK as "TRUCK", etc.
     *     Much safer and more readable in the database.
     *
     *   Database column will look like: vehicleType = "CAR" or "BIKE" or "TRUCK" etc.
     */
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    /**
     * The model name of the vehicle, e.g., "Honda City", "Pulsar 150", "Tata Ace".
     * No validation annotations here, so this field is optional.
     * No @Column annotation either, so it uses default column settings
     * (column name = "model", nullable = true).
     */
    private String model;

    /**
     * MANY-TO-ONE RELATIONSHIP: Many vehicles belong to one user.
     *
     * @ManyToOne = This is the "Many" side of the relationship.
     *              Multiple Vehicle objects can point to the same User object.
     *              Think of it this way: Many vehicles can belong to One user.
     *
     *   Example:
     *     Vehicle "KA-01-AB-1234" --> belongs to User "John" (id=2)
     *     Vehicle "KA-02-CD-5678" --> belongs to User "John" (id=2)
     *     Vehicle "MH-04-EF-9012" --> belongs to User "Jane" (id=3)
     *
     * @JoinColumn(name = "user_id") = Creates a column called "user_id" in the "vehicles" table.
     *              This column holds the foreign key - the ID of the user who owns this vehicle.
     *              In the example above, vehicles 1 and 2 have user_id=2 (John),
     *              and vehicle 3 has user_id=3 (Jane).
     *
     *   HOW IT LINKS TO User.java:
     *     - In User.java:    @OneToMany(mappedBy = "user")  --> "I have many vehicles"
     *     - In Vehicle.java: @ManyToOne + @JoinColumn        --> "I belong to one user"
     *     - The "mappedBy = user" in User.java refers to THIS "user" field below.
     *     - The Vehicle table OWNS the relationship (it has the foreign key column).
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * ONE-TO-MANY RELATIONSHIP: One vehicle can have many violations.
     *
     * @OneToMany = This vehicle has a List of violations associated with it.
     *              One vehicle can accumulate multiple traffic violations over time.
     *
     *   Example:
     *     Vehicle "KA-01-AB-1234" has:
     *       - Violation #1: Speeding on MG Road (PENDING)
     *       - Violation #2: Signal jumping on Brigade Road (PAID)
     *       - Violation #3: Wrong parking on Church Street (PENDING)
     *
     * mappedBy = "vehicle" = The "vehicle" field in Violation.java owns this relationship.
     *                        This means the "violations" table has the foreign key "vehicle_id",
     *                        NOT the other way around. This side just reads the data.
     *
     * cascade = CascadeType.ALL = If we DELETE a vehicle, all its violations get deleted too.
     *                             This is called "cascading" - changes cascade (flow down)
     *                             from parent (Vehicle) to children (Violations).
     *                             Without this, deleting a vehicle would fail because
     *                             violations still reference it.
     */
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<Violation> violations;

    /**
     * =====================================================================
     * ENUM: VehicleType - Defines the possible types of vehicles.
     * =====================================================================
     *
     * An enum (short for "enumeration") is a special Java type that represents
     * a FIXED SET of constants. A vehicle can ONLY be one of these types:
     *
     *   CAR   - Four-wheeler car (Honda City, Maruti Swift, etc.)
     *   BIKE  - Two-wheeler motorcycle (Pulsar, Royal Enfield, etc.)
     *   TRUCK - Heavy goods vehicle (Tata Ace, Ashok Leyland, etc.)
     *   AUTO  - Three-wheeler auto-rickshaw
     *   BUS   - Public/private bus
     *
     * Why use an enum instead of a plain String?
     *   - Prevents invalid values: You can't accidentally set vehicleType = "AIRPLANE"
     *   - Type safety: The compiler catches typos at compile time
     *   - Clean code: Easy to use in switch/case statements and dropdowns
     *
     * In the HTML form, this enum populates a dropdown menu for users to select
     * their vehicle type when registering a new vehicle.
     */
    public enum VehicleType {
        CAR, BIKE, TRUCK, AUTO, BUS
    }

    // =====================================================================
    // CONSTRUCTOR
    // =====================================================================

    /**
     * Default (no-argument) constructor - REQUIRED by JPA.
     *
     * JPA (Hibernate) creates Vehicle objects using reflection, which needs
     * a no-arg constructor. Without this, Hibernate would throw an error
     * when trying to load vehicles from the database.
     */
    public Vehicle() {}

    // =====================================================================
    // GETTERS AND SETTERS
    // =====================================================================
    //
    // Getters and Setters are methods that read and write field values.
    //   - Getter: Returns the value of a private field   (e.g., getId() returns id)
    //   - Setter: Updates the value of a private field   (e.g., setId(5) sets id to 5)
    //
    // Why are they needed?
    //   1. JPA/Hibernate uses them to map database columns to Java fields
    //      (when loading a vehicle from the DB, Hibernate calls setRegistrationNumber(...))
    //   2. Thymeleaf templates use them to display data
    //      (e.g., th:text="${vehicle.registrationNumber}" calls getRegistrationNumber())
    //   3. Spring form binding uses setters to populate this object from form data
    //      (when a user submits the "Add Vehicle" form, Spring calls the setters)
    //   4. The fields are "private", so they can ONLY be accessed through these methods.
    //      This is called ENCAPSULATION - a core principle of Object-Oriented Programming.
    // =====================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Violation> getViolations() { return violations; }
    public void setViolations(List<Violation> violations) { this.violations = violations; }
}
