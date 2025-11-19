import React, { useState, useEffect, useRef } from 'react';
import { Send, Loader2, AlertCircle } from 'lucide-react';
import MessageBubble from './MessageBubble';
import { apiService } from '../services/apiService';
import { mockAiService } from '../services/mockAiService';  // Using mock AI service (no OpenAI credits needed)

function ChatInterface({ session, userId }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (session) {
      loadMessages();
    }
  }, [session]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadMessages = async () => {
    try {
      setLoading(true);
      const data = await apiService.getMessages(session.id);
      setMessages(data.content || []);
      setError(null);
    } catch (err) {
      setError('Failed to load messages');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!input.trim() || aiLoading) return;

    const userMessage = input.trim();
    setInput('');
    setError(null);

    try {
      // 1. Save user message
      const savedUserMsg = await apiService.addMessage(
        session.id,
        'USER',
        userMessage,
        userId
      );
      setMessages(prev => [...prev, savedUserMsg]);

      // 2. Get AI response (using mock AI - no OpenAI API needed)
      setAiLoading(true);
      const conversationHistory = [
        ...messages.filter(m => m.sender !== 'SYSTEM'),
        { sender: 'USER', content: userMessage }
      ];

      const aiResponse = await mockAiService.generateResponse(
        conversationHistory,
        true // Include RAG context simulation
      );

      // 3. Save AI response with context
      const savedAiMsg = await apiService.addMessage(
        session.id,
        'ASSISTANT',
        aiResponse.content,
        userId,
        aiResponse.context
      );
      setMessages(prev => [...prev, savedAiMsg]);

    } catch (err) {
      setError('Failed to send message: ' + err.message);
      console.error(err);
    } finally {
      setAiLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <h2 className="text-lg font-semibold text-gray-800">{session.title}</h2>
        <p className="text-sm text-gray-500">Session ID: {session.id}</p>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
        {loading ? (
          <div className="flex justify-center items-center h-full">
            <Loader2 className="animate-spin text-blue-600" size={32} />
          </div>
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-500">
            <p className="text-lg mb-2">No messages yet</p>
            <p className="text-sm">Start a conversation with the AI assistant</p>
          </div>
        ) : (
          <>
            {messages.map((message) => (
              <MessageBubble key={message.id} message={message} />
            ))}
            {aiLoading && (
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-bold">
                  AI
                </div>
                <div className="bg-white rounded-lg p-4 shadow-sm">
                  <Loader2 className="animate-spin text-blue-600" size={20} />
                  <span className="ml-2 text-gray-600">Thinking...</span>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* Error Display */}
      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 m-4">
          <div className="flex items-center">
            <AlertCircle className="text-red-500 mr-2" size={20} />
            <p className="text-red-700">{error}</p>
          </div>
        </div>
      )}

      {/* Input */}
      <div className="bg-white border-t border-gray-200 p-4">
        <form onSubmit={handleSendMessage} className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type your message..."
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600"
            disabled={aiLoading}
          />
          <button
            type="submit"
            disabled={!input.trim() || aiLoading}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
          >
            {aiLoading ? (
              <Loader2 className="animate-spin" size={20} />
            ) : (
              <Send size={20} />
            )}
            Send
          </button>
        </form>
      </div>
    </div>
  );
}

export default ChatInterface;
