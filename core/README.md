# HomeChef Core Service

Backend service for recipe parsing and management.

## Prerequisites

- Java 17+
- Docker & Docker Compose (for local infrastructure)
- Maven (or use included `./mvnw`)

## Quick Start

### 1. Start infrastructure

```bash
cd ../infrastructure
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Kafka (port 9092)

### 2. Run the service

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8000`.

## Local Database Seeding

When running with `spring.profiles.active=local` (default), the service **automatically seeds mock recipe data** on startup.

### Seeded Recipes

| Recipe | URL |
|--------|-----|
| World's Best Lasagna | allrecipes.com |
| Chicken Tikka Masala | seriouseats.com |
| Classic Carbonara | bonappetit.com |
| Fresh Guacamole | foodnetwork.com |
| Perfect Buttermilk Pancakes | epicurious.com |
| Homemade Pad Thai | budgetbytes.com |
| Classic Chocolate Chip Cookies | kingarthurbaking.com |
| Homemade Pizza | simplyrecipes.com |

### Behavior

- **Idempotent**: Safe to restart multiple times. Existing data is skipped.
- **Profile-gated**: Only runs when `spring.profiles.active=local`.
- **Logs**:
  ```
  DataSeeder: Checking if seed data is needed...
  DataSeeder: Seeded recipe 'World's Best Lasagna' from https://...
  DataSeeder: Seeded 8 new recipes
  ```

### Resetting Seed Data

To re-seed from scratch:

```bash
# Stop the service, then:
docker-compose -f ../infrastructure/docker-compose.yml exec postgres \
  psql -U postgres -d core -c "TRUNCATE resource CASCADE;"

# Restart the service
./mvnw spring-boot:run
```

### Disabling Seeding

Run with a different profile:

```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

Or set in `application.properties`:
```properties
spring.profiles.active=dev
```

## Importing Offline-Prepared Data

For sideloading recipes parsed offline (e.g., by an LLM scraping pipeline), use the JSON import feature.

### JSON Format

Create a JSON file with this structure:

```json
[
  {
    "url": "https://example.com/recipe/123",
    "title": "Recipe Title",
    "ingredients": [
      { "quantity": "2", "unit": "cups", "name": "flour" },
      { "quantity": "1/2", "unit": "tsp", "name": "salt" },
      { "name": "butter for greasing" }
    ]
  }
]
```

**Field notes:**
- `url` (required): Original recipe URL (used for deduplication via hash)
- `title` (required): Recipe title
- `ingredients[].quantity`: Supports decimals (`"0.5"`), fractions (`"1/2"`), mixed (`"1 1/2"`), or omit for "to taste"
- `ingredients[].unit`: Omit or set to `null` for unitless items ("3 eggs")
- `ingredients[].name` (required): Ingredient description

See `src/main/resources/data/recipes-import.sample.json` for a complete example.

### Configuring the Import

Set the import file path in `application.properties`:

```properties
# From classpath (bundled in JAR)
homechef.import.recipes-file=classpath:data/recipes-import.json

# From filesystem (external file)
homechef.import.recipes-file=file:/path/to/recipes.json

# Disable import
homechef.import.enabled=false
```

Or via command line:

```bash
./mvnw spring-boot:run \
  -Dspring.profiles.active=local \
  -Dhomechef.import.recipes-file=file:./my-recipes.json
```

### Import Behavior

- **Runs on startup**: Before the hardcoded DataSeeder
- **Idempotent**: Skips recipes where URL hash already exists
- **Fault-tolerant**: Logs failures per recipe, continues with remaining
- **Logs**:
  ```
  RecipeImporter: Loading recipes from file:./recipes.json
  RecipeImporter: Imported 'Spaghetti Bolognese' from https://...
  RecipeImporter: Complete - imported=15, skipped=3, failed=0
  ```

### Workflow for Offline Scraping

1. Scrape recipe URLs from target websites
2. Parse each page with LLM, output JSON array
3. Place file in project or external path
4. Configure `homechef.import.recipes-file`
5. Start service - data loads automatically

## API Endpoints

### Submit URL for parsing

```http
POST /api/v1/recipes/parse
Content-Type: application/json

{
  "url": "https://www.allrecipes.com/recipe/23600/worlds-best-lasagna/"
}
```

**Response (200 OK)** - cached/seeded recipe:
```json
{
  "status": "COMPLETED",
  "recipe": {
    "urlHash": "a1b2c3...",
    "title": "World's Best Lasagna",
    "ingredients": [
      { "quantity": "1", "unit": "pound", "name": "sweet Italian sausage" },
      { "quantity": null, "unit": null, "name": "salt to taste" }
    ],
    "parsedAt": "2026-01-19T18:19:00Z"
  }
}
```

**Response (202 Accepted)** - new URL, parsing started:
```json
{
  "status": "PENDING",
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Poll parse status

```http
GET /api/v1/recipes/parse-requests/{requestId}
```

**Response:**
```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "recipe": { ... }
}
```

## Configuration

Key properties in `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8000 | HTTP port |
| `spring.profiles.active` | local | Active profile |
| `spring.datasource.url` | jdbc:postgresql://localhost:5432/core | DB connection |
| `homechef.recipe.ttl-days` | 30 | Recipe cache TTL |
| `homechef.import.recipes-file` | _(empty)_ | Path to JSON import file |
| `homechef.import.enabled` | true | Enable/disable import on startup |

## Architecture

See [ADR-001: Hexagonal Architecture](docs/adr/ADR-001-hexagonal-architecture-and-ddd.md) for design decisions.
