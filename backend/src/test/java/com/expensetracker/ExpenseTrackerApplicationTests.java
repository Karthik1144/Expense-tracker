package com.expensetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class ExpenseTrackerApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context starts successfully.
        // Requires a reachable MySQL instance as configured in application.yml.
    }
}
