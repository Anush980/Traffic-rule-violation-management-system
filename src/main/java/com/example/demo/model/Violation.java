package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * VIOLATION ENTITY - Represents a traffic violation recorded by admin.
 * =====================================================================
 *
 * This is the CENTRAL TABLE in the entire traffic violation system.
 * It acts as the bridge (junction/linking table) that connects TWO
 * other entities together:
 *
 *   1. Vehicle  -- which vehicle committed the violation?
 *   2. TrafficRule -- which traffic rule was broken?
 *
 * Think of it this way:
 *   - A Vehicle can have MANY Violations    (one car, multiple tickets)
 *   - A TrafficRule can apply to MANY Violations (one rule broken by many cars)
 *   - Each Violation links exactly ONE Vehicle to exactly ONE TrafficRule
 *
 * RELATIONSHIP DIAGRAM:
 *
 *   +----------+         +-------------+         +---------------+
 *   | Vehicle  |  1   *  |  Violation  |  *   1  |  TrafficRule  |
 *   |----------|-------->|-------------|-------->|---------------|
 *   | id (PK)  |         | id (PK)     |         | id (PK)       |
 *   | regNo    |         | vehicle_id  |--FK-->  | ruleName      |
 *   | model    |         | rule_id     |--FK-->  | description   |
 *   | ...      |         | date        |         | fineAmount    |
 *   +----------+         | location    |         +---------------+
 *                        | description |
 *                        | status      |
 *                        +-------------+
 *
 *   (1 = one side, * = many side)
 *   FK = Foreign Key pointing to the parent table's Primary Key
 *
 * DATABASE TABLE ("violations") looks like this:
 *
 *   +----+------------+---------+---------------------+----------+-------------+---------+
 *   | id | vehicle_id | rule_id | violation_date      | location | description | status  |
 *   +----+------------+---------+---------------------+----------+-------------+---------+
 *   |  1 |          3 |       2 | 2025-06-15 14:30:00 | MG Road  | Ran red     | PENDING |
 *   |  2 |          3 |       5 | 2025-07-01 09:00:00 | NH-44    | Overspeeding| PAID    |
 *   |  3 |          7 |       2 | 2025-07-10 18:45:00 | Ring Rd  | Ran red     | PENDING |
 *   +----+------------+---------+---------------------+----------+-------------+---------+
 *
 *   Notice: vehicle_id=3 appears twice (same vehicle, two violations).
 *           rule_id=2 appears twice (same rule broken by different vehicles).
 *           This is the Many-to-One relationship in action.
 *
 * WHY DOESN'T VIOLATION STORE THE FINE AMOUNT DIRECTLY?
 *
 *   The fine amount lives in the TrafficRule table, NOT here.
 *   This avoids duplicating the fine amount across every violation row.
 *   To get the fine for a violation, you navigate the relationship:
 *
 *       double fine = violation.getTrafficRule().getFineAmount();
 *                     ^^^^^^^^^                  ^^^^^^^^^^^^^^
 *                     Step 1: Get the linked      Step 2: From that rule,
 *                     TrafficRule object           get the fine amount
 *
 *   This is called "traversing the object graph" -- one of the key
 *   benefits of JPA/Hibernate. You work with Java objects, not SQL joins.
 *   Behind the scenes, Hibernate may execute a JOIN query like:
 *
 *       SELECT v.*, tr.fine_amount
 *       FROM violations v
 *       JOIN traffic_rules tr ON v.rule_id = tr.id
 *       WHERE v.id = ?
 */

/*
 * @Entity -- Tells JPA/Hibernate: "This Java class maps to a database table."
 *            Without this annotation, Hibernate completely ignores this class.
 *            Hibernate will:
 *              1. Create/manage a table for this class
 *              2. Map each field to a column
 *              3. Handle all SQL (INSERT, SELECT, UPDATE, DELETE) for you
 */
@Entity

/*
 * @Table(name = "violations") -- Specifies the EXACT table name in the database.
 *
 *   Why use this? By default, Hibernate would name the table "Violation"
 *   (matching the class name). We want "violations" (lowercase, plural)
 *   to follow standard database naming conventions.
 *
 *   This maps:  Java class "Violation" <---> Database table "violations"
 */
@Table(name = "violations")
public class Violation {

    /*
     * @Id -- Marks this field as the PRIMARY KEY of the "violations" table.
     *        Every JPA entity MUST have exactly one @Id field.
     *        The primary key uniquely identifies each row in the table.
     *        Example: violation with id=1 is different from id=2.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     *        Tells the database to AUTO-INCREMENT the id value.
     *        You do NOT set the id yourself -- the database assigns it.
     *
     *        How it works:
     *          - First violation inserted  -> id = 1 (auto-assigned)
     *          - Second violation inserted -> id = 2 (auto-assigned)
     *          - Third violation inserted  -> id = 3 (auto-assigned)
     *
     *        GenerationType.IDENTITY means: "Let the database handle it."
     *        This uses the database's native auto-increment feature
     *        (e.g., AUTO_INCREMENT in MySQL, SERIAL in PostgreSQL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @ManyToOne -- Defines the relationship: MANY Violations -> ONE Vehicle.
     *
     *   This means:
     *     - Many Violation rows can point to the SAME Vehicle row
     *     - But each Violation points to exactly ONE Vehicle
     *
     *   Real-world analogy:
     *     One car (Vehicle) can receive many parking tickets (Violations),
     *     but each ticket belongs to only one car.
     *
     * @JoinColumn(name = "vehicle_id")
     *   Tells Hibernate: "In the violations table, create a column called
     *   'vehicle_id' that stores the foreign key pointing to the vehicles table."
     *
     *   In the database, this creates:
     *     violations.vehicle_id  --->  vehicles.id
     *
     *   Example: If vehicle_id = 3, this violation is linked to the Vehicle
     *   whose id = 3 in the "vehicles" table.
     *
     *   IMPORTANT: Even though the database stores just a number (vehicle_id = 3),
     *   in Java you get a full Vehicle OBJECT with all its fields (regNo, model, etc).
     *   Hibernate handles the JOIN behind the scenes.
     */
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    /*
     * @ManyToOne -- Defines the relationship: MANY Violations -> ONE TrafficRule.
     *
     *   This means:
     *     - Many Violation rows can reference the SAME TrafficRule
     *     - But each Violation references exactly ONE TrafficRule
     *
     *   Real-world analogy:
     *     The rule "No running red lights" (TrafficRule) can be broken by
     *     many different vehicles at different times (many Violations),
     *     but each specific violation is for breaking one specific rule.
     *
     * @JoinColumn(name = "rule_id")
     *   In the database, this creates a foreign key column:
     *     violations.rule_id  --->  traffic_rules.id
     *
     *   This is HOW you get the fine amount for a violation:
     *     violation.getTrafficRule()              -> returns TrafficRule object
     *     violation.getTrafficRule().getFineAmount() -> returns the fine (e.g., 500.0)
     *     violation.getTrafficRule().getRuleName()   -> returns the rule name
     */
    @ManyToOne
    @JoinColumn(name = "rule_id")
    private TrafficRule trafficRule;

    /*
     * The date and time when the violation occurred.
     *
     * LocalDateTime stores both date + time without timezone info.
     * Example value: 2025-06-15T14:30:00 (June 15, 2025, at 2:30 PM)
     *
     * Hibernate automatically maps LocalDateTime to the appropriate
     * database column type (e.g., TIMESTAMP in MySQL/PostgreSQL).
     */
    private LocalDateTime violationDate;

    /*
     * The physical location where the violation took place.
     * Example values: "MG Road, Bangalore", "NH-44 Toll Plaza", "Ring Road Junction"
     *
     * Maps to a VARCHAR column in the database by default.
     */
    private String location;

    /*
     * A free-text description of what happened.
     * Example values: "Ran red light at signal", "Exceeded speed limit by 40 km/h"
     *
     * This provides additional context beyond just the rule that was broken.
     */
    private String description;

    /*
     * @Enumerated(EnumType.STRING)
     *   Tells Hibernate HOW to store the Java enum in the database.
     *
     *   There are two options:
     *     - EnumType.STRING  -> stores "PENDING" or "PAID" as text (we use this)
     *     - EnumType.ORDINAL -> stores 0 or 1 as numbers (fragile, avoid this!)
     *
     *   Why STRING is better than ORDINAL:
     *     With ORDINAL, PENDING=0 and PAID=1. If someone later adds a new status
     *     like DISPUTED between them, all existing numbers shift and data breaks!
     *     With STRING, "PENDING" is always "PENDING" regardless of enum order.
     *
     *   Database column will contain:
     *     +--------+
     *     | status |
     *     +--------+
     *     |PENDING |   <-- stored as readable text, not a number
     *     |PAID    |
     *     |PENDING |
     *     +--------+
     *
     *   Default value: Status.PENDING
     *     When a new violation is created, it starts as PENDING (unpaid).
     *     The admin or user can later change it to PAID after payment.
     */
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    /*
     * =====================================================================
     * STATUS ENUM - Tracks whether the fine for this violation has been paid.
     * =====================================================================
     *
     * This enum defines the lifecycle of a violation's payment status:
     *
     *   PENDING --> The violation has been recorded but the fine is NOT yet paid.
     *               This is the initial/default state for every new violation.
     *               The vehicle owner still owes money.
     *
     *   PAID    --> The fine has been paid and the violation is resolved.
     *               This is the final state. Once paid, the violation is settled.
     *
     * Typical flow:
     *   1. Admin records a violation       -> status = PENDING (automatic default)
     *   2. Vehicle owner pays the fine     -> status = PAID    (updated by admin/system)
     *
     * In code:
     *   violation.setStatus(Violation.Status.PAID);  // mark as paid
     *
     * In database queries:
     *   SELECT * FROM violations WHERE status = 'PENDING';  // find unpaid fines
     */
    public enum Status {
        PENDING, PAID
    }

    /*
     * =====================================================================
     * DEFAULT (NO-ARGUMENT) CONSTRUCTOR - Required by JPA/Hibernate.
     * =====================================================================
     *
     * JPA REQUIRES every entity to have a public or protected no-arg constructor.
     *
     * Why? When Hibernate loads data from the database, it needs to:
     *   1. Create an empty Violation object using this constructor  -> new Violation()
     *   2. Fill in the fields using setter methods or reflection    -> setId(1), setLocation("MG Road"), etc.
     *
     * If you remove this constructor, you will get an error like:
     *   "org.hibernate.InstantiationException: No default constructor for entity: Violation"
     *
     * Note: If you had NO constructors at all, Java would auto-generate this one.
     *       But if you add a parameterized constructor later, Java stops generating
     *       the default one, so it is good practice to always declare it explicitly.
     */
    public Violation() {}

    // =====================================================================
    // GETTERS AND SETTERS
    // =====================================================================
    //
    // These methods provide controlled access to the private fields above.
    // JPA/Hibernate uses these (along with reflection) to read/write field values.
    //
    // Pattern:
    //   getXxx() -> returns the current value of field xxx
    //   setXxx() -> updates the value of field xxx
    //
    // Example usage in a service/controller:
    //
    //   Violation v = new Violation();
    //   v.setVehicle(someVehicle);
    //   v.setTrafficRule(someRule);
    //   v.setViolationDate(LocalDateTime.now());
    //   v.setLocation("MG Road");
    //   v.setDescription("Ran red light");
    //   // status is already PENDING by default
    //   violationRepository.save(v);   // Hibernate inserts into database
    //
    // Reading data:
    //   Violation v = violationRepository.findById(1L).get();
    //   String where = v.getLocation();                        // "MG Road"
    //   double fine  = v.getTrafficRule().getFineAmount();      // e.g., 500.0
    //   String plate = v.getVehicle().getRegistrationNumber();  // e.g., "KA-01-AB-1234"

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public TrafficRule getTrafficRule() { return trafficRule; }
    public void setTrafficRule(TrafficRule trafficRule) { this.trafficRule = trafficRule; }

    public LocalDateTime getViolationDate() { return violationDate; }
    public void setViolationDate(LocalDateTime violationDate) { this.violationDate = violationDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
