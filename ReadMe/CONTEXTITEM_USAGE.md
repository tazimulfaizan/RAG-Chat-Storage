# 📋 ContextItem - Usage Documentation

## 🎯 **What is ContextItem?**

`ContextItem` represents **RAG (Retrieval-Augmented Generation) context** attached to chat messages. When a chatbot uses RAG to generate responses, it retrieves relevant information from external sources. `ContextItem` stores this retrieved context along with the AI's response.

---

## 🏗️ **Purpose & Use Case**

### **RAG Chat Flow:**
```
1. User asks: "What's the weather in Tokyo?"
   ↓
2. RAG System retrieves relevant data:
   - Source: Weather API
   - Snippet: "Tokyo: 22°C, Sunny"
   - Metadata: {timestamp, source_url, confidence}
   ↓
3. AI generates response: "The weather in Tokyo is 22°C and sunny."
   ↓
4. Your API stores BOTH:
   - Message content (AI's response)
   - Context (Retrieved data used to generate response)
```

**ContextItem stores the retrieved data (#2) that was used to generate the AI response.**

---

## 📊 **Data Structure**

### **ContextItem.java (Domain Model)**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextItem {
    private String sourceId;      // ID of the source document
    private String snippet;        // Excerpt from the source
    private Map<String, Object> metadata;  // Additional info (confidence, url, etc.)
}
```

### **Fields Explained:**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `sourceId` | String | Unique identifier of source document | `"doc-12345"`, `"weather-api"` |
| `snippet` | String | Relevant text excerpt from source | `"Tokyo: 22°C, Sunny"` |
| `metadata` | Map<String, Object> | Additional context information | `{confidence: 0.95, url: "https://..."}` |

---

## 🔄 **Complete Data Flow**

### **1. Request (Client → API)**

**Endpoint:** `POST /api/v1/sessions/{sessionId}/messages`

**Request Body:**
```json
{
  "sender": "ASSISTANT",
  "content": "The weather in Tokyo is 22°C and sunny.",
  "context": [
    {
      "sourceId": "weather-api-tokyo",
      "snippet": "Tokyo: Temperature 22°C, Conditions: Sunny, Humidity: 45%",
      "metadata": {
        "source": "OpenWeatherMap API",
        "timestamp": "2025-11-15T10:00:00Z",
        "confidence": 0.98,
        "url": "https://api.openweathermap.org/..."
      }
    },
    {
      "sourceId": "doc-japan-climate",
      "snippet": "Tokyo typically has mild weather in November...",
      "metadata": {
        "source": "Climate Database",
        "document_type": "reference",
        "relevance_score": 0.85
      }
    }
  ]
}
```

### **2. DTO (Data Transfer Object)**

**ContextItemDto.java:**
```java
public record ContextItemDto(
    String sourceId,
    String snippet,
    Map<String, Object> metadata
) {}
```

**CreateMessageRequest.java:**
```java
public record CreateMessageRequest(
    @NotNull SenderType sender,
    @NotBlank String content,
    List<ContextItemDto> context  // ← ContextItemDto used here
) {}
```

### **3. Service Layer Conversion**

**ChatMessageService.java:**
```java
public ChatMessage addMessage(String sessionId, CreateMessageRequest request) {
    // Convert ContextItemDto to ContextItem entity
    List<ContextItem> contextItems = null;
    if (request.context() != null) {
        contextItems = request.context().stream()
            .map(this::toEntity)  // DTO → Entity
            .toList();
    }
    
    ChatMessage message = ChatMessage.builder()
        .sessionId(sessionId)
        .sender(request.sender())
        .content(request.content())
        .context(contextItems)  // ← Stored in ChatMessage
        .createdAt(Instant.now())
        .build();
    
    return messageRepository.save(message);
}

private ContextItem toEntity(ContextItemDto dto) {
    return ContextItem.builder()
        .sourceId(dto.sourceId())
        .snippet(dto.snippet())
        .metadata(dto.metadata() == null ? Collections.emptyMap() : dto.metadata())
        .build();
}
```

### **4. Domain Model Storage**

**ChatMessage.java:**
```java
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;
    private String sessionId;
    private SenderType sender;
    private String content;
    private List<ContextItem> context;  // ← Stored here
    private Instant createdAt;
}
```

**MongoDB Document:**
```json
{
  "_id": "msg-67890",
  "sessionId": "session-12345",
  "sender": "ASSISTANT",
  "content": "The weather in Tokyo is 22°C and sunny.",
  "context": [
    {
      "sourceId": "weather-api-tokyo",
      "snippet": "Tokyo: Temperature 22°C, Conditions: Sunny",
      "metadata": {
        "source": "OpenWeatherMap API",
        "confidence": 0.98
      }
    }
  ],
  "createdAt": "2025-11-15T10:30:00Z"
}
```

### **5. Response (API → Client)**

**MessageResponse.java:**
```java
public record MessageResponse(
    String id,
    String sessionId,
    String sender,
    String content,
    List<Map<String, Object>> context,  // ← Converted to Map
    Instant createdAt
) {
    public static MessageResponse from(ChatMessage message) {
        List<Map<String, Object>> ctx = null;
        if (message.getContext() != null) {
            ctx = message.getContext().stream()
                .map(MessageResponse::toMap)  // Entity → Map
                .toList();
        }
        
        return new MessageResponse(/*...*/);
    }
    
    private static Map<String, Object> toMap(ContextItem item) {
        return Map.of(
            "sourceId", item.getSourceId(),
            "snippet", item.getSnippet(),
            "metadata", item.getMetadata()
        );
    }
}
```

**Response JSON:**
```json
{
  "id": "msg-67890",
  "sessionId": "session-12345",
  "sender": "ASSISTANT",
  "content": "The weather in Tokyo is 22°C and sunny.",
  "context": [
    {
      "sourceId": "weather-api-tokyo",
      "snippet": "Tokyo: Temperature 22°C, Conditions: Sunny",
      "metadata": {
        "source": "OpenWeatherMap API",
        "confidence": 0.98
      }
    }
  ],
  "createdAt": "2025-11-15T10:30:00Z"
}
```

---

## 🎛️ **Controller Usage**

### **ChatMessageController.java**

```java
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
public class ChatMessageController {
    
    private final ChatMessageService messageService;
    
    // CREATE MESSAGE WITH CONTEXT
    @PostMapping
    public ResponseEntity<MessageResponse> addMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody CreateMessageRequest request) {
        // request.context() contains List<ContextItemDto>
        ChatMessage created = messageService.addMessage(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(MessageResponse.from(created));
    }
    
    // GET MESSAGES (includes context in response)
    @GetMapping
    public ResponseEntity<PagedResponse<MessageResponse>> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        
        Page<ChatMessage> result = messageService.getMessages(sessionId, page, effectiveSize);
        
        var content = result.getContent().stream()
            .map(MessageResponse::from)  // Converts ContextItem to Map
            .toList();
        
        return ResponseEntity.ok(new PagedResponse<>(/*...*/));
    }
}
```

---

## 📍 **Where is ContextItem Used?**

### **1. Domain Model:**
- **ChatMessage.java** - `private List<ContextItem> context;`

### **2. DTO (Data Transfer Objects):**
- **ContextItemDto.java** - Record version for API
- **CreateMessageRequest.java** - Contains `List<ContextItemDto> context`
- **MessageResponse.java** - Converts ContextItem to Map for JSON response

### **3. Service Layer:**
- **ChatMessageService.java** - Converts DTO → Entity, saves to DB

### **4. Mapper (Optional - currently generated by MapStruct):**
- **ChatMessageMapper.java** - Maps between DTO and Entity

### **5. Controller:**
- **ChatMessageController.java** - Handles HTTP requests with ContextItem

---

## 🌐 **API Endpoints Using ContextItem**

### **POST /api/v1/sessions/{sessionId}/messages**

**Create a message with RAG context:**

```bash
curl -X POST http://localhost:8080/api/v1/sessions/session-123/messages \
  -H "X-API-KEY: changeme" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "ASSISTANT",
    "content": "Based on the documents, the answer is...",
    "context": [
      {
        "sourceId": "doc-456",
        "snippet": "Relevant excerpt from document...",
        "metadata": {
          "confidence": 0.92,
          "source": "Knowledge Base"
        }
      }
    ]
  }'
```

### **GET /api/v1/sessions/{sessionId}/messages**

**Retrieve messages (includes context):**

```bash
curl -X GET "http://localhost:8080/api/v1/sessions/session-123/messages?page=0&size=20" \
  -H "X-API-KEY: changeme"
```

**Response:**
```json
{
  "content": [
    {
      "id": "msg-789",
      "sessionId": "session-123",
      "sender": "ASSISTANT",
      "content": "Based on the documents, the answer is...",
      "context": [
        {
          "sourceId": "doc-456",
          "snippet": "Relevant excerpt from document...",
          "metadata": {
            "confidence": 0.92,
            "source": "Knowledge Base"
          }
        }
      ],
      "createdAt": "2025-11-15T10:30:00Z"
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

## 💡 **Real-World Example**

### **Scenario: RAG Chatbot answering about company policies**

**User:** "What's our vacation policy?"

**RAG System:**
1. Searches company documents
2. Finds relevant policy document
3. Extracts snippet
4. AI generates human-friendly response

**Stored Message:**
```json
{
  "sender": "ASSISTANT",
  "content": "Employees get 20 days of vacation per year, plus public holidays.",
  "context": [
    {
      "sourceId": "hr-policy-2025-v3",
      "snippet": "Annual Leave: Full-time employees are entitled to 20 working days...",
      "metadata": {
        "document_name": "Employee Handbook 2025",
        "page": 12,
        "section": "Leave Policy",
        "confidence": 0.96,
        "last_updated": "2025-01-01"
      }
    }
  ]
}
```

**Benefits:**
- ✅ **Transparency** - User can see sources
- ✅ **Verification** - Can trace back to original documents
- ✅ **Audit Trail** - Know what data was used
- ✅ **Debugging** - If AI is wrong, check the context

---

## 📊 **Summary**

### **ContextItem Purpose:**
Stores **RAG retrieved context** (source documents, snippets, metadata) that was used to generate AI responses.

### **Used In:**
1. **Controller:** `ChatMessageController` - POST and GET endpoints
2. **Service:** `ChatMessageService` - DTO → Entity conversion
3. **Model:** `ChatMessage` - Stored in MongoDB
4. **DTO:** `ContextItemDto`, `CreateMessageRequest`, `MessageResponse`

### **Data Flow:**
```
Client Request (ContextItemDto)
    ↓
Controller (ChatMessageController)
    ↓
Service (ChatMessageService)
    ↓
Domain Model (ChatMessage with List<ContextItem>)
    ↓
MongoDB Storage
    ↓
Response (MessageResponse with context as Map)
    ↓
Client
```

### **Key Files:**
- **Model:** `ContextItem.java`, `ChatMessage.java`
- **DTO:** `ContextItemDto.java`, `CreateMessageRequest.java`, `MessageResponse.java`
- **Service:** `ChatMessageService.java`
- **Controller:** `ChatMessageController.java`

**ContextItem is essential for RAG systems to maintain traceability and transparency of AI-generated responses!** 🎯

