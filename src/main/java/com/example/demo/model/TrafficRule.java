package com.example.demo.model;

// --- Jakarta Persistence imports ---
// These come from the Jakarta Persistence API (JPA), which lets us map
// Java classes to database tables without writing raw SQL.
import jakarta.persistence.*;

// --- Jakarta Validation imports ---
// These are Bean Validation annotations. They let us declare rules
// (like "this field must not be blank") directly on the fields.
// Spring Boot automatically enforces them when we use @Valid in a controller.
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * =====================================================================
 * TRAFFIC RULE ENTITY - Represents a traffic rule and its fine amount.
 * =====================================================================
 *
 * PURPOSE:
 *   This class is a JPA "entity" -- a plain Java object that JPA
 *   automatically maps to a row in a relational database table.
 *   Each instance of TrafficRule corresponds to ONE row in the
 *   "traffic_rules" table.
 *
 * DATABASE TABLE IT MAPS TO:
 *
 *   Table name: traffic_rules
 *
 *   +----+--------------------+-------------------------------+-------------+
 *   | id | rule_name          | description                   | fine_amount |
 *   +----+--------------------+-------------------------------+-------------+
 *   |  1 | Speed Limit        | Exceeding the speed limit     |      150.00 |
 *   |  2 | Red Light          | Running a red light           |      300.00 |
 *   |  3 | No Seatbelt        | Driving without a seatbelt    |      100.00 |
 *   +----+--------------------+-------------------------------+-------------+
 *
 * HOW IT WORKS (the big picture):
 *   1. We annotate this class with @Entity so JPA knows it is a table.
 *   2. Each field (id, ruleName, description, fineAmount) becomes a column.
 *   3. JPA uses the getters/setters to read and write field values.
 *   4. When Spring Boot starts, Hibernate (the JPA implementation) can
 *      auto-create or update this table in the database for us.
 *
 * RELATIONSHIPS:
 *   - This entity is referenced by the TrafficViolation entity.
 *     A single TrafficRule can be associated with many TrafficViolation
 *     records (a one-to-many relationship from the rule's perspective).
 *     For example, the "Speed Limit" rule could appear in hundreds of
 *     individual violation records.
 *
 * NAMING CONVENTION:
 *   - Java field names use camelCase  (e.g., ruleName, fineAmount).
 *   - Hibernate's default naming strategy converts camelCase to
 *     snake_case for the database columns (e.g., rule_name, fine_amount).
 *     So you do NOT need a @Column annotation unless you want a
 *     completely custom column name.
 */

/*
 * @Entity
 * -------
 * Marks this class as a JPA entity (i.e., a database table).
 * Without this annotation, JPA would completely ignore this class.
 * Think of it as saying: "Hey JPA, please manage this class as a
 * database table."
 */
@Entity

/*
 * @Table(name = "traffic_rules")
 * -------------------------------
 * Specifies the EXACT table name to use in the database.
 * If we omitted this, JPA would default to naming the table after
 * the class ("TrafficRule" or "traffic_rule" depending on the
 * naming strategy). Here we explicitly set it to "traffic_rules"
 * so there is no ambiguity.
 */
@Table(name = "traffic_rules")
public class TrafficRule {

    /*
     * @Id
     * ----
     * Marks this field as the PRIMARY KEY of the table.
     * Every JPA entity MUST have exactly one @Id field.
     * The primary key uniquely identifies each row -- no two rows
     * can share the same id value.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * ---------------------------------------------------
     * Tells the database to AUTO-GENERATE the id value for each new row.
     * GenerationType.IDENTITY means the database itself handles the
     * auto-increment (e.g., MySQL's AUTO_INCREMENT or PostgreSQL's
     * SERIAL). You never need to set the id manually when creating
     * a new TrafficRule -- the database assigns it automatically.
     *
     * Example: If the last row had id = 5, the next INSERT will
     *          automatically get id = 6.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @NotBlank
     * ---------
     * A Bean Validation annotation that enforces TWO rules:
     *   1. The value must NOT be null.
     *   2. The trimmed value must NOT be empty ("" or "   ").
     *
     * If someone tries to save a TrafficRule with a blank ruleName,
     * Spring will reject the request with a validation error BEFORE
     * it even reaches the database.
     *
     * Note: @NotBlank only works on String fields. For other types,
     *       you would use @NotNull or @NotEmpty.
     *
     * This field maps to the "rule_name" column (camelCase -> snake_case).
     */
    @NotBlank
    private String ruleName;

    /*
     * description
     * -----------
     * An optional text field that provides extra details about the rule.
     * Notice there is NO validation annotation here, which means:
     *   - It CAN be null (no @NotNull).
     *   - It CAN be blank (no @NotBlank).
     * This is a deliberate design choice -- not every rule needs a
     * lengthy description.
     *
     * Maps to the "description" column in the database.
     */
    private String description;

    /*
     * @Positive
     * ---------
     * A Bean Validation annotation that ensures the value is STRICTLY
     * greater than zero (> 0). Zero and negative numbers are rejected.
     *
     * This makes sense for a fine amount -- you cannot fine someone
     * $0 or a negative amount.
     *
     * If validation fails, Spring returns a 400 Bad Request with a
     * message like "must be greater than 0".
     *
     * Maps to the "fine_amount" column in the database.
     */
    @Positive
    private Double fineAmount;

    // =================================================================
    //  CONSTRUCTORS
    // =================================================================

    /*
     * DEFAULT (NO-ARG) CONSTRUCTOR
     * ----------------------------
     * JPA **requires** every entity to have a public or protected
     * no-argument constructor. Here is why:
     *
     *   When JPA loads data from the database, it needs to create an
     *   instance of this class FIRST (using this no-arg constructor),
     *   and THEN populate the fields via setters or reflection.
     *
     *   Without this constructor, JPA would throw an
     *   InstantiationException at runtime.
     *
     * Even though this constructor does nothing, it is essential.
     * If you only had the parameterized constructor below, Java
     * would NOT auto-generate a default constructor, and JPA would
     * break.
     */
    public TrafficRule() {}

    /*
     * PARAMETERIZED CONSTRUCTOR
     * -------------------------
     * A convenience constructor for creating TrafficRule objects in
     * your own code (e.g., in tests, seed data, or service methods).
     *
     * Notice we do NOT set the "id" here -- the database generates
     * it automatically thanks to @GeneratedValue.
     *
     * Example usage:
     *   TrafficRule rule = new TrafficRule(
     *       "Speed Limit",
     *       "Exceeding the posted speed limit",
     *       150.00
     *   );
     *   trafficRuleRepository.save(rule);  // id is assigned by the DB
     */
    public TrafficRule(String ruleName, String description, Double fineAmount) {
        this.ruleName = ruleName;
        this.description = description;
        this.fineAmount = fineAmount;
    }

    // =================================================================
    //  GETTERS AND SETTERS
    // =================================================================
    //
    //  Why do we need getters and setters?
    //
    //  1. ENCAPSULATION: The fields above are "private", meaning no
    //     outside class can access them directly. Getters and setters
    //     provide controlled access.
    //
    //  2. JPA / HIBERNATE: By default, Hibernate uses getter/setter
    //     methods to read from and write to entity fields when
    //     loading or saving data. Without them, Hibernate would have
    //     to fall back to direct field access via reflection, which
    //     some configurations may not support.
    //
    //  3. SPRING & JACKSON: When Spring converts a TrafficRule to JSON
    //     (for an API response), the Jackson library looks for getter
    //     methods to figure out which fields to include and what to
    //     name them. Without getters, your JSON would be empty.
    //
    //  4. VALIDATION & LOGIC: If you ever need to add logic when
    //     setting a value (e.g., trimming whitespace), you can add
    //     it inside the setter without changing any other code.
    // =================================================================

    /** Returns the auto-generated primary key (database id). */
    public Long getId() { return id; }

    /** Sets the primary key. Typically only used by JPA internally. */
    public void setId(Long id) { this.id = id; }

    /** Returns the name of the traffic rule (e.g., "Speed Limit"). */
    public String getRuleName() { return ruleName; }

    /** Sets the name of the traffic rule. Must not be blank (@NotBlank). */
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    /** Returns the optional description of the traffic rule. */
    public String getDescription() { return description; }

    /** Sets the optional description of the traffic rule. */
    public void setDescription(String description) { this.description = description; }

    /** Returns the fine amount in dollars (must be positive). */
    public Double getFineAmount() { return fineAmount; }

    /** Sets the fine amount. Must be a positive number (@Positive). */
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }
}
