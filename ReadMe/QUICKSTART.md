# Quick Start Guide

## 🚀 Get Started in 3 Minutes

### Step 1: Start the Application with Docker

```bash
# Build the application
./gradlew clean bootJar

# Start all services (MongoDB, Mongo Express, Application)
docker-compose up --build
```

Wait for the services to start. You should see:
- ✅ MongoDB running on port 27017
- ✅ Mongo Express UI on http://localhost:8081
- ✅ Application API on http://localhost:8080

### Step 2: Access the Services

Open your browser and visit:

- **Swagger UI (API Documentation)**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/health
- **Mongo Express (Database UI)**: http://localhost:8081
  - Username: `admin`
  - Password: `admin`

### Step 3: Test the API

#### Option A: Use Swagger UI (Easiest)

1. Go to http://localhost:8080/swagger-ui/index.html
2. Click the 🔓 **Authorize** button at the top
3. Enter API Key: `changeme-use-a-strong-key`
4. Click **Authorize**
5. Try the endpoints directly from the browser!

#### Option B: Use the Test Script

```bash
# Edit the API key in the script
nano test-api.sh
# Change: API_KEY="your-api-key-here"
# To: API_KEY="changeme-use-a-strong-key"

# Run the test script
./test-api.sh
```

#### Option C: Use curl

```bash
# Set your API key
export API_KEY="changeme-use-a-strong-key"

# Create a session
curl -X POST http://localhost:8080/api/v1/sessions \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "title": "My Chat"
  }'

# This returns: {"id":"abc123",...}
# Use the id in the next requests

# Add a message
curl -X POST http://localhost:8080/api/v1/sessions/abc123/messages \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "Hello!",
    "context": [
      {
        "sourceId": "doc-1",
        "snippet": "Retrieved context...",
        "metadata": {"score": 0.95}
      }
    ]
  }'

# Get messages
curl -X GET "http://localhost:8080/api/v1/sessions/abc123/messages?page=0&size=20" \
  -H "X-API-KEY: $API_KEY"
```

## 🛠️ Configuration

### Using .env file (Recommended for Docker)

```bash
# Copy the example
cp .env.example .env

# Edit with your values
nano .env
```

Then start with:
```bash
docker-compose up
```

### Using Environment Variables

```bash
export SECURITY_API_KEY="your-strong-api-key-here"
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:4200"
export RATE_LIMIT_REQUESTS_PER_MINUTE=100

./gradlew bootRun
```

## 📊 View Your Data

1. Open Mongo Express: http://localhost:8081
2. Login with `admin` / `admin`
3. Click on `rag-chat-storage` database
4. View collections:
   - `chat_sessions` - All your chat sessions
   - `chat_messages` - All messages with RAG context

## 🧪 Run Tests

```bash
# Run all tests
./gradlew test

# View test report
open build/reports/tests/test/index.html
```

## 🛑 Stop the Application

```bash
# Stop all containers
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v
```

## 🔧 Common Issues

### Port 8080 already in use
```bash
# Option 1: Stop the conflicting service
lsof -i :8080
kill -9 <PID>

# Option 2: Change the port
export SERVER_PORT=8081
./gradlew bootRun
```

### MongoDB connection error
```bash
# Check if MongoDB is running
docker ps | grep mongo

# Restart MongoDB
docker-compose restart mongo
```

### Build fails
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies
```

## 📚 Next Steps

- Read the full [README.md](../README.md) for detailed documentation
- Explore the Swagger UI for complete API reference
- Check out the service tests in `src/test/java`
- Review the configuration in `application.yml`

## 🎯 Example Use Case: RAG Chatbot Integration

This microservice is designed to be integrated with a RAG (Retrieval-Augmented Generation) chatbot:

1. **User asks a question** → Your frontend sends it to your RAG chatbot
2. **RAG chatbot retrieves context** → From your vector database/knowledge base
3. **RAG chatbot generates answer** → Using retrieved context
4. **Store the conversation** → Call this API to save:
   - User message (with optional context)
   - Assistant response (with RAG context showing which documents were used)

This gives you:
- ✅ Complete conversation history
- ✅ Audit trail of which sources were used
- ✅ Ability to review and improve RAG performance
- ✅ Session management (favorites, rename, delete)

## 🔐 Security Notes

⚠️ **Before deploying to production:**

1. Change the API key to a strong, unique value
2. Enable MongoDB authentication
3. Use HTTPS/TLS
4. Set appropriate CORS origins
5. Adjust rate limits based on your needs
6. Enable MongoDB persistence and backups
7. Review and update security configurations

## 🎉 You're Ready!

Your RAG Chat Storage microservice is now running and ready to store chat histories!

