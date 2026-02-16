package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * =====================================================================
 * USER REPOSITORY - Interface for database operations on the "users" table.
 * =====================================================================
 *
 * What is a Repository?
 *   A Repository is a layer between your application and the database.
 *   Instead of writing raw SQL queries, you define method names and
 *   Spring Data JPA AUTOMATICALLY generates the SQL for you.
 *
 * What is JpaRepository<User, Long>?
 *   - User = The Entity class this repository works with
 *   - Long = The data type of the primary key (id field in User)
 *
 * What do you get for FREE by extending JpaRepository?
 *   - save(user)        --> INSERT INTO users ...  (or UPDATE if exists)
 *   - findById(1L)      --> SELECT * FROM users WHERE id = 1
 *   - findAll()         --> SELECT * FROM users
 *   - deleteById(1L)    --> DELETE FROM users WHERE id = 1
 *   - count()           --> SELECT COUNT(*) FROM users
 *   ... and many more!
 *
 * MAGIC OF SPRING DATA JPA:
 *   You just write the method signature (name + parameters),
 *   and Spring auto-generates the implementation. No SQL needed!
 *
 *   Example: findByEmail(String email)
 *   Spring reads the method name and generates:
 *     SELECT * FROM users WHERE email = ?
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Returns Optional<User> because the user might not exist.
     *
     * Used in: CustomUserDetailsService (for login authentication)
     *          UserController (to get current logged-in user)
     *
     * Generated SQL: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     * Returns true/false. Used during registration to prevent duplicate emails.
     *
     * Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);

    /**
     * Count how many users have a specific role (ADMIN or USER).
     * Used in Admin Dashboard to show "Total Registered Users".
     *
     * Example: countByRole(User.Role.USER) --> counts all regular users
     * Generated SQL: SELECT COUNT(*) FROM users WHERE role = ?
     */
    long countByRole(User.Role role);

    /**
     * Find a user by phone number.
     * Used for forgot password when user provides phone instead of email.
     *
     * Generated SQL: SELECT * FROM users WHERE phone = ?
     */
    Optional<User> findByPhone(String phone);
}
