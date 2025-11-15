import React, { useState } from 'react';
import { User, Bot, AlertCircle, ChevronDown, ChevronUp } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

function MessageBubble({ message }) {
  const [showContext, setShowContext] = useState(false);

  const isUser = message.sender === 'USER';
  const isAssistant = message.sender === 'ASSISTANT';
  const isSystem = message.sender === 'SYSTEM';

  const hasContext = message.context && message.context.length > 0;

  if (isSystem) {
    return (
      <div className="flex justify-center my-2">
        <div className="bg-yellow-100 border border-yellow-300 rounded-lg px-4 py-2 flex items-center gap-2 text-sm text-yellow-800">
          <AlertCircle size={16} />
          <span>{message.content}</span>
        </div>
      </div>
    );
  }

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'} items-start gap-3`}>
      {/* Avatar */}
      {isAssistant && (
        <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-bold flex-shrink-0">
          <Bot size={18} />
        </div>
      )}

      {/* Message Content */}
      <div className={`max-w-[70%] ${isUser ? 'order-first' : ''}`}>
        <div
          className={`rounded-lg p-4 shadow-sm ${
            isUser
              ? 'bg-blue-600 text-white'
              : 'bg-white text-gray-800'
          }`}
        >
          <p className="whitespace-pre-wrap break-words">{message.content}</p>
        </div>

        {/* Context Display */}
        {hasContext && (
          <div className="mt-2">
            <button
              onClick={() => setShowContext(!showContext)}
              className="flex items-center gap-1 text-sm text-gray-600 hover:text-gray-800 transition-colors"
            >
              {showContext ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
              <span>
                {showContext ? 'Hide' : 'Show'} RAG Context ({message.context.length})
              </span>
            </button>

            {showContext && (
              <div className="mt-2 space-y-2">
                {message.context.map((ctx, index) => (
                  <div
                    key={index}
                    className="bg-gray-50 border border-gray-200 rounded-lg p-3 text-sm"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-medium text-gray-700">
                        Source: {ctx.sourceId}
                      </span>
                      {ctx.metadata?.confidence && (
                        <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded">
                          {(ctx.metadata.confidence * 100).toFixed(1)}% confidence
                        </span>
                      )}
                    </div>
                    <p className="text-gray-600 italic">{ctx.snippet}</p>
                    {ctx.metadata && Object.keys(ctx.metadata).length > 0 && (
                      <div className="mt-2 pt-2 border-t border-gray-200">
                        <p className="text-xs text-gray-500 font-medium mb-1">Metadata:</p>
                        <div className="text-xs text-gray-600 space-y-1">
                          {Object.entries(ctx.metadata).map(([key, value]) => (
                            <div key={key}>
                              <span className="font-medium">{key}:</span>{' '}
                              {typeof value === 'object' ? JSON.stringify(value) : String(value)}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Timestamp */}
        <p className="text-xs text-gray-500 mt-1">
          {formatDistanceToNow(new Date(message.createdAt), { addSuffix: true })}
        </p>
      </div>

      {/* User Avatar */}
      {isUser && (
        <div className="w-8 h-8 rounded-full bg-gray-600 flex items-center justify-center text-white font-bold flex-shrink-0">
          <User size={18} />
        </div>
      )}
    </div>
  );
}

export default MessageBubble;

