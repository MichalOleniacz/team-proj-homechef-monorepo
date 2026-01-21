# HomeChef Infrastructure

Local development infrastructure using Docker Compose.

## Prerequisites

- Docker Desktop 4.x+
- Docker Compose v2+

## Quick Start

```bash
# Start everything (recommended for first-time setup)
docker compose --profile all up -d

# Check status
docker compose ps

# View logs
docker compose logs -f
```

## Development Modes

### Mode 1: Frontend Dev (Everything Containerized)

Best for frontend developers who just need the API running.

```bash
docker compose --profile all up -d
```

**What starts:**
- Java app (production build)
- PostgreSQL + Redis
- Kafka + Kafka UI
- Grafana + Prometheus + Loki (observability)

**Access:**
| Service | URL |
|---------|-----|
| API | http://localhost:8000 |
| Kafka UI | http://localhost:9094 |
| Grafana | http://localhost:9990 |

---

### Mode 2: Backend Dev (Java on Host)

Best for backend developers who want IDE debugging and hot-reload.

```bash
# Terminal 1: Start infrastructure
docker compose --profile infra up -d

# Terminal 2: Run Java app locally
cd ../core
./mvnw spring-boot:run
```

**What starts in Docker:**
- PostgreSQL + Redis
- Kafka + Kafka UI
- Exporters (metrics)

**Connection strings for local Java app:**
| Service | Connection |
|---------|------------|
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |
| Kafka | `localhost:29092` |

---

### Mode 3: Backend Dev + Observability

Same as Mode 2, plus metrics and logging.

```bash
docker compose --profile infra --profile ops up -d

cd ../core
./mvnw spring-boot:run
```

**Additional services:**
| Service | URL |
|---------|-----|
| Grafana | http://localhost:9990 |
| Prometheus | http://localhost:9991 |
| Loki | http://localhost:9993 |

---

### Mode 4: Hot-Reload Container

Java app runs in Docker with source code mounted for automatic restarts.

```bash
docker compose --profile infra --profile core up -d
```

Source changes in `core/src/` trigger automatic restart via Spring DevTools.

---

### Mode 5: Just Databases

Minimal setup for quick tests.

```bash
docker compose --profile db up -d
```

**What starts:**
- PostgreSQL (localhost:5432)
- Redis (localhost:6379)

---

## Docker Compose Profiles

| Profile | Services | Use Case |
|---------|----------|----------|
| `db` | PostgreSQL, Redis, exporters | Minimal databases |
| `infra` | db + Kafka + Kafka UI | Backend dev (Java on host) |
| `ops` | Grafana, Loki, Promtail, Prometheus, Node Exporter | Observability stack |
| `core` | Java app with hot-reload | Dev container with source mount |
| `full` | infra + ops | Backend dev with full observability |
| `all` | infra + ops + production Java app | Complete stack |

**Combining profiles:**
```bash
# infra + ops
docker compose --profile infra --profile ops up -d

# db + ops (databases + observability, no Kafka)
docker compose --profile db --profile ops up -d
```

---

## Port Reference

| Service | Internal Port | External Port | Protocol |
|---------|---------------|---------------|----------|
| **Application** |
| Java App | 8000 | 8000 | HTTP |
| **Databases** |
| PostgreSQL | 5432 | 5432 | TCP |
| Redis | 6379 | 6379 | TCP |
| **Messaging** |
| Kafka (container) | 9092 | 9092 | TCP |
| Kafka (host) | 29092 | 29092 | TCP |
| Kafka UI | 8080 | 9094 | HTTP |
| **Observability** |
| Grafana | 3000 | 9990 | HTTP |
| Prometheus | 9090 | 9991 | HTTP |
| Node Exporter | 9100 | 9992 | HTTP |
| Loki | 3100 | 9993 | HTTP |
| **Exporters** |
| Postgres Exporter | 9187 | 9187 | HTTP |
| Redis Exporter | 9121 | 9121 | HTTP |

---

## Service Details

### PostgreSQL

- **Image:** `postgres:16`
- **Database:** `core`
- **Credentials:** `postgres` / `postgres`
- **Data:** Persisted in `./data/postgres/`
- **Init script:** `./config/postgres/init.sql`

```bash
# Connect via psql
docker compose exec homechef-datastore-core psql -U postgres -d core

# Reset database
docker compose --profile db down -v
docker compose --profile db up -d
```

### Redis

- **Image:** `redis:7`
- **Port:** 6379
- **No persistence** (cache-only)

```bash
# Connect via redis-cli
docker compose exec homechef-cache-core redis-cli
```

### Kafka

- **Image:** `apache/kafka:3.7.0`
- **Mode:** KRaft (no Zookeeper)
- **Topics:** Auto-created on first use
- **Data:** Persisted in `./data/kafka/`

**Listeners:**
| Listener | Port | Use |
|----------|------|-----|
| PLAINTEXT | 9092 | Container-to-container |
| PLAINTEXT_HOST | 29092 | Host access |

```bash
# List topics
docker compose exec homechef-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list

# Create topic manually
docker compose exec homechef-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --topic my-topic --partitions 1 --replication-factor 1

# Consume messages
docker compose exec homechef-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic parse-requests --from-beginning
```

### Kafka UI

- **URL:** http://localhost:9094
- **Features:** Browse topics, view messages, consumer groups

### Grafana

- **URL:** http://localhost:9990
- **Credentials:** `admin` / `admin` (first login)
- **Datasources:** Pre-configured (Prometheus, Loki)
- **Dashboards:** `./config/grafana/dashboards/`

### Prometheus

- **URL:** http://localhost:9991
- **Scrape interval:** 10s
- **Targets:** Java app, PostgreSQL, Redis, Node Exporter
- **Config:** `./config/prometheus/prometheus.yml`

### Loki + Promtail

- **Loki URL:** http://localhost:9993
- **Log collection:** All containers with `loki_scope` label
- **Config:** `./config/promtail/promtail-config.yml`

---

## Common Operations

### Start/Stop

```bash
# Start with specific profile
docker compose --profile <profile> up -d

# Stop containers (keep volumes)
docker compose --profile <profile> down

# Stop and remove volumes (fresh start)
docker compose --profile <profile> down -v

# Restart single service
docker compose restart homechef-kafka
```

### Logs

```bash
# All logs
docker compose logs -f

# Specific service
docker compose logs -f homechef-core

# Last 100 lines
docker compose logs --tail 100 homechef-kafka
```

### Rebuild

```bash
# Rebuild Java app container
docker compose --profile all build homechef-core

# Rebuild and restart
docker compose --profile all up -d --build homechef-core

# Force rebuild (no cache)
docker compose --profile all build --no-cache homechef-core
```

### Health Checks

```bash
# Check service health
docker compose ps

# Detailed health status
docker inspect homechef-kafka --format='{{.State.Health.Status}}'
```

### Reset Everything

```bash
# Nuclear option: remove all containers, volumes, networks
docker compose --profile all down -v --remove-orphans
rm -rf ./data/postgres ./data/kafka ./data/prometheus ./data/grafana-storage

# Restart fresh
docker compose --profile all up -d
```

---

## Troubleshooting

### Kafka won't start

```bash
# Check logs
docker compose logs homechef-kafka

# Common fix: remove old data
rm -rf ./data/kafka/*
docker compose --profile infra up -d
```

### PostgreSQL connection refused

```bash
# Wait for health check
docker compose ps  # Should show "healthy"

# Check if port is in use
lsof -i :5432
```

### Java app can't connect to services

When running Java on host:
- Use `localhost:5432` for PostgreSQL
- Use `localhost:6379` for Redis
- Use `localhost:29092` for Kafka (not 9092!)

When running Java in container:
- Uses container names (handled by environment variables)

### Prometheus not scraping Java app

If Java runs on host, Prometheus uses `host.docker.internal:8000`.
On Linux, you may need to add:

```yaml
# docker-compose.override.yml
services:
  homechef-ops-prom-prometheus:
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

### Out of disk space

```bash
# Clean up Docker
docker system prune -a --volumes
```

---

## Directory Structure

```
infrastructure/
├── config/
│   ├── grafana/
│   │   ├── dashboards/           # JSON dashboard definitions
│   │   └── provisioning/
│   │       ├── datasources/      # Prometheus, Loki configs
│   │       └── dashboards.yml    # Dashboard provisioner
│   ├── postgres/
│   │   └── init.sql              # Database initialization
│   ├── prometheus/
│   │   └── prometheus.yml        # Scrape targets
│   └── promtail/
│       └── promtail-config.yml   # Log collection config
├── data/                         # Persistent volumes (gitignored)
│   ├── grafana-storage/
│   ├── kafka/
│   ├── postgres/
│   └── prometheus/
├── docker-compose.yml
└── README.md
```

---

## Environment Variables

The Java app accepts these environment variables (set automatically when running in Docker):

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | local | Spring profile |
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://localhost:5432/core | PostgreSQL URL |
| `SPRING_DATASOURCE_USERNAME` | postgres | DB username |
| `SPRING_DATASOURCE_PASSWORD` | postgres | DB password |
| `SPRING_DATA_REDIS_HOST` | localhost | Redis host |
| `SPRING_DATA_REDIS_PORT` | 6379 | Redis port |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:29092 | Kafka bootstrap servers |

When running Java on host, defaults are used. When running in Docker, these are overridden to use container names.
