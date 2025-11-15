import OpenAI from 'openai';

const AI_PROVIDER = import.meta.env.VITE_AI_PROVIDER || 'openai';
const OPENAI_API_KEY = import.meta.env.VITE_OPENAI_API_KEY;
const OPENAI_MODEL = import.meta.env.VITE_OPENAI_MODEL || 'gpt-4o-mini';

let openai = null;

if (OPENAI_API_KEY && AI_PROVIDER === 'openai') {
  openai = new OpenAI({
    apiKey: OPENAI_API_KEY,
    dangerouslyAllowBrowser: true, // For demo only - in production, use a backend proxy
  });
}

export const aiService = {
  async generateResponse(messages, includeRagContext = false) {
    if (!openai) {
      throw new Error('AI service not configured. Please add VITE_OPENAI_API_KEY to .env file');
    }

    try {
      // Convert messages to OpenAI format
      const formattedMessages = messages
        .filter(msg => msg.sender !== 'SYSTEM') // Exclude system messages
        .map(msg => ({
          role: msg.sender === 'USER' ? 'user' : 'assistant',
          content: msg.content,
        }));

      const completion = await openai.chat.completions.create({
        model: OPENAI_MODEL,
        messages: formattedMessages,
        temperature: 0.7,
        max_tokens: 1000,
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
            model: OPENAI_MODEL,
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

  isConfigured() {
    return !!openai;
  },
};
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

