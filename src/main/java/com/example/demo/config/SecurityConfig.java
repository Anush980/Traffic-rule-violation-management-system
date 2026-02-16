package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * =====================================================================
 * SECURITY CONFIGURATION - Controls who can access what in the application.
 * =====================================================================
 *
 * What is Spring Security?
 *   Spring Security handles authentication (who are you?) and
 *   authorization (what are you allowed to do?).
 *
 * What does this config do?
 *   1. Defines which URLs are public (login, register) and which need login
 *   2. Restricts /admin/** pages to ADMIN role only
 *   3. Restricts /user/** pages to USER role only
 *   4. Sets up the login page and logout behavior
 *   5. Redirects users to the correct dashboard after login (admin vs user)
 *   6. Configures password hashing (BCrypt)
 *
 * @Configuration  = Tells Spring this class contains bean definitions
 * @EnableWebSecurity = Activates Spring Security for the application
 *
 * What is a @Bean?
 *   A method annotated with @Bean returns an object that Spring manages.
 *   Spring calls these methods at startup and stores the returned objects.
 *   Other parts of the app can then use these objects (dependency injection).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * SECURITY FILTER CHAIN - The main security rules for the application.
     *
     * Think of it as a bouncer at a club:
     *   - Some people can enter without checking ID (public pages)
     *   - VIP area is only for ADMIN (admin pages)
     *   - Members area is only for USER (user pages)
     *   - Everyone else needs to show their ID first (login)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ---- URL ACCESS RULES ----
            .authorizeHttpRequests(auth -> auth
                // These URLs are PUBLIC - anyone can access without login
                .requestMatchers("/", "/login", "/register", "/forgot-password", "/verify-code", "/reset-password", "/css/**", "/js/**", "/h2-console/**").permitAll()
                // Only users with ROLE_ADMIN can access /admin/** pages
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Only users with ROLE_USER can access /user/** pages
                .requestMatchers("/user/**").hasRole("USER")
                // Everything else requires login
                .anyRequest().authenticated()
            )

            // ---- LOGIN CONFIGURATION ----
            .formLogin(form -> form
                .loginPage("/login")           // Our custom login page (not Spring's default)
                .usernameParameter("email")    // Login form uses "email" field instead of "username"
                // After successful login, redirect based on role:
                .successHandler((request, response, authentication) -> {
                    var authorities = authentication.getAuthorities();
                    // If user is ADMIN --> go to admin dashboard
                    if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/admin/dashboard");
                    } else {
                        // If user is USER --> go to user dashboard
                        response.sendRedirect("/user/dashboard");
                    }
                })
                .permitAll()                   // Allow everyone to see the login page
            )

            // ---- LOGOUT CONFIGURATION ----
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")  // After logout, go to login page with message
                .permitAll()
            )

            // ---- H2 CONSOLE SETTINGS ----
            // H2 console uses iframes and POST requests, so we need to:
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")  // Disable CSRF for H2 console
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())  // Allow iframes from same origin
            );

        return http.build();
    }

    /**
     * PASSWORD ENCODER - Hashes passwords using BCrypt algorithm.
     *
     * BCrypt is a one-way hash function:
     *   "admin123" --> "$2a$10$dXJ3SW6G7P50lGEk..." (can never be reversed!)
     *
     * During login, Spring hashes the entered password and compares it
     * with the stored hash. If they match, login succeeds.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AUTHENTICATION MANAGER - Handles the actual authentication process.
     * Spring Security uses this internally to validate credentials.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
