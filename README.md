# RAG Chat Storage Microservice

Production-grade backend + frontend stack for storing chat sessions, messages, and Retrieval-Augmented Generation (RAG) context items. This powers a Groq LLM backed chatbot with persistent session history.

## Overview
This service manages:
- **Chat Sessions**: create, list, rename, favorite, delete
- **Chat Messages**: persist sender, content, timestamp, optional contextual RAG snippets
- **Context Items**: per-message retrieval snippets (sourceId, snippet, metadata)
- **User Isolation**: messages tagged with a `userId` enforced to match owning session
- **Rate Limiting**: Nginx reverse proxy front-line throttling (IP based)
- **Security**: API key authentication (header `X-API-KEY`)
- **Observability**: structured async logging + health endpoints
- **Docs**: Interactive Swagger/OpenAPI UI

## Current Tech Stack
| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.x (Web, Validation, Data JPA) |
| Database | MySQL 8 (JPA + Flyway migrations) |
| Frontend | React + Vite + TailwindCSS |
| AI Model | Groq (Llama 3.x) via Groq API |
| Reverse Proxy | Nginx (rate limiting & routing) |
| DB Admin UI | Adminer (http://localhost:8081) |
| Build Tool | Gradle |
| API Docs | springdoc-openapi / Swagger UI |
| Containerization | Docker & docker-compose |
| Logging | SLF4J + structured controller/service logs |
| Migrations | Flyway (`src/main/resources/db/migration`) |
| Caching | Caffeine (in-memory) |

## Key Features
### Core
- ✅ Persist chat sessions & messages (relational schema)
- ✅ Store optional RAG context per message
- ✅ Rename & favorite sessions
- ✅ Delete sessions (cascade delete messages + context)
- ✅ Paginated message history (page/size with max limits)

### Platform / Infrastructure
- ✅ API Key auth (header `X-API-KEY` configurable)
- ✅ Nginx rate limiting (requests per minute + burst)
- ✅ Adminer MySQL UI for browsing tables & data
- ✅ Flyway migrations (versioned schema control)
- ✅ Centralized configuration (`chart/values.yaml` + env overrides)
- ✅ Structured logging with timing & success/error markers
- ✅ Global exception handling (uniform JSON error envelope)
- ✅ Pagination & CORS configuration
- ✅ Health endpoints (Actuator + lightweight `/health`)

### AI Integration (Groq)
- ✅ Real-time responses using Groq API (no mock after cleanup)
- ✅ Frontend uses `VITE_GROQ_API_KEY` (must be provided) & model selection
- ✅ Graceful error when API key missing

## Database Schema (MySQL)
Initial schema applied via Flyway migration `V1__initial_schema.sql`:
- `chat_sessions` (id, user_id, title, favorite, created_at, updated_at)
- `chat_messages` (id, session_id, sender, content, user_id, created_at)
- `context_items` (id, message_id, source_id, snippet, metadata JSON)

## Configuration
Primary defaults live in `chart/values.yaml`. Secrets (API keys) should NOT be committed—use environment variables locally (e.g. `export GROQ_API_KEY=...`).

### Critical Variables
| Variable | Purpose | Source / Override |
|----------|---------|-------------------|
| `DATABASE_URL` | JDBC MySQL URL | docker-compose env / values.yaml |
| `DATABASE_USERNAME` | DB user | docker-compose / values.yaml |
| `DATABASE_PASSWORD` | DB password | docker-compose / values.yaml |
| `SECURITY_API_KEY` | Primary API key | docker-compose / values.yaml |
| `SECURITY_API_KEYS` | Comma list of accepted keys | values.yaml |
| `GROQ_API_KEY` | Groq model invocation | (Must export locally) |
| `GROQ_MODEL` | Model id (default llama-3.3-70b-versatile) | values.yaml |
| `PAGINATION_DEFAULT_PAGE_SIZE` | Default page size | values.yaml |
| `PAGINATION_MAX_PAGE_SIZE` | Max allowed page size | values.yaml |

### Rate Limiting (Nginx)
Configured in `nginx.conf.template` using a shared zone & burst parameters:
- Requests per minute: `RATE_LIMIT_REQUESTS_PER_MINUTE` (default 60)
- Burst capacity: `RATE_LIMIT_BURST` (default 10)
Adjust values in `chart/values.yaml` then rebuild/restart.

## Quick Start
### 1. Export Required Secrets (Local Only)
```bash
export GROQ_API_KEY="your_groq_key_here"   # obtain from https://console.groq.com/
export SECURITY_API_KEY="changeme"         # or a new key
```

### 2. Launch Stack
```bash
docker-compose up -d --build
```
Services:
- Backend API: http://localhost (via Nginx proxy to app on 8082)
- Frontend UI: http://localhost:3000
- Adminer (DB UI): http://localhost:8081
- Swagger: http://localhost/swagger-ui.html
- Health: http://localhost/actuator/health & http://localhost/health

### 3. Verify Database
Open Adminer → System: MySQL → Server: `mysql` → DB: `rag_chat_storage` → User: `ragchat` → Password: `password` (defaults—change for prod).

### 4. Test API
```bash
API_KEY=changeme
curl -H "X-API-KEY: $API_KEY" "http://localhost/api/v1/sessions?userId=demo-user"
```

## Frontend (Groq Integration)
Environment variables consumed by Vite (already injected in Docker build):
- `VITE_GROQ_API_KEY` (must be set via env before build)
- `VITE_GROQ_MODEL` (defaults to llama-3.3-70b-versatile)
- `VITE_API_URL` (defaults http://localhost)
- `VITE_API_KEY` (matches backend API key)

If you see: `Groq API key not configured!` ensure you exported `GROQ_API_KEY` BEFORE running `docker-compose up`.

## API Authentication
All protected endpoints require:
```
X-API-KEY: <one of SECURITY_API_KEYS>
```
Unprotected: Health endpoints & Swagger UI.

## Sample Endpoints
### Create Session
```bash
curl -X POST http://localhost/api/v1/sessions \
  -H "X-API-KEY: $API_KEY" -H "Content-Type: application/json" \
  -d '{"userId":"demo-user","title":"My First Chat"}'
```
### Add Message
```bash
curl -X POST http://localhost/api/v1/sessions/<SESSION_ID>/messages \
  -H "X-API-KEY: $API_KEY" -H "Content-Type: application/json" \
  -d '{"sender":"USER","content":"Hello there"}'
```
### List Messages (Paginated)
```bash
curl -H "X-API-KEY: $API_KEY" \
  "http://localhost/api/v1/sessions/<SESSION_ID>/messages?page=0&size=20"
```

## Error Envelope
Standard JSON error structure:
```json
{
  "timestamp": "2025-11-24T12:34:56Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "User ID does not match session owner",
  "path": "/api/v1/sessions/abc/messages"
}
```

## Logging
Controller & Service layers produce structured logs:
- Start markers (🔵 / 🔍)
- Success (✅) with durations (ms)
- Errors (❌) with stack traces
- Performance timing for each request

Adjust log levels in `application.yml` or via env:
```
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_ROOT=INFO
```

## Global Exception Handling
Custom exceptions mapped to HTTP status codes:
| Exception | Status |
|-----------|--------|
| NotFoundException | 404 |
| BadRequestException | 400 |
| BusinessException | 422 |
| DuplicateResourceException | 409 |
| RateLimitExceededException | 429 |
| DatabaseException | 500 |
| Generic (Exception) | 500 |

## Database Operations & Migrations
On startup Flyway runs pending migrations. To add a new migration:
1. Create file `src/main/resources/db/migration/V2__description.sql`
2. Restart container / app

## Development (Local Without Docker)
```bash
# MySQL running locally (example with Docker):
docker run -d --name mysql -e MYSQL_ROOT_PASSWORD=password \ 
  -e MYSQL_DATABASE=rag_chat_storage -e MYSQL_USER=ragchat -e MYSQL_PASSWORD=password \ 
  -p 3307:3306 mysql:8.0

export DATABASE_URL="jdbc:mysql://localhost:3307/rag_chat_storage?useSSL=false&allowPublicKeyRetrieval=true"
export DATABASE_USERNAME=ragchat
export DATABASE_PASSWORD=password
export SECURITY_API_KEY=changeme
export GROQ_API_KEY=your_groq_key_here

./gradlew bootRun
```
Backend then on: http://localhost:8082 (if `SERVER_PORT=8082`).

## Testing
```bash
./gradlew test            # Run unit tests
open build/reports/tests/test/index.html  # View HTML report (macOS)
```

## Production Hardening Checklist
- Change all default passwords & API keys
- Use managed MySQL (Aurora / RDS / CloudSQL)
- Enable HTTPS at the load balancer / ingress
- Externalize secrets via vault / secret manager
- Configure structured log shipping (ELK / Loki)
- Tune rate limit & introduce distributed limiter (e.g. Redis) if needed
- Add monitoring (Prometheus + Grafana dashboards) & alerting

## Adminer Usage
Browse DB at http://localhost:8081
Login:
- System: MySQL
- Server: mysql
- Username: ragchat
- Password: password
- Database: rag_chat_storage

## Cleaning & Secrets
Do NOT commit real API keys. In `chart/values.yaml` keep:
```yaml
frontend:
  groq:
    apiKey: "${GROQ_API_KEY}"  # resolved from environment at runtime
```
Export locally:
```bash
export GROQ_API_KEY="your_real_key"
```

## Contributing
1. Fork
2. Create feature branch (`git checkout -b feature/x`)
3. Implement + add tests
4. Ensure `./gradlew test` passes
5. Submit PR

## License
Educational / interview use only.

## Support
- Swagger UI: http://localhost/swagger-ui.html
- Health: http://localhost/actuator/health
- Logs: `docker logs -f rag-chat-storage-app`
- DB browse: http://localhost:8081

---
**Note:** This README reflects the updated MySQL + Groq integration after cleanup (MongoDB & mock AI removed).
