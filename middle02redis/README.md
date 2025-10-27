# Book Cache & Session Service

This Spring Boot 3 project showcases how to combine PostgreSQL, Redis data caching, and Redis-backed HTTP sessions for an online bookstore scenario. Books are stored in the relational database, cached per-record in Redis with a 10-minute TTL, and frequently viewed items are tracked via a Redis sorted set to power the “popular books” feed. HTTP sessions are externalized to Redis (30-minute sliding window) so the application layer remains stateless and horizontally scalable. A lightweight cache monitor gives you TTL insight and cleanup tooling right from the API.

---

## Architecture at a Glance

- **API Layer** – REST controllers (`BookController`, `SessionController`, `CacheController`) expose CRUD, session, and cache-observability endpoints.
- **Service Layer** – `BookService`, `SessionService`, and `CacheMonitorService` encapsulate data-access, Redis caching, session TTL calculation, and monitoring.
- **Persistence** – PostgreSQL (via Spring Data JPA) stores canonical book data. Tests default to H2 for zero-config CI runs.
- **Caching & Sessions** – Redis holds individual book snapshots (`book:{id}`), a sorted set of request counts (`books:popularity`), a cache of popular lists (`books:popular`), and Spring Session data.
- **Config** – `RedisTemplate` is JSON-serialized for safe object storage, security is relaxed for quick experimentation, and scheduling is enabled to run cache cleanup tasks.

You can spin everything up locally with Docker (`docker-compose.yml` supplies Redis + PostgreSQL) and run the app with Gradle.

---

## Prerequisites

- JDK 17+  
- Gradle 8+ (wrapper included)  
- Docker (for optional Redis/PostgreSQL containers)  
- Postman or compatible client (collection provided)  

---

## Running the Stack

```bash
# 1. Start Redis and PostgreSQL (optional if you already have them running)
docker-compose up -d

# 2. Launch the Spring Boot application
cd middle02redis
./gradlew bootRun

# 3. Hit http://localhost:8000 with Postman/cURL
```

### Key Configuration (`src/main/resources/application.properties`)

```
server.port=8000
spring.datasource.url=jdbc:postgresql://localhost:2345/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

server.servlet.session.timeout=30m
spring.session.store-type=redis
spring.session.redis.namespace=bookstore
spring.session.redis.flush-mode=on-save
```

> **Tip:** Override any property via environment variables or a custom profile (e.g., `SPRING_DATASOURCE_URL`, `SPRING_SESSION_REDIS_NAMESPACE`). Tests use `src/test/resources/application.properties`, which swaps PostgreSQL for in-memory H2 and disables the cache monitor.

---

## API Surface

| Area | Endpoint | Method | Notes |
|------|----------|--------|-------|
| Books | `/books` | `GET` | List all books. |
|       | `/books/{id}` | `GET` | Cached read with Redis fallback to DB. |
|       | `/books` | `POST` | Create book, invalidate caches, seed popularity set. |
|       | `/books/{id}` | `PUT` | Update book and refresh cache entry. |
|       | `/books/{id}` | `DELETE` | Delete book, cache entry, popularity stats. |
|       | `/books/popular?limit=5` | `GET` | Returns most-requested books (Redis sorted set + cached list). |
| Sessions | `/sessions/login` | `POST` | Creates/renews Redis-backed HTTP session with username. |
|          | `/sessions/data` | `POST` | Appends arbitrary strings to the session’s data list. |
|          | `/sessions/me` | `GET` | Shows session id, username, stored data, TTL minutes. |
|          | `/sessions/logout` | `DELETE` | Invalidates the current session. |
| Cache Ops | `/cache/monitor?pattern=book:*&unit=SECONDS` | `GET` | Returns TTL per key (book caches, session keys, etc.). |
|           | `/cache/cleanup?pattern=book:*` | `POST` | Removes expired keys up-front. Scheduled cleanup runs every 60 seconds when `cache.monitor.enabled=true`. |

- Book records are cached under `book:{id}` for 10 minutes. A “popular list” cache (`books:popular`) persists for 5 minutes to avoid hot loops.
- Session TTL auto-extends on activity (30-minute window). TTL reported by `/sessions/me` is fetched straight from Redis; if Redis is unavailable, it gracefully falls back to the servlet container timeout.
- Security is intentionally permissive (`permitAll`) so you can focus on infrastructure behavior. Add authentication once you move beyond prototyping.

---

## Cache & Session Workflow Details

1. **Read Through** – `BookService#getBook` checks Redis → DB, caches misses, and increments a Redis sorted-set score (`books:popularity`).  
2. **Write-Through** – `createBook`/`updateBook` persist to PostgreSQL, then replace the Redis cache entry and clear the `books:popular` list to keep aggregates in sync.  
3. **Delete** – Removes DB row, per-book cache, popularity footprint, and popular list caches.  
4. **Session Storage** – `SessionService` persists usernames and `session:dataList` payloads into Spring Session (Redis). All session APIs rely on the HTTP cookie automatically managed by your client.  
5. **Monitoring** – `CacheMonitorService` can be scheduled (default) and/or triggered manually to inspect TTLs or delete expired keys by pattern. Disable it by setting `cache.monitor.enabled=false`.  

---

## Testing & Tooling

```bash
# Runs unit/integration tests with H2 + disabled cache monitor
./gradlew test
```

- Tests rely on the Gradle wrapper and do not need Docker services running.
- Linting/formatting follows standard Spring Boot conventions (no extra tooling required).

---

## Postman Collection

A ready-to-import Postman collection lives at `postman/book-cache-session.postman_collection.json`. It defines:

- A `baseUrl` variable (`http://localhost:8000`) you can override per environment.
- Full CRUD flows for `/books`, including sample payloads.
- Session workflow requests (login, add data, inspect session, logout). Remember to allow Postman to store cookies so the same session is reused.
- Cache monitoring/cleanup requests. Adjust the `pattern` and `unit` query params as needed.

Import it via **Postman → Import → Files** and select the JSON file, or use the “Raw text” importer to paste the file contents.

---

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|--------------|-----|
| `org.postgresql.util.PSQLException: Connection refused` | Docker DB not running or wrong port. | Ensure `docker-compose up -d` succeeded, confirm port `2345` is free, or update `spring.datasource.url`. |
| `RedisConnectionFailureException` on startup | Redis container offline. | Start Redis, or point `spring.data.redis.host/port` to a reachable instance. |
| Session endpoints return new IDs every call | Client isn’t preserving cookies. | In Postman, leave “Automatically follow redirects” ON and ensure “Enable cookie management” remains enabled. |
| Cache monitor returns empty map | No keys match the supplied pattern or monitor disabled. | Use broader patterns (`*`) or set `cache.monitor.enabled=true`. |

---

## Next Steps

- Add authentication/authorization once you move beyond demos.
- Layer in pagination/search for `/books` and analytics for popular lists.
- Extend monitoring to emit metrics (Micrometer/Prometheus) instead of (or in addition to) the simple REST endpoints.

Happy caching! 😄
