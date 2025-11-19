import React, { useState, useEffect } from 'react';
import SessionList from './components/SessionList';
import ChatInterface from './components/ChatInterface';
import LoginScreen from './components/LoginScreen';
import UserHeader from './components/UserHeader';
import { apiService } from './services/apiService';

function App() {
  const [sessions, setSessions] = useState([]);
  const [currentSession, setCurrentSession] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [userId, setUserId] = useState(null);

  // Load userId from localStorage on mount
  useEffect(() => {
    const savedUserId = localStorage.getItem('rag_chat_user_id');
    if (savedUserId) {
      setUserId(savedUserId);
    }
  }, []);

  // Load sessions when userId changes
  useEffect(() => {
    if (userId) {
      loadSessions();
    }
  }, [userId]);

  const handleLogin = (newUserId) => {
    localStorage.setItem('rag_chat_user_id', newUserId);
    setUserId(newUserId);
    setSessions([]);
    setCurrentSession(null);
    setError(null);
  };

  const handleLogout = () => {
    if (window.confirm(`Switch user? Current sessions for "${userId}" will remain saved.`)) {
      localStorage.removeItem('rag_chat_user_id');
      setUserId(null);
      setSessions([]);
      setCurrentSession(null);
      setError(null);
    }
  };

  // Show login screen if no userId
  if (!userId) {
    return <LoginScreen onLogin={handleLogin} />;
  }

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
    <div className="flex h-screen bg-gray-100 flex-col">
      {/* User Header */}
      <UserHeader userId={userId} onLogout={handleLogout} />

      {/* Main Content */}
      <div className="flex flex-1 overflow-hidden">
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
  </div>
  );
}

export default App;

