# RAG Chat Storage API - Insomnia Collection

## 📦 Import Instructions

### Method 1: Import from File (Recommended)

1. Open Insomnia
2. Click on the dropdown next to your workspace name
3. Select **Import/Export** → **Import Data** → **From File**
4. Select the `Insomnia_RAG_Chat_Storage.json` file
5. All requests will be imported with the collection

### Method 2: Create Manually

If import doesn't work, here are the cURL commands formatted for Insomnia:

---

## 🔧 Setup Environment Variables

In Insomnia, set these environment variables:

```json
{
  "baseUrl": "http://localhost:8080",
  "apiKey": "changeme",
  "sessionId": "paste-session-id-here"
}
```

**How to set:**
1. Click on **No Environment** dropdown (top left)
2. Click **Manage Environments**
3. Add the variables above
4. Click **Done**

---

## 📋 cURL Commands for Insomnia

### Health Checks

#### 1. Health Check
```bash
curl --request GET \
  --url http://localhost:8080/health
```

#### 2. Actuator Health
```bash
curl --request GET \
  --url http://localhost:8080/actuator/health
```

---

### Session Management

#### 3. Create Session
```bash
curl --request POST \
  --url http://localhost:8080/api/v1/sessions \
  --header 'Content-Type: application/json' \
  --header 'X-API-KEY: changeme' \
  --data '{
  "userId": "user-123",
  "title": "My RAG Chat Session"
}'
```

#### 4. Get Sessions for User
```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/sessions?userId=user-123' \
  --header 'X-API-KEY: changeme'
```

#### 5. Get Favorite Sessions Only
```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/sessions?userId=user-123&favorite=true' \
  --header 'X-API-KEY: changeme'
```

#### 6. Rename Session
```bash
curl --request PATCH \
  --url http://localhost:8080/api/v1/sessions/SESSION_ID/rename \
  --header 'Content-Type: application/json' \
  --header 'X-API-KEY: changeme' \
  --data '{
  "title": "Updated Session Title"
}'
```

#### 7. Mark as Favorite
```bash
curl --request PATCH \
  --url http://localhost:8080/api/v1/sessions/SESSION_ID/favorite \
  --header 'Content-Type: application/json' \
  --header 'X-API-KEY: changeme' \
  --data '{
  "favorite": true
}'
```

#### 8. Unmark as Favorite
```bash
curl --request PATCH \
  --url http://localhost:8080/api/v1/sessions/SESSION_ID/favorite \
  --header 'Content-Type: application/json' \
  --header 'X-API-KEY: changeme' \
  --data '{
  "favorite": false
}'
```

#### 9. Delete Session
```bash
curl --request DELETE \
  --url http://localhost:8080/api/v1/sessions/SESSION_ID \
  --header 'X-API-KEY: changeme'
```

---

### Message Management

#### 10. Add User Message
```bash
curl --request POST \
  --url http://localhost:8080/api/v1/sessions/SESSION_ID/messages \
  --header 'Content-Type: application/json' \
  --header 'X-API-KEY: changeme' \
  --data '{
  "sender": "USER",
  "content": "What is artificial intelligence?"
}'
```

#### 11. Add Assistant Message with RAG Context
```bash
curl --request POST \
  --url http://localhost:8080/api/v1/sessions/SESSION_ID/messages \
  --header 'Content-Type: application/json' \
  --header 'X-API-KEY: changeme' \
  --data '{
  "sender": "ASSISTANT",
  "content": "Artificial Intelligence (AI) is the simulation of human intelligence in machines that are programmed to think and learn. It encompasses machine learning, neural networks, and deep learning.",
  "context": [
    {
      "sourceId": "wiki-ai-001",
      "snippet": "Artificial intelligence is intelligence demonstrated by machines, as opposed to natural intelligence displayed by humans and animals.",
      "metadata": {
        "score": 0.95,
        "source": "wikipedia",
        "lastUpdated": "2024-01-15"
      }
    },
    {
      "sourceId": "paper-ml-123",
      "snippet": "Machine learning is a subset of AI that provides systems the ability to automatically learn and improve from experience.",
      "metadata": {
        "score": 0.89,
        "source": "research_paper",
        "year": 2023
      }
    }
  ]
}'
```

#### 12. Add System Message
```bash
curl --request POST \
  --url http://localhost:8080/api/v1/sessions/SESSION_ID/messages \
  --header 'Content-Type: application/json' \
  --header 'X-API-KEY: changeme' \
  --data '{
  "sender": "SYSTEM",
  "content": "Session started. RAG mode enabled."
}'
```

#### 13. Get Messages (Paginated)
```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/sessions/SESSION_ID/messages?page=0&size=20' \
  --header 'X-API-KEY: changeme'
```

---

## 🎯 Quick Start Guide for Insomnia

### Step 1: Import Collection
- Open Insomnia
- Import `Insomnia_RAG_Chat_Storage.json`

### Step 2: Configure Environment
- Set `baseUrl`: `http://localhost:8080`
- Set `apiKey`: `changeme`
- Set `sessionId`: Leave empty for now

### Step 3: Start Your Service
```bash
docker-compose up
```

### Step 4: Create a Session
1. Run **"Create Session"** request
2. Copy the `id` from the response
3. Update environment variable `sessionId` with the copied ID

### Step 5: Test Other Endpoints
- All requests are now ready to use!
- The `sessionId` variable will be automatically used in URLs

---

## 🔑 Using Environment Variables in Insomnia

Replace hardcoded values with variables:

- `{{ _.baseUrl }}` → Base URL
- `{{ _.apiKey }}` → API Key
- `{{ _.sessionId }}` → Session ID

Example:
```
{{ _.baseUrl }}/api/v1/sessions/{{ _.sessionId }}/messages
```

---

## 📊 Response Examples

### Create Session Response
```json
{
  "id": "673589a1b2c3d4e5f6789abc",
  "userId": "user-123",
  "title": "My RAG Chat Session",
  "favorite": false,
  "createdAt": "2025-11-14T14:30:00Z",
  "updatedAt": "2025-11-14T14:30:00Z"
}
```

### Get Messages Response
```json
{
  "content": [
    {
      "id": "message-id-1",
      "sessionId": "session-id",
      "sender": "USER",
      "content": "What is artificial intelligence?",
      "context": null,
      "createdAt": "2025-11-14T14:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

## 🚨 Error Responses

### 401 Unauthorized
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing API key"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-14T14:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Session not found: session-id",
  "path": "/api/v1/sessions/session-id/messages"
}
```

---

## 💡 Tips for Insomnia

1. **Use Chain Requests**: Extract `id` from create session and use it in subsequent requests
2. **Environment Switching**: Create different environments (dev, staging, prod)
3. **Code Generation**: Use Insomnia's "Generate Code" feature for other languages
4. **Tests**: Add tests to validate responses
5. **Documentation**: Use descriptions to document each request

---

## 🔗 Alternative Tools

If you prefer other tools, these cURL commands work with:
- **Postman** (import as cURL)
- **HTTPie** (convert to HTTPie syntax)
- **VS Code REST Client** (save as .http files)
- **Terminal** (use directly)

---

**Happy Testing! 🚀**

For more details, see: `CURL_TESTING_GUIDE.md`

