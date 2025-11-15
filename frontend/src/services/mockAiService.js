/**
 * Mock AI Service - Simulates OpenAI responses
 * Use this when you don't have OpenAI API credits or want to test without API calls
 *
 * To switch back to real OpenAI, change import in ChatInterface.jsx:
 * from './services/mockAiService' to './services/aiService'
 */

// Simulate AI thinking delay
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// Mock AI responses based on user input
const generateMockResponse = (userMessage) => {
  const lowerMessage = userMessage.toLowerCase();

  // Context-aware responses
  if (lowerMessage.includes('hello') || lowerMessage.includes('hi')) {
    return "Hello! I'm a mock AI assistant. I'm here to help you test the RAG Chat Storage system. How can I assist you today?";
  }

  if (lowerMessage.includes('what') && lowerMessage.includes('ai')) {
    return "Artificial Intelligence (AI) refers to the simulation of human intelligence in machines that are programmed to think and learn. AI systems can perform tasks such as visual perception, speech recognition, decision-making, and language translation.";
  }

  if (lowerMessage.includes('rag')) {
    return "RAG (Retrieval-Augmented Generation) is a technique that enhances AI responses by retrieving relevant information from a knowledge base before generating an answer. This system stores chat histories with RAG context for future reference.";
  }

  if (lowerMessage.includes('test')) {
    return "This is a mock AI response for testing purposes. Your message has been stored in MongoDB along with this response and simulated RAG context. The system is working correctly!";
  }

  // Default intelligent response
  return `I understand you're asking about "${userMessage}". This is a simulated AI response for testing your RAG Chat Storage system. In production, this would be replaced with actual OpenAI GPT responses. Your messages are being stored in MongoDB successfully!`;
};

// Generate mock RAG context
const generateMockContext = (userMessage) => {
  return [
    {
      sourceId: `doc-${Date.now()}-1`,
      snippet: `Mock retrieved context for query: "${userMessage.substring(0, 50)}...". This simulates information retrieved from a knowledge base.`,
      metadata: {
        source: 'Mock Knowledge Base',
        document: 'test_document.pdf',
        page: Math.floor(Math.random() * 100) + 1,
        confidence: 0.85 + Math.random() * 0.14, // 0.85-0.99
        timestamp: new Date().toISOString(),
        relevanceScore: 0.90,
      }
    },
    {
      sourceId: `doc-${Date.now()}-2`,
      snippet: `Additional context: This is the second piece of retrieved information related to your query. It demonstrates multi-source RAG context storage.`,
      metadata: {
        source: 'Mock Database',
        document: 'reference_guide.txt',
        confidence: 0.78 + Math.random() * 0.10,
        timestamp: new Date().toISOString(),
        relevanceScore: 0.75,
      }
    }
  ];
};

export const mockAiService = {
  async generateResponse(messages, includeRagContext = false) {
    try {
      // Simulate API call delay (500-1500ms)
      const thinkingTime = 500 + Math.random() * 1000;
      await delay(thinkingTime);

      // Get the last user message
      const lastMessage = messages[messages.length - 1];
      const userContent = lastMessage.content;

      // Generate contextual response
      const aiResponse = generateMockResponse(userContent);

      // Generate RAG context if requested
      let ragContext = null;
      if (includeRagContext) {
        ragContext = generateMockContext(userContent);
      }

      return {
        content: aiResponse,
        context: ragContext,
      };
    } catch (error) {
      console.error('Mock AI Service Error:', error);
      throw new Error('Failed to generate mock AI response: ' + error.message);
    }
  },

  isConfigured() {
    return true; // Mock AI is always "configured"
  },
};

// Export as default for easy import
export default mockAiService;

