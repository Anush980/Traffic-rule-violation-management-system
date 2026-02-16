package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * =====================================================================
 * MAIN APPLICATION CLASS - Entry point of the entire Spring Boot app.
 * =====================================================================
 *
 * What is @SpringBootApplication?
 *   It is a shortcut that combines 3 annotations:
 *   1. @Configuration     - Tells Spring this class has bean definitions
 *   2. @EnableAutoConfiguration - Spring Boot auto-configures beans based on
 *                                 dependencies in pom.xml (e.g., JPA, Security)
 *   3. @ComponentScan     - Scans all packages under "com.example.demo"
 *                           to find @Controller, @Service, @Repository, etc.
 *
 * When you run this class, Spring Boot:
 *   1. Starts an embedded Tomcat server on port 8080
 *   2. Sets up H2 database and creates tables from our @Entity classes
 *   3. Configures Spring Security (login, roles, etc.)
 *   4. Loads Thymeleaf templates from /resources/templates/
 *   5. Runs DataInitializer to seed admin account + sample rules
 */
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		// This single line does everything - starts server, DB, security, etc.
		SpringApplication.run(DemoApplication.class, args);
	}

}
