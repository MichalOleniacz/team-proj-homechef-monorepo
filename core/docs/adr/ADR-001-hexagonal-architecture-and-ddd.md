# ADR-001: Hexagonal Architecture with Domain-Driven Design

**Status:** Accepted
**Date:** 2025-01-06
**Deciders:** Michal Oleniacz

## Context

HomeChef Core is a Spring Boot backend service that:
- Receives recipe URLs from clients
- Orchestrates async parsing via Kafka + LLM
- Caches results in Redis
- Persists data in PostgreSQL
- Will evolve to support user accounts and shopping list aggregation

We need an architecture that:
1. Is easy to understand and navigate
2. Enforces clear boundaries (domain logic vs infrastructure)
3. Scales with feature complexity without major refactoring
4. Serves as a learning vehicle for production patterns

## Decision

We adopt **Hexagonal Architecture** (Ports & Adapters) combined with **Domain-Driven Design** tactical patterns.

### Core Principles

1. **Dependency Rule:** Dependencies point inward. Domain knows nothing about infrastructure.
2. **Ports:** Interfaces that define how the application interacts with the outside world.
3. **Adapters:** Implementations that connect ports to real infrastructure (DB, Kafka, HTTP).
4. **Domain Isolation:** Business logic lives in the domain layer, free of framework annotations.

### Package Structure

```
org.homechef.core
│
├── domain/                          # Pure business logic, no framework deps
│   ├── recipe/
│   │   ├── Recipe.java              # Aggregate root
│   │   ├── Ingredient.java          # Entity within aggregate
│   │   ├── ParseRequest.java        # Entity
│   │   ├── ParseStatus.java         # Enum
│   │   ├── UrlHash.java             # Value Object
│   │   └── RecipeRepository.java    # Repository interface (driven port)
│   │
│   ├── shoppinglist/                # Future: Phase 3
│   │   └── ...
│   │
│   └── shared/                      # Cross-cutting domain concepts
│       ├── DomainEvent.java         # Marker interface
│       └── AggregateRoot.java       # Base class
│
├── application/                     # Use cases, orchestration
│   ├── port/
│   │   ├── in/                      # Driving ports (use cases)
│   │   │   ├── ParseRecipeUseCase.java
│   │   │   ├── GetParseStatusUseCase.java
│   │   │   └── dto/                 # Command/Query objects
│   │   │       ├── ParseRecipeCommand.java
│   │   │       └── ParseStatusResponse.java
│   │   │
│   │   └── out/                     # Driven ports (infrastructure interfaces)
│   │       ├── RecipeRepository.java      # Could also live in domain
│   │       ├── RecipeCachePort.java
│   │       ├── RecipeParseRequestPort.java
│   │       └── EventPublisherPort.java
│   │
│   └── service/                     # Use case implementations
│       ├── ParseRecipeService.java
│       └── GetParseStatusService.java
│
├── adapter/                         # Infrastructure implementations
│   ├── in/                          # Driving adapters (receive requests)
│   │   ├── web/
│   │   │   ├── RecipeController.java
│   │   │   ├── dto/                 # REST-specific DTOs
│   │   │   │   ├── ParseRecipeRequest.java
│   │   │   │   └── ParseRecipeResponse.java
│   │   │   └── mapper/
│   │   │       └── RecipeWebMapper.java
│   │   │
│   │   └── kafka/
│   │       └── IngredientListConsumer.java
│   │
│   └── out/                         # Driven adapters (call external systems)
│       ├── persistence/
│       │   ├── RecipeJdbcRepository.java
│       │   ├── entity/              # DB entities (not domain!)
│       │   │   └── RecipeEntity.java
│       │   └── mapper/
│       │       └── RecipePersistenceMapper.java
│       │
│       ├── kafka/
│       │   └── RecipeParseRequestProducer.java
│       │
│       ├── redis/
│       │   └── RecipeCacheAdapter.java
│       │
│       └── event/
│           └── SpringEventPublisher.java
│
└── config/                          # Spring configuration
    ├── KafkaConfig.java
    ├── RedisConfig.java
    └── SecurityConfig.java
```

### DDD Tactical Patterns

| Pattern | Usage | Example |
|---------|-------|---------|
| **Aggregate** | Consistency boundary, accessed via root | `Recipe` (root) contains `Ingredient[]` |
| **Entity** | Has identity, mutable | `ParseRequest`, `Ingredient` |
| **Value Object** | Immutable, equality by value | `UrlHash`, `Quantity`, `Unit` |
| **Domain Event** | Signals something happened | `RecipeParsedEvent`, `ParseRequestCreatedEvent` |
| **Repository** | Interface in domain/application, impl in adapter | `RecipeRepository` |
| **Domain Service** | Stateless logic spanning aggregates | `IngredientAggregationService` (Phase 3) |

### Light CQRS

- **Commands:** Mutate state, go through domain layer
- **Queries:** Can bypass domain, read directly via optimized paths
- **Single datastore:** No event sourcing, same DB for reads/writes
- **Separate DTOs:** REST responses use dedicated response objects, not domain entities

```
Command Flow:
  Controller → UseCase (port.in) → Service → Domain → Repository (port.out) → Adapter

Query Flow (simple reads):
  Controller → QueryService → Repository → Adapter (can skip domain if no logic needed)
```

### Domain Events

Events enable loose coupling between bounded contexts and trigger async side effects.

```java
// Domain event
public record RecipeParsedEvent(
    UUID recipeId,
    String urlHash,
    Instant occurredAt
) implements DomainEvent {}

// Published after Recipe aggregate is saved
// Listeners can: update cache, notify other contexts, trigger analytics
```

**Event flow:**
1. Domain creates event during business operation
2. Application service collects events from aggregate
3. After transaction commits, events are published
4. Adapters (in-process or Kafka) handle events

### Dependency Rule Enforcement (ArchUnit)

Automated tests prevent architectural violations:

```java
// src/test/java/org/homechef/core/ArchitectureTest.java
package org.homechef.core;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
            .importPackages("org.homechef.core");
    }

    @Test
    void domainShouldNotDependOnApplication() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..application..")
            .check(classes);
    }

    @Test
    void domainShouldNotDependOnAdapters() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..")
            .check(classes);
    }

    @Test
    void applicationShouldNotDependOnAdapters() {
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..")
            .check(classes);
    }

    @Test
    void layeredArchitecture() {
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Adapter").definedBy("..adapter..")
            .layer("Config").definedBy("..config..")

            .whereLayer("Domain").mayNotAccessAnyLayer()
            .whereLayer("Application").mayOnlyAccessLayers("Domain")
            .whereLayer("Adapter").mayOnlyAccessLayers("Application", "Domain")
            .whereLayer("Config").mayOnlyAccessLayers("Adapter", "Application", "Domain")

            .check(classes);
    }
}
```

**Maven dependency to add:**
```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

## Consequences

### Positive

1. **Clear mental model:** Three layers, explicit boundaries, easy to explain to juniors
2. **Testability:** Domain and application layers testable without Spring context
3. **Flexibility:** Swap adapters (e.g., switch from Redis to Memcached) without touching domain
4. **Evolvability:** Add new features by adding new ports/adapters
5. **Automated enforcement:** ArchUnit catches violations at build time

### Negative

1. **More files:** Mappers, DTOs, ports add boilerplate vs. anemic CRUD
2. **Learning curve:** Team needs to understand layer responsibilities
3. **Potential over-engineering:** For simple CRUD, hexagonal is overkill (but we're not doing simple CRUD)

### Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Juniors put logic in adapters | ArchUnit tests fail build; code review |
| Mapper explosion | Use MapStruct for generated mappers |
| Domain events lost | Transactional outbox pattern (future, if needed) |

## References

- Alistair Cockburn, "Hexagonal Architecture" (original)
- Vaughn Vernon, "Implementing Domain-Driven Design"
- Tom Hombergs, "Get Your Hands Dirty on Clean Architecture" (Spring-specific)