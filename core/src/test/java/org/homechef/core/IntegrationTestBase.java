package org.homechef.core;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests providing TestContainers setup.
 * Uses singleton container pattern: containers are started once per JVM
 * and reused across all tests (including nested classes).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer<?> postgres;
    static final KafkaContainer kafka;

    static {
        // Singleton containers - started once per JVM, fresh each test run
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
                .withDatabaseName("core_test")
                .withUsername("test")
                .withPassword("test");
        postgres.start();

        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        // Disable Redis for now (integration tests focus on DB)
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");

        // Flyway should run migrations
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
