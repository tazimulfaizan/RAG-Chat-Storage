# 🚀 RAG Chat Storage with AI Frontend - Quick Start Guide

## ✅ **Complete Setup in 5 Minutes**

Your RAG Chat Storage microservice now has a **complete React frontend with AI chat support**!

---

## 📦 **What's Included**

✅ **Backend Microservice** - Spring Boot REST API  
✅ **Frontend Application** - React with Vite  
✅ **AI Integration** - OpenAI GPT-4 support  
✅ **MongoDB** - Data storage  
✅ **Mongo Express** - Database management UI  
✅ **Nginx** - Rate limiting (optional)  
✅ **Docker** - Complete containerization  

---

## 🎯 **Quick Start**

### **Step 1: Get OpenAI API Key**

1. Go to https://platform.openai.com/api-keys
2. Create a new API key
3. Copy the key (starts with `sk-...`)

### **Step 2: Configure Environment**

```bash
cd /Users/tazimul.faizan/Downloads/rag-chat-storage

# Copy frontend environment file
cp frontend/.env.example frontend/.env

# Edit and add your OpenAI API key
nano frontend/.env
```

**Update this line:**
```env
VITE_OPENAI_API_KEY=sk-your-actual-key-here
```

### **Step 3: Install Frontend Dependencies**

```bash
cd frontend
npm install
cd ..
```

### **Step 4: Start Everything with Docker**

```bash
# From project root
docker-compose up --build
```

### **Step 5: Access the Application**

- **Frontend (AI Chat UI):** http://localhost:3000
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Mongo Express:** http://localhost:8081 (admin/admin)

---

## 🎨 **Using the Frontend**

### **1. Create a New Chat Session**
Click **"+ New Chat"** button in the sidebar

### **2. Start Chatting with AI**
- Type your message in the input box
- Click **Send** or press **Enter**
- AI will respond with GPT-4
- **RAG context** is automatically simulated and stored

### **3. View RAG Context**
- AI responses show **"Show RAG Context"** link
- Click to see retrieved sources and metadata
- All context is stored in your backend

### **4. Manage Sessions**
- ⭐ **Favorite** - Click star icon
- ✏️ **Rename** - Click edit icon
- 🗑️ **Delete** - Click trash icon

---

## 📊 **Complete Architecture**

```
┌─────────────────────────────────────────────────────────┐
│                    USER BROWSER                         │
│              http://localhost:3000                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              REACT FRONTEND (Port 3000)                 │
│  - Chat UI                                              │
│  - Session Management                                   │
│  - AI Integration (OpenAI)                              │
│  - RAG Context Display                                  │
└────────────────────┬────────────────────────────────────┘
                     │ REST API Calls
                     ▼
┌─────────────────────────────────────────────────────────┐
│          SPRING BOOT BACKEND (Port 8080)                │
│  - Session Management                                   │
│  - Message Storage                                      │
│  - API Key Authentication                               │
│  - Rate Limiting (via Nginx)                            │
└────────────────────┬────────────────────────────────────┘
                     │ MongoDB Driver
                     ▼
┌─────────────────────────────────────────────────────────┐
│             MONGODB (Port 27017)                        │
│  - chat_sessions collection                             │
│  - chat_messages collection                             │
│  - RAG context embedded in messages                     │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 **Complete Workflow**

### **User Asks Question:**
```
1. User types: "What's our vacation policy?"
   ↓
2. Frontend saves USER message to backend
   POST /api/v1/sessions/{id}/messages
   { "sender": "USER", "content": "What's our vacation policy?" }
   ↓
3. Frontend calls OpenAI GPT-4
   ↓
4. AI responds: "Employees get 20 days..."
   ↓
5. Frontend simulates RAG context retrieval
   context: [{ sourceId: "hr-policy", snippet: "...", metadata: {...} }]
   ↓
6. Frontend saves ASSISTANT message with context
   POST /api/v1/sessions/{id}/messages
   {
     "sender": "ASSISTANT",
     "content": "Employees get 20 days...",
     "context": [...]
   }
   ↓
7. Both messages stored in MongoDB
   ↓
8. UI displays conversation with expandable RAG context
```

---

## 🧪 **Testing the Complete System**

### **Test 1: Create Session**
```bash
curl -X POST http://localhost:8080/api/v1/sessions \
  -H "X-API-KEY: changeme" \
  -H "Content-Type: application/json" \
  -d '{"userId":"test-user","title":"Test Chat"}'
```

### **Test 2: Add Message via API**
```bash
curl -X POST http://localhost:8080/api/v1/sessions/SESSION_ID/messages \
  -H "X-API-KEY: changeme" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "Hello AI!"
  }'
```

### **Test 3: Use Frontend**
1. Open http://localhost:3000
2. Click "+ New Chat"
3. Type "What is artificial intelligence?"
4. See AI response with RAG context
5. Click "Show RAG Context" to see sources

---

## 📁 **Project Structure**

```
rag-chat-storage/
├── backend (Spring Boot)
│   ├── src/main/java/.../
│   │   ├── controller/     # REST endpoints
│   │   ├── service/        # Business logic
│   │   ├── model/          # Entities
│   │   ├── repository/     # MongoDB repos
│   │   └── config/         # Security, CORS
│   ├── Dockerfile
│   └── build.gradle
│
├── frontend (React + Vite)
│   ├── src/
│   │   ├── components/
│   │   │   ├── ChatInterface.jsx
│   │   │   ├── SessionList.jsx
│   │   │   └── MessageBubble.jsx
│   │   ├── services/
│   │   │   ├── apiService.js      # Backend API calls
│   │   │   └── aiService.js       # OpenAI integration
│   │   └── App.jsx
│   ├── Dockerfile
│   ├── package.json
│   └── .env
│
├── docker-compose.yml       # Complete stack
├── .env                     # Backend config
└── README.md               # This file
```

---

## ⚙️ **Configuration**

### **Backend (.env in root)**
```env
SECURITY_API_KEY=changeme
MONGODB_URI=mongodb://mongo:27017/rag-chat-storage
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### **Frontend (frontend/.env)**
```env
VITE_API_URL=http://localhost:8080
VITE_API_KEY=changeme
VITE_OPENAI_API_KEY=sk-your-key-here
VITE_AI_PROVIDER=openai
VITE_DEFAULT_USER_ID=demo-user
```

---

## 🚀 **Development Mode**

### **Run Backend Only:**
```bash
./gradlew bootRun
```

### **Run Frontend Only:**
```bash
cd frontend
npm run dev
```

### **Run Everything:**
```bash
docker-compose up
```

---

## 🎨 **Features**

### **Frontend Features:**
✅ Real-time AI chat with GPT-4  
✅ Session management (create, rename, delete, favorite)  
✅ RAG context display (expandable)  
✅ Message history with timestamps  
✅ Loading states and error handling  
✅ Responsive design  
✅ Clean, modern UI  

### **Backend Features:**
✅ RESTful API  
✅ MongoDB storage  
✅ API key authentication  
✅ Rate limiting (Nginx)  
✅ CORS configuration  
✅ Pagination support  
✅ Health checks  
✅ Swagger documentation  
✅ Caching (Caffeine)  
✅ Global error handling  

---

## 🔧 **Troubleshooting**

### **Frontend not connecting to backend:**
Check CORS settings in backend `.env`:
```env
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### **AI not responding:**
1. Check OpenAI API key in `frontend/.env`
2. Verify API key is valid: https://platform.openai.com/account/api-keys
3. Check browser console for errors

### **Port already in use:**
```bash
# Kill process on port 3000
lsof -ti:3000 | xargs kill -9

# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

---

## 📚 **API Documentation**

**Swagger UI:** http://localhost:8080/swagger-ui/index.html

**Key Endpoints:**
- `POST /api/v1/sessions` - Create session
- `GET /api/v1/sessions` - List sessions
- `POST /api/v1/sessions/{id}/messages` - Add message
- `GET /api/v1/sessions/{id}/messages` - Get messages
- `PATCH /api/v1/sessions/{id}/rename` - Rename session
- `DELETE /api/v1/sessions/{id}` - Delete session

---

## 🎉 **You're All Set!**

Your complete RAG Chat Storage system with AI support is now running!

**Access Points:**
- 🎨 **Frontend:** http://localhost:3000
- 🔌 **Backend:** http://localhost:8080
- 📊 **Database UI:** http://localhost:8081
- 📖 **API Docs:** http://localhost:8080/swagger-ui/index.html

**Start chatting with AI and watch your conversations being stored with RAG context!** 🚀

