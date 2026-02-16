package com.example.demo.service;

import com.example.demo.model.TrafficRule;
import com.example.demo.repository.TrafficRuleRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * TRAFFIC RULE SERVICE - Business logic for managing traffic rules.
 * =====================================================================
 *
 * What is a Service?
 *   The Service layer sits between Controller and Repository:
 *
 *     Controller  -->  Service  -->  Repository  -->  Database
 *     (handles      (business      (database         (stores
 *      HTTP          logic)         queries)          data)
 *      requests)
 *
 *   WHY do we need a Service when the Controller could call the Repository directly?
 *   1. Separation of concerns - keeps controllers thin and focused on HTTP
 *   2. Reusability - multiple controllers can use the same service method
 *   3. Testing - easier to write unit tests for isolated business logic
 *   4. In real projects, you'd add validation, caching, logging here
 *
 * What is @Service?
 *   Same as @Component - marks this as a Spring-managed bean.
 *   Spring creates one instance and injects it wherever needed.
 */
@Service
public class TrafficRuleService {

    // Repository for database operations on traffic_rules table
    private final TrafficRuleRepository trafficRuleRepository;

    /** Constructor injection - Spring passes the repository automatically */
    public TrafficRuleService(TrafficRuleRepository trafficRuleRepository) {
        this.trafficRuleRepository = trafficRuleRepository;
    }

    /** Get all traffic rules - used in Rules page and Add Violation dropdown */
    public List<TrafficRule> getAllRules() {
        return trafficRuleRepository.findAll();
    }

    /** Get a single rule by ID - used when recording a violation */
    public Optional<TrafficRule> getRuleById(Long id) {
        return trafficRuleRepository.findById(id);
    }

    /** Save a new rule or update an existing one - used in Add Rule form */
    public TrafficRule saveRule(TrafficRule rule) {
        return trafficRuleRepository.save(rule);
    }

    /** Delete a rule by ID - used when admin clicks "Delete" button */
    public void deleteRule(Long id) {
        trafficRuleRepository.deleteById(id);
    }
}
