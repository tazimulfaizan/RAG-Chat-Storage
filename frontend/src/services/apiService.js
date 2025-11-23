import axios from 'axios';

// Use port 80 (nginx with rate limiting) instead of direct backend port 8082
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost';
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

  async addMessage(sessionId, sender, content, userId, context = null) {
    const response = await api.post(`/api/v1/sessions/${sessionId}/messages`, {
      sender,
      content,
      userId,
      context,
    });
    return response.data;
  },
};
