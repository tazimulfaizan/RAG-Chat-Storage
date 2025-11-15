# 🚀 Complete Frontend Implementation Guide

## 📦 **Quick Start**

```bash
cd /Users/tazimul.faizan/Downloads/rag-chat-storage/frontend

# Install dependencies
npm install

# Copy environment file
cp .env.example .env

# Edit .env and add your OpenAI API key
nano .env

# Start development server
npm run dev

# Open http://localhost:3000
```

---

## 🏗️ **Project Structure Created**

```
frontend/
├── package.json              ✅ Created
├── vite.config.js            ✅ Created
├── .env.example              ✅ Created
├── index.html                ✅ Created
├── src/
│   ├── index.jsx             → Need to create
│   ├── App.jsx               → Need to create
│   ├── components/
│   │   ├── ChatInterface.jsx → Need to create
│   │   ├── SessionList.jsx   → Need to create
│   │   └── MessageBubble.jsx → Need to create
│   └── services/
│       ├── apiService.js     → Need to create
│       └── aiService.js      → Need to create
```

---

## 📝 **Complete Implementation Files**

I'll provide all the code you need to create. Copy each file below:

### **1. src/index.jsx**

```jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

---

### **2. src/index.css**

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#root {
  width: 100%;
  height: 100vh;
}
```

---

### **3. src/App.jsx**

```jsx
import React, { useState, useEffect } from 'react';
import SessionList from './components/SessionList';
import ChatInterface from './components/ChatInterface';
import { apiService } from './services/apiService';

function App() {
  const [sessions, setSessions] = useState([]);
  const [currentSession, setCurrentSession] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const userId = import.meta.env.VITE_DEFAULT_USER_ID || 'demo-user';

  // Load sessions on mount
  useEffect(() => {
    loadSessions();
  }, []);

  const loadSessions = async () => {
    try {
      setLoading(true);
      const data = await apiService.getSessions(userId);
      setSessions(data);
      setError(null);
    } catch (err) {
      setError('Failed to load sessions: ' + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const createNewSession = async () => {
    try {
      const newSession = await apiService.createSession(userId, 'New Chat');
      setSessions([newSession, ...sessions]);
      setCurrentSession(newSession);
      setError(null);
    } catch (err) {
      setError('Failed to create session: ' + err.message);
      console.error(err);
    }
  };

  const selectSession = (session) => {
    setCurrentSession(session);
  };

  const deleteSession = async (sessionId) => {
    try {
      await apiService.deleteSession(sessionId);
      setSessions(sessions.filter(s => s.id !== sessionId));
      if (currentSession?.id === sessionId) {
        setCurrentSession(null);
      }
      setError(null);
    } catch (err) {
      setError('Failed to delete session: ' + err.message);
      console.error(err);
    }
  };

  const renameSession = async (sessionId, newTitle) => {
    try {
      const updated = await apiService.renameSession(sessionId, newTitle);
      setSessions(sessions.map(s => s.id === sessionId ? updated : s));
      if (currentSession?.id === sessionId) {
        setCurrentSession(updated);
      }
      setError(null);
    } catch (err) {
      setError('Failed to rename session: ' + err.message);
      console.error(err);
    }
  };

  const toggleFavorite = async (sessionId, favorite) => {
    try {
      const updated = await apiService.toggleFavorite(sessionId, favorite);
      setSessions(sessions.map(s => s.id === sessionId ? updated : s));
      if (currentSession?.id === sessionId) {
        setCurrentSession(updated);
      }
      setError(null);
    } catch (err) {
      setError('Failed to toggle favorite: ' + err.message);
      console.error(err);
    }
  };

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <div className="w-80 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-4 border-b border-gray-200">
          <h1 className="text-xl font-bold text-gray-800">RAG Chat Storage</h1>
          <p className="text-sm text-gray-500">with AI Support</p>
        </div>
        
        <div className="p-4">
          <button
            onClick={createNewSession}
            className="w-full bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors font-medium"
            disabled={loading}
          >
            + New Chat
          </button>
        </div>

        <SessionList
          sessions={sessions}
          currentSession={currentSession}
          onSelectSession={selectSession}
          onDeleteSession={deleteSession}
          onRenameSession={renameSession}
          onToggleFavorite={toggleFavorite}
        />
      </div>

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col">
        {error && (
          <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4">
            <p className="font-bold">Error</p>
            <p>{error}</p>
          </div>
        )}

        {currentSession ? (
          <ChatInterface session={currentSession} userId={userId} />
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-500">
            <div className="text-center">
              <h2 className="text-2xl font-bold mb-2">Welcome to RAG Chat</h2>
              <p>Select a session or create a new one to start chatting</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
```

---

### **4. src/services/apiService.js**

```javascript
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const API_KEY = import.meta.env.VITE_API_KEY || 'changeme';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
    'X-API-KEY': API_KEY,
  },
});

export const apiService = {
  // Sessions
  async getSessions(userId, favorite = null) {
    const params = { userId };
    if (favorite !== null) params.favorite = favorite;
    const response = await api.get('/api/v1/sessions', { params });
    return response.data;
  },

  async createSession(userId, title) {
    const response = await api.post('/api/v1/sessions', { userId, title });
    return response.data;
  },

  async deleteSession(sessionId) {
    await api.delete(`/api/v1/sessions/${sessionId}`);
  },

  async renameSession(sessionId, title) {
    const response = await api.patch(`/api/v1/sessions/${sessionId}/rename`, { title });
    return response.data;
  },

  async toggleFavorite(sessionId, favorite) {
    const response = await api.patch(`/api/v1/sessions/${sessionId}/favorite`, { favorite });
    return response.data;
  },

  // Messages
  async getMessages(sessionId, page = 0, size = 50) {
    const response = await api.get(`/api/v1/sessions/${sessionId}/messages`, {
      params: { page, size },
    });
    return response.data;
  },

  async addMessage(sessionId, sender, content, context = null) {
    const response = await api.post(`/api/v1/sessions/${sessionId}/messages`, {
      sender,
      content,
      context,
    });
    return response.data;
  },
};
```

---

### **5. src/services/aiService.js**

```javascript
import OpenAI from 'openai';

const AI_PROVIDER = import.meta.env.VITE_AI_PROVIDER || 'openai';
const OPENAI_API_KEY = import.meta.env.VITE_OPENAI_API_KEY;
const OPENAI_MODEL = import.meta.env.VITE_OPENAI_MODEL || 'gpt-4-turbo-preview';

let openai = null;

if (OPENAI_API_KEY && AI_PROVIDER === 'openai') {
  openai = new OpenAI({
    apiKey: OPENAI_API_KEY,
    dangerouslyAllowBrowser: true, // For demo only
  });
}

export const aiService = {
  async generateResponse(messages, includeRagContext = false) {
    if (!openai) {
      throw new Error('AI service not configured. Please add VITE_OPENAI_API_KEY to .env');
    }

    try {
      // Convert messages to OpenAI format
      const formattedMessages = messages.map(msg => ({
        role: msg.sender === 'USER' ? 'user' : 'assistant',
        content: msg.content,
      }));

      const completion = await openai.chat.completions.create({
        model: OPENAI_MODEL,
        messages: formattedMessages,
      });

      const aiResponse = completion.choices[0].message.content;

      // Simulate RAG context (in production, this would come from your RAG system)
      let ragContext = null;
      if (includeRagContext) {
        ragContext = [{
          sourceId: `doc-${Date.now()}`,
          snippet: `Simulated RAG context for: "${messages[messages.length - 1].content.substring(0, 50)}..."`,
          metadata: {
            source: 'Knowledge Base',
            confidence: 0.95,
            timestamp: new Date().toISOString(),
          },
        }];
      }

      return {
        content: aiResponse,
        context: ragContext,
      };
    } catch (error) {
      console.error('AI Service Error:', error);
      throw new Error('Failed to generate AI response: ' + error.message);
    }
  },
};
```

---

### **6. src/components/SessionList.jsx**

```jsx
import React, { useState } from 'react';
import { Star, Trash2, Edit2, Check, X } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

function SessionList({ 
  sessions, 
  currentSession, 
  onSelectSession, 
  onDeleteSession, 
  onRenameSession,
  onToggleFavorite 
}) {
  const [editingId, setEditingId] = useState(null);
  const [editTitle, setEditTitle] = useState('');

  const startEditing = (session) => {
    setEditingId(session.id);
    setEditTitle(session.title);
  };

  const saveEdit = (sessionId) => {
    if (editTitle.trim()) {
      onRenameSession(sessionId, editTitle);
    }
    setEditingId(null);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditTitle('');
  };

  return (
    <div className="flex-1 overflow-y-auto">
      {sessions.length === 0 ? (
        <div className="p-4 text-center text-gray-500">
          No sessions yet. Create one to start chatting!
        </div>
      ) : (
        <div className="space-y-1 p-2">
          {sessions.map((session) => (
            <div
              key={session.id}
              className={`p-3 rounded-lg cursor-pointer transition-colors ${
                currentSession?.id === session.id
                  ? 'bg-blue-50 border-l-4 border-blue-600'
                  : 'hover:bg-gray-50'
              }`}
              onClick={() => !editingId && onSelectSession(session)}
            >
              {editingId === session.id ? (
                <div className="flex items-center gap-2" onClick={(e) => e.stopPropagation()}>
                  <input
                    type="text"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                    className="flex-1 px-2 py-1 border border-gray-300 rounded"
                    autoFocus
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') saveEdit(session.id);
                      if (e.key === 'Escape') cancelEdit();
                    }}
                  />
                  <button
                    onClick={() => saveEdit(session.id)}
                    className="p-1 text-green-600 hover:bg-green-50 rounded"
                  >
                    <Check size={16} />
                  </button>
                  <button
                    onClick={cancelEdit}
                    className="p-1 text-red-600 hover:bg-red-50 rounded"
                  >
                    <X size={16} />
                  </button>
                </div>
              ) : (
                <>
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="font-medium text-gray-800 text-sm flex-1 line-clamp-1">
                      {session.title}
                    </h3>
                    <div className="flex items-center gap-1" onClick={(e) => e.stopPropagation()}>
                      <button
                        onClick={() => onToggleFavorite(session.id, !session.favorite)}
                        className={`p-1 rounded ${
                          session.favorite ? 'text-yellow-500' : 'text-gray-400 hover:text-yellow-500'
                        }`}
                      >
                        <Star size={14} fill={session.favorite ? 'currentColor' : 'none'} />
                      </button>
                      <button
                        onClick={() => startEditing(session)}
                        className="p-1 text-gray-400 hover:text-blue-600 rounded"
                      >
                        <Edit2 size={14} />
                      </button>
                      <button
                        onClick={() => onDeleteSession(session.id)}
                        className="p-1 text-gray-400 hover:text-red-600 rounded"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    {formatDistanceToNow(new Date(session.updatedAt), { addSuffix: true })}
                  </p>
                </>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default SessionList;
```

---

## 🚀 **Next Steps**

I've created the **complete project structure and core files**. 

**To complete the implementation, you need to:**

1. **Copy the code above** for each file into the corresponding location
2. **Create remaining component files** (ChatInterface.jsx, MessageBubble.jsx)
3. **Install Tailwind CSS** configuration
4. **Add your OpenAI API key** to `.env`

Would you like me to:
1. ✅ Create the remaining React components (ChatInterface, MessageBubble)?
2. ✅ Add Tailwind CSS configuration?
3. ✅ Create a Docker setup for the frontend?
4. ✅ Add alternative AI providers (Claude, Gemini)?

Let me know and I'll continue with the complete implementation!

