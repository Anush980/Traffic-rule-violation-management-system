package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Collections;

/**
 * =====================================================================
 * CUSTOM USER DETAILS SERVICE - Connects our User entity to Spring Security.
 * =====================================================================
 *
 * What is this class?
 *   Spring Security needs to know HOW to load a user from the database
 *   during login. This class tells Spring Security:
 *     "Here's how to find a user by their email, and here's their role."
 *
 * What is UserDetailsService?
 *   It's a Spring Security interface with one method: loadUserByUsername()
 *   Spring calls this method automatically when a user tries to log in.
 *
 * What is @Service?
 *   Marks this class as a Spring "bean" (managed object).
 *   Spring auto-detects it and creates an instance at startup.
 *   Similar to @Controller and @Repository, but used for business logic.
 *
 * HOW LOGIN WORKS (step by step):
 *   1. User enters email + password on login page
 *   2. Spring Security calls loadUserByUsername(email)
 *   3. We find the user in our database
 *   4. We return a UserDetails object with email, hashed password, and role
 *   5. Spring Security compares the entered password with the stored hash
 *   6. If they match --> login success, redirect to dashboard
 *   7. If they don't --> login failure, show error message
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Inject UserRepository to query the database
    private final UserRepository userRepository;

    /**
     * CONSTRUCTOR INJECTION: Spring automatically passes the UserRepository bean.
     * This is the recommended way to inject dependencies (instead of @Autowired).
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method is called by Spring Security during login.
     *
     * @param email - The email entered by the user on the login form
     *                (we configured "email" as the username parameter in SecurityConfig)
     * @return UserDetails - An object that Spring Security uses to verify the password
     *                       and determine the user's role/permissions
     * @throws UsernameNotFoundException - If no user exists with this email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Step 1: Find the user in our database by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Step 2: Convert our User entity to Spring Security's UserDetails object
        // We pass: email (as username), hashed password, and the role (ROLE_ADMIN or ROLE_USER)
        // Spring Security requires the "ROLE_" prefix for role-based access control
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),                  // username (we use email)
                user.getPassword(),               // BCrypt hashed password
                Collections.singletonList(        // list of authorities/roles
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    // If role is ADMIN --> "ROLE_ADMIN"
                    // If role is USER  --> "ROLE_USER"
                )
        );
    }
}
