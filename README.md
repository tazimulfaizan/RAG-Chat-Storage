# RAG Chat Storage Microservice

Production-ready backend microservice to store chat sessions, messages, and RAG context for a Retrieval-Augmented Generation (RAG) based chatbot system.

## Overview

This microservice provides a complete solution for managing chat histories, including:
- **Chat Sessions**: Create and manage conversation sessions
- **Chat Messages**: Store messages with sender information and RAG context
- **Session Management**: Rename, favorite, and delete sessions
- **Message History**: Retrieve paginated message history
- **Security**: API key authentication and rate limiting
- **Documentation**: Interactive Swagger/OpenAPI documentation

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.4
- **Database**: MongoDB
- **Build Tool**: Gradle
- **Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
- **Authentication**: API Key-based
- **Rate Limiting**: Bucket4j (Token Bucket Algorithm)
- **Containerization**: Docker & docker-compose
- **Logging**: SLF4J with request/response logging

## Features

### Core Functionalities
✅ Start and maintain chat sessions for users  
✅ Save messages within a session with sender, content, and optional RAG context  
✅ Rename chat sessions  
✅ Mark/unmark sessions as favorite  
✅ Delete sessions and associated messages  
✅ Retrieve paginated message history  

### Technical Features
✅ API Key authentication (environment-configured)  
✅ Rate limiting to prevent API abuse  
✅ Centralized logging with request tracking  
✅ Global error handling  
✅ CORS configuration  
✅ Pagination support  
✅ Health check endpoints  
✅ Swagger/OpenAPI documentation  
✅ Docker support with MongoDB and mongo-express  
✅ Unit tests for business logic  

## Environment Variables

All environment variables are documented in `.env.example`. Copy it and configure:


### Required Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `SPRING_DATA_MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/rag-chat-storage` | `mongodb://mongo:27017/rag-chat-storage` |
| `SECURITY_API_KEY` | API key for authentication | `changeme` | `your-secure-api-key-here` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:3000` | `http://localhost:3000,http://localhost:4200` |
| `RATE_LIMIT_REQUESTS_PER_MINUTE` | Requests per minute per key/IP | `60` | `100` |
| `PAGINATION_DEFAULT_PAGE_SIZE` | Default page size | `20` | `20` |
| `PAGINATION_MAX_PAGE_SIZE` | Maximum page size | `100` | `100` |

## Getting Started

### 🚀 Quick Start (One Command)

**The fastest way to start everything:**

```bash
./start-all.sh
```

This single command will:
- ✅ Start MongoDB (Docker)
- ✅ Start Mongo Express (Docker)
- ✅ Build and start Backend API
- ✅ Start Frontend UI

**To stop everything:**
```bash
./stop-all.sh
```

See [QUICKSTART.md](QUICKSTART.md) for details.

---

### Prerequisites

- Java 21 or higher
- Docker & Docker Compose
- Node.js 18+ and npm (for frontend)
- MongoDB (automatically started by Docker)

### Option 1: Complete Stack with Docker (Recommended)

This is the easiest way to get started. Docker Compose will start MongoDB, mongo-express (database UI), and the application.

1. **Build the application JAR**:
   ```bash
   ./gradlew clean bootJar
   ```

2. **Start all services**:
   ```bash
   docker-compose up --build
   ```

3. **Access the services**:
   - Application API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - Mongo Express: http://localhost:8081
   - Health Check: http://localhost:8080/health

4. **Stop the services**:
   ```bash
   docker-compose down
   ```

### Option 2: Running Locally (without Docker)

1. **Start MongoDB**:
   ```bash
   # Using Docker
   docker run -d -p 27017:27017 --name mongodb mongo:7
   
   # Or install MongoDB locally
   ```

2. **Set environment variables**:
   ```bash
   export SPRING_DATA_MONGODB_URI="mongodb://localhost:27017/rag-chat-storage"
   export SECURITY_API_KEY="my-secret-api-key"
   export CORS_ALLOWED_ORIGINS="http://localhost:3000"
   export RATE_LIMIT_REQUESTS_PER_MINUTE=60
   ```

3. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

4. **Access the application**:
   - Application: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - Health Check: http://localhost:8080/health

## API Documentation

### Authentication

All `/api/**` endpoints require an API key in the `X-API-KEY` header:

```bash
curl -H "X-API-KEY: your-api-key-here" http://localhost:8080/api/v1/sessions?userId=user-123
```

Health check and Swagger UI endpoints do **not** require authentication.

### Interactive Documentation

Visit **http://localhost:8080/swagger-ui/index.html** for interactive API documentation where you can:
- View all endpoints and their parameters
- Test endpoints directly from the browser
- See request/response schemas

### API Endpoints

#### Session Management

**Create Session**
```http
POST /api/v1/sessions
Content-Type: application/json
X-API-KEY: your-api-key

{
  "userId": "user-123",
  "title": "My Chat Session"
}
```

**Get Sessions for User**
```http
GET /api/v1/sessions?userId=user-123&favorite=true
X-API-KEY: your-api-key
```

**Rename Session**
```http
PATCH /api/v1/sessions/{sessionId}/rename
Content-Type: application/json
X-API-KEY: your-api-key

{
  "title": "New Title"
}
```

**Mark/Unmark as Favorite**
```http
PATCH /api/v1/sessions/{sessionId}/favorite
Content-Type: application/json
X-API-KEY: your-api-key

{
  "favorite": true
}
```

**Delete Session**
```http
DELETE /api/v1/sessions/{sessionId}
X-API-KEY: your-api-key
```

#### Message Management

**Add Message to Session**
```http
POST /api/v1/sessions/{sessionId}/messages
Content-Type: application/json
X-API-KEY: your-api-key

{
  "sender": "USER",
  "content": "What is the capital of France?",
  "context": [
    {
      "sourceId": "doc-123",
      "snippet": "Paris is the capital and most populous city of France...",
      "metadata": {
        "score": 0.95,
        "source": "wikipedia"
      }
    }
  ]
}
```

Valid sender types: `USER`, `ASSISTANT`, `SYSTEM`

**Get Messages from Session (Paginated)**
```http
GET /api/v1/sessions/{sessionId}/messages?page=0&size=20
X-API-KEY: your-api-key
```

Response includes pagination metadata:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "last": false
}
```

### Health Check

**Simple Health Check**
```http
GET /health
```

**Actuator Health Check**
```http
GET /actuator/health
```

## Rate Limiting

- Uses in-memory sliding window algorithm
- Tracks requests per API key (or IP if key is missing)
- Default: 60 requests per minute
- Returns `HTTP 429 Too Many Requests` when exceeded

## Error Handling

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2025-11-14T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: title must not be blank",
  "path": "/api/v1/sessions/123/rename"
}
```

Common status codes:
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (invalid/missing API key)
- `404`: Not Found (resource doesn't exist)
- `429`: Too Many Requests (rate limit exceeded)
- `500`: Internal Server Error

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Test Reports

After running tests, view the HTML report:
```bash
open build/reports/tests/test/index.html
```

### Manual Testing with curl

```bash
# Set your API key
API_KEY="your-api-key-here"

# Create a session
SESSION_ID=$(curl -s -X POST http://localhost:8080/api/v1/sessions \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"userId":"test-user","title":"Test Session"}' | jq -r '.id')

# Add a message
curl -X POST http://localhost:8080/api/v1/sessions/$SESSION_ID/messages \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "Hello, how are you?",
    "context": [
      {
        "sourceId": "doc-1",
        "snippet": "Greetings are important in conversation",
        "metadata": {"score": 0.85}
      }
    ]
  }'

# Get messages
curl -X GET "http://localhost:8080/api/v1/sessions/$SESSION_ID/messages?page=0&size=10" \
  -H "X-API-KEY: $API_KEY"

# Delete session
curl -X DELETE http://localhost:8080/api/v1/sessions/$SESSION_ID \
  -H "X-API-KEY: $API_KEY"
```

## Project Structure

```
src/main/java/com/example/ragchatstorage/
├── config/               # Configuration classes
│   ├── CorsConfig.java
│   ├── OpenApiConfig.java
│   └── ...
├── controller/           # REST controllers
│   ├── ChatSessionController.java
│   ├── ChatMessageController.java
│   └── HealthController.java
├── dto/                  # Data Transfer Objects
├── exception/            # Custom exceptions & global handler
├── filter/               # Request filters (auth, rate limiting, logging)
├── mapper/               # MapStruct mappers
├── model/                # Domain models
├── repository/           # MongoDB repositories
└── service/              # Business logic
```

## MongoDB Collections

### chat_sessions
```json
{
  "_id": "session-123",
  "userId": "user-123",
  "title": "My Chat",
  "favorite": false,
  "createdAt": "2025-11-14T10:00:00Z",
  "updatedAt": "2025-11-14T10:30:00Z"
}
```

### chat_messages
```json
{
  "_id": "message-456",
  "sessionId": "session-123",
  "sender": "USER",
  "content": "What is AI?",
  "context": [
    {
      "sourceId": "doc-789",
      "snippet": "Artificial Intelligence...",
      "metadata": {"score": 0.92}
    }
  ],
  "createdAt": "2025-11-14T10:15:00Z"
}
```

## Security Best Practices

1. **Change the default API key**: Never use `changeme` in production
2. **Use HTTPS**: Always use TLS/SSL in production
3. **Rotate API keys**: Implement key rotation policies
4. **MongoDB authentication**: Enable authentication in production MongoDB
5. **Network security**: Use firewalls and VPCs
6. **Rate limiting**: Adjust limits based on your usage patterns

## Troubleshooting

### Port already in use
```bash
# Check what's using port 8080
lsof -i :8080

# Stop the process or change the port
export SERVER_PORT=8081
./gradlew bootRun
```

### MongoDB connection issues
```bash
# Check if MongoDB is running
docker ps | grep mongo

# View MongoDB logs
docker logs rag-chat-mongo
```

### Application logs
```bash
# View application logs
docker logs rag-chat-storage-app

# Follow logs
docker logs -f rag-chat-storage-app
```

## Development

### Building

```bash
# Clean build
./gradlew clean build

# Build without tests
./gradlew clean build -x test

# Build JAR only
./gradlew bootJar
```

### Code Quality

```bash
# Run tests with coverage
./gradlew test jacocoTestReport

# Check dependencies
./gradlew dependencies
```

## Production Considerations

1. **Database**: Use MongoDB Atlas or a managed MongoDB cluster
2. **Scaling**: Deploy multiple instances behind a load balancer
3. **Rate Limiting**: Consider Redis-based distributed rate limiting
4. **Monitoring**: Integrate with Prometheus/Grafana
5. **Logging**: Use centralized logging (ELK stack, CloudWatch, etc.)
6. **Secrets**: Use secret management tools (Vault, AWS Secrets Manager)
7. **Backup**: Implement automated MongoDB backups

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is for interview/educational purposes.

## Support

For issues and questions:
- Check the Swagger documentation at `/swagger-ui/index.html`
- Review the logs for error details
- Ensure all environment variables are set correctly

## Message User Identification
Each user message now carries a `userId` that must match the owning session's `userId`. The backend enforces this to prevent cross-user injection into another user's session.
