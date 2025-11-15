# RAG Chat Storage API - cURL Testing Guide

This guide provides curl commands to test all API endpoints.

## Setup

First, set your API key as an environment variable:

```bash
export API_KEY="changeme"
export BASE_URL="http://localhost:8080"
```

---

## Health Check (No API Key Required)

### Simple Health Check
```bash
curl -X GET "$BASE_URL/health" | jq
```

### Actuator Health Check
```bash
curl -X GET "$BASE_URL/actuator/health" | jq
```

---

## Session Management

### 1. Create a New Session

```bash
curl -X POST "$BASE_URL/api/v1/sessions" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "title": "My First RAG Chat"
  }' | jq

# Save the session ID for next commands
export SESSION_ID="<paste-session-id-here>"
```

**Expected Response:**
```json
{
  "id": "673589a1b2c3d4e5f6789abc",
  "userId": "user-123",
  "title": "My First RAG Chat",
  "favorite": false,
  "createdAt": "2025-11-14T14:30:00Z",
  "updatedAt": "2025-11-14T14:30:00Z"
}
```

### 2. Get All Sessions for a User

```bash
curl -X GET "$BASE_URL/api/v1/sessions?userId=user-123" \
  -H "X-API-KEY: $API_KEY" | jq
```

### 3. Get Only Favorite Sessions

```bash
curl -X GET "$BASE_URL/api/v1/sessions?userId=user-123&favorite=true" \
  -H "X-API-KEY: $API_KEY" | jq
```

### 4. Rename a Session

```bash
curl -X PATCH "$BASE_URL/api/v1/sessions/$SESSION_ID/rename" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Session Title"
  }' | jq
```

### 5. Mark Session as Favorite

```bash
curl -X PATCH "$BASE_URL/api/v1/sessions/$SESSION_ID/favorite" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "favorite": true
  }' | jq
```

### 6. Unmark Session as Favorite

```bash
curl -X PATCH "$BASE_URL/api/v1/sessions/$SESSION_ID/favorite" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "favorite": false
  }' | jq
```

### 7. Delete a Session

```bash
curl -X DELETE "$BASE_URL/api/v1/sessions/$SESSION_ID" \
  -H "X-API-KEY: $API_KEY" \
  -w "\nHTTP Status: %{http_code}\n"
```

---

## Message Management

### 1. Add a User Message (Simple)

```bash
curl -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "What is the capital of France?"
  }' | jq
```

### 2. Add a User Message (with Context)

```bash
curl -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "Tell me about artificial intelligence",
    "context": []
  }' | jq
```

### 3. Add an Assistant Message with RAG Context

```bash
curl -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "ASSISTANT",
    "content": "The capital of France is Paris. It is located in the north-central part of the country and is known for its art, fashion, gastronomy, and culture.",
    "context": [
      {
        "sourceId": "wiki-france-001",
        "snippet": "Paris is the capital and most populous city of France, with an estimated population of 2,165,423 residents in 2019...",
        "metadata": {
          "score": 0.95,
          "source": "wikipedia",
          "lastUpdated": "2024-01-15",
          "url": "https://en.wikipedia.org/wiki/Paris"
        }
      },
      {
        "sourceId": "geography-db-456",
        "snippet": "Paris, city and capital of France, situated in the north-central part of the country. People were living on the site of the present-day city...",
        "metadata": {
          "score": 0.92,
          "source": "britannica",
          "category": "geography"
        }
      }
    ]
  }' | jq
```

### 4. Add a System Message

```bash
curl -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "SYSTEM",
    "content": "Session started. RAG mode enabled."
  }' | jq
```

### 5. Get Messages (First Page)

```bash
curl -X GET "$BASE_URL/api/v1/sessions/$SESSION_ID/messages?page=0&size=20" \
  -H "X-API-KEY: $API_KEY" | jq
```

### 6. Get Messages (Second Page)

```bash
curl -X GET "$BASE_URL/api/v1/sessions/$SESSION_ID/messages?page=1&size=20" \
  -H "X-API-KEY: $API_KEY" | jq
```

### 7. Get Messages (Custom Page Size)

```bash
curl -X GET "$BASE_URL/api/v1/sessions/$SESSION_ID/messages?page=0&size=10" \
  -H "X-API-KEY: $API_KEY" | jq
```

**Expected Response Format:**
```json
{
  "content": [
    {
      "id": "message-id-1",
      "sessionId": "session-id",
      "sender": "USER",
      "content": "What is the capital of France?",
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

## Error Testing

### 1. Test Missing API Key (401 Unauthorized)

```bash
curl -X GET "$BASE_URL/api/v1/sessions?userId=user-123" | jq
```

**Expected Response:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing API key"
}
```

### 2. Test Invalid API Key (401 Unauthorized)

```bash
curl -X GET "$BASE_URL/api/v1/sessions?userId=user-123" \
  -H "X-API-KEY: invalid-key" | jq
```

### 3. Test Session Not Found (404 Not Found)

```bash
curl -X GET "$BASE_URL/api/v1/sessions/non-existent-id/messages" \
  -H "X-API-KEY: $API_KEY" | jq
```

**Expected Response:**
```json
{
  "timestamp": "2025-11-14T14:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Session not found: non-existent-id",
  "path": "/api/v1/sessions/non-existent-id/messages"
}
```

### 4. Test Validation Error (400 Bad Request)

```bash
curl -X POST "$BASE_URL/api/v1/sessions" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "",
    "title": "Test"
  }' | jq
```

### 5. Test Rate Limiting (429 Too Many Requests)

```bash
# Send 65 requests rapidly (if rate limit is 60/min)
for i in {1..65}; do
  curl -X GET "$BASE_URL/api/v1/sessions?userId=user-123" \
    -H "X-API-KEY: $API_KEY" -s -w "\nRequest $i: %{http_code}\n"
  sleep 0.1
done
```

---

## Complete Workflow Example

### Complete Session Lifecycle

```bash
#!/bin/bash

API_KEY="changeme"
BASE_URL="http://localhost:8080"

echo "=== 1. Create Session ==="
SESSION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/sessions" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "title": "Testing Session"
  }')

echo "$SESSION_RESPONSE" | jq
SESSION_ID=$(echo "$SESSION_RESPONSE" | jq -r '.id')
echo "Session ID: $SESSION_ID"
echo ""

echo "=== 2. Add User Message ==="
curl -s -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "What is machine learning?"
  }' | jq
echo ""

echo "=== 3. Add Assistant Response with RAG Context ==="
curl -s -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "ASSISTANT",
    "content": "Machine learning is a subset of artificial intelligence that enables systems to learn and improve from experience without being explicitly programmed.",
    "context": [
      {
        "sourceId": "ml-intro-001",
        "snippet": "Machine learning is the study of computer algorithms that improve automatically through experience...",
        "metadata": {
          "score": 0.97,
          "source": "research_paper",
          "year": 2023
        }
      }
    ]
  }' | jq
echo ""

echo "=== 4. Get All Messages ==="
curl -s -X GET "$BASE_URL/api/v1/sessions/$SESSION_ID/messages?page=0&size=10" \
  -H "X-API-KEY: $API_KEY" | jq
echo ""

echo "=== 5. Mark as Favorite ==="
curl -s -X PATCH "$BASE_URL/api/v1/sessions/$SESSION_ID/favorite" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"favorite": true}' | jq
echo ""

echo "=== 6. Rename Session ==="
curl -s -X PATCH "$BASE_URL/api/v1/sessions/$SESSION_ID/rename" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"title": "ML Discussion"}' | jq
echo ""

echo "=== 7. Get User Sessions ==="
curl -s -X GET "$BASE_URL/api/v1/sessions?userId=user-123" \
  -H "X-API-KEY: $API_KEY" | jq
echo ""

echo "=== 8. Delete Session ==="
curl -s -X DELETE "$BASE_URL/api/v1/sessions/$SESSION_ID" \
  -H "X-API-KEY: $API_KEY" \
  -w "HTTP Status: %{http_code}\n"
echo ""

echo "=== Workflow Complete ==="
```

---

## Advanced Examples

### Multi-Turn RAG Conversation

```bash
# Turn 1: User asks
curl -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "What are neural networks?"
  }' | jq

# Turn 1: Assistant responds with context
curl -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "ASSISTANT",
    "content": "Neural networks are computing systems inspired by biological neural networks. They consist of interconnected nodes (neurons) that process information.",
    "context": [
      {
        "sourceId": "nn-basics-101",
        "snippet": "A neural network is a series of algorithms that endeavors to recognize underlying relationships in a set of data...",
        "metadata": {"score": 0.94, "source": "textbook", "chapter": 3}
      }
    ]
  }' | jq

# Turn 2: Follow-up question
curl -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "How do they learn?"
  }' | jq

# Turn 2: Response
curl -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/messages" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "ASSISTANT",
    "content": "Neural networks learn through a process called backpropagation, where the network adjusts its weights based on errors in predictions.",
    "context": [
      {
        "sourceId": "nn-training-202",
        "snippet": "Backpropagation is a method used in artificial neural networks to calculate the gradient...",
        "metadata": {"score": 0.91, "source": "course_material"}
      }
    ]
  }' | jq
```

---

## Testing Tips

### 1. Save Session ID Automatically

```bash
SESSION_ID=$(curl -s -X POST "$BASE_URL/api/v1/sessions" \
  -H "X-API-KEY: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-123","title":"Test"}' | jq -r '.id')

echo "Created session: $SESSION_ID"
```

### 2. Pretty Print with jq

```bash
# Install jq if not available: brew install jq (macOS) or apt-get install jq (Linux)
curl ... | jq '.'
```

### 3. Show HTTP Status Code

```bash
curl ... -w "\nHTTP Status: %{http_code}\n"
```

### 4. Show Response Headers

```bash
curl ... -i
```

### 5. Verbose Output (Debug)

```bash
curl ... -v
```

### 6. Save Response to File

```bash
curl ... -o response.json
```

---

## Quick Reference

| Endpoint | Method | Auth Required |
|----------|--------|---------------|
| `/health` | GET | No |
| `/actuator/health` | GET | No |
| `/api/v1/sessions` | POST | Yes |
| `/api/v1/sessions` | GET | Yes |
| `/api/v1/sessions/{id}/rename` | PATCH | Yes |
| `/api/v1/sessions/{id}/favorite` | PATCH | Yes |
| `/api/v1/sessions/{id}` | DELETE | Yes |
| `/api/v1/sessions/{id}/messages` | POST | Yes |
| `/api/v1/sessions/{id}/messages` | GET | Yes |

---

## Environment Setup for Different Environments

### Local Development
```bash
export API_KEY="changeme"
export BASE_URL="http://localhost:8080"
```

### Docker
```bash
export API_KEY="changeme-use-a-strong-key"
export BASE_URL="http://localhost:8080"
```

### Production
```bash
export API_KEY="your-production-api-key"
export BASE_URL="https://your-domain.com"
```

---

## Troubleshooting

### Connection Refused
```bash
# Check if service is running
curl -I http://localhost:8080/health

# If not, start the service
docker-compose up
```

### 401 Unauthorized
- Check if API_KEY environment variable is set
- Verify the API key matches the one in .env or docker-compose.yml

### 404 Not Found
- Verify the session ID exists
- Check the endpoint URL is correct

### 429 Too Many Requests
- Wait for the rate limit window to reset (1 minute)
- Reduce request frequency

---

**Note:** Replace `$SESSION_ID` with an actual session ID from a create session response, or save it as shown in the examples above.

