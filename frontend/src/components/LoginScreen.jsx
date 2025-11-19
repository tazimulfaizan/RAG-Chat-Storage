import React, { useState } from 'react';
import { User, LogIn } from 'lucide-react';

function LoginScreen({ onLogin }) {
  const [userId, setUserId] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!userId.trim()) {
      setError('Please enter your User ID');
      return;
    }

    // Validate userId format (alphanumeric, email, etc.)
    const validFormat = /^[a-zA-Z0-9@._-]+$/.test(userId.trim());
    if (!validFormat) {
      setError('User ID can only contain letters, numbers, @, ., _, and -');
      return;
    }

    onLogin(userId.trim());
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-full mb-4">
            <User className="text-white" size={32} />
          </div>
          <h1 className="text-3xl font-bold text-gray-800 mb-2">
            RAG Chat Storage
          </h1>
          <p className="text-gray-600">
            AI-Powered Chat with Context Retrieval
          </p>
        </div>

        {/* Login Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label
              htmlFor="userId"
              className="block text-sm font-medium text-gray-700 mb-2"
            >
              Enter Your User ID
            </label>
            <input
              id="userId"
              type="text"
              value={userId}
              onChange={(e) => {
                setUserId(e.target.value);
                setError('');
              }}
              placeholder="e.g., alice@example.com or user-123"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent"
              autoFocus
            />
            {error && (
              <p className="mt-2 text-sm text-red-600">{error}</p>
            )}
            <p className="mt-2 text-xs text-gray-500">
              Your User ID can be your email, username, or any unique identifier
            </p>
          </div>

          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition-colors font-medium flex items-center justify-center gap-2"
          >
            <LogIn size={20} />
            Continue to Chat
          </button>
        </form>

        {/* Info Section */}
        <div className="mt-6 p-4 bg-blue-50 rounded-lg">
          <p className="text-xs text-blue-800">
            <strong>Note:</strong> Each user ID has their own chat sessions and messages.
            You can switch between users to see different conversation histories.
          </p>
        </div>
      </div>
    </div>
  );
}

export default LoginScreen;
