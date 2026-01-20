package org.homechef.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Smoke test verifying that the Spring application context loads successfully.
 * Extends IntegrationTestBase to use TestContainers for PostgreSQL and Kafka.
 */
@DisplayName("Core Application")
class CoreApplicationTests extends IntegrationTestBase {

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
        // Context loads without throwing - validates wiring
    }

}
