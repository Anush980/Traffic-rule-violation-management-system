package com.example.demo.repository;

import com.example.demo.model.TrafficRule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * =====================================================================
 * TRAFFIC RULE REPOSITORY - Database operations for "traffic_rules" table.
 * =====================================================================
 *
 * This is the simplest repository - no custom methods needed!
 * All we need are the built-in JpaRepository methods:
 *   - findAll()       --> Get all traffic rules (for rules list page)
 *   - findById(id)    --> Get a specific rule (when recording a violation)
 *   - save(rule)      --> Add a new rule or update existing one
 *   - deleteById(id)  --> Delete a rule
 *   - count()         --> Count total rules (used in DataInitializer)
 *
 * Spring Data JPA provides all these automatically - no code to write!
 */
public interface TrafficRuleRepository extends JpaRepository<TrafficRule, Long> {
    // No custom methods needed - JpaRepository provides everything we need
}
