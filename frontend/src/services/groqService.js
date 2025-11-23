/**
 * Groq AI Service - FREE AI Service (no credit card required!)
 *
 * Groq provides free API access to fast LLM models like Llama 3.3 70B
 * Configuration is managed through values.yaml and docker-compose.yml
 *
 * To get started:
 * 1. Go to https://console.groq.com/
 * 2. Sign up for free (no credit card needed)
 * 3. Create an API key
 * 4. Add it to chart/values.yaml under frontend.groq.apiKey
 *    OR set GROQ_API_KEY environment variable
 */

// Read from Vite environment variables (passed from docker-compose/values.yaml)
const GROQ_API_KEY = import.meta.env.VITE_GROQ_API_KEY;
const GROQ_MODEL = import.meta.env.VITE_GROQ_MODEL || 'llama-3.3-70b-versatile';
const GROQ_API_URL = 'https://api.groq.com/openai/v1/chat/completions';

// Simulate delay for better UX
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

export const groqService = {
  async generateResponse(messages, includeRagContext = false) {
    if (!GROQ_API_KEY || GROQ_API_KEY === '' || GROQ_API_KEY === 'undefined') {
      throw new Error(
        '🔑 Groq API key not configured!\n\n' +
        '📝 Get your FREE key from: https://console.groq.com/\n' +
        '⚙️  Add it to chart/values.yaml under frontend.groq.apiKey\n' +
        '🔄 Or set GROQ_API_KEY environment variable'
      );
    }

    try {
      // Convert messages to OpenAI-compatible format (Groq uses OpenAI format)
      const formattedMessages = messages
        .filter(msg => msg.sender !== 'SYSTEM')
        .map(msg => ({
          role: msg.sender === 'USER' ? 'user' : 'assistant',
          content: msg.content,
        }));

      // Add system prompt at the beginning
      formattedMessages.unshift({
        role: 'system',
        content: 'You are a helpful AI assistant. Provide clear, concise, and accurate responses.',
      });

      // Call Groq API (uses OpenAI-compatible format)
      const response = await fetch(GROQ_API_URL, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${GROQ_API_KEY}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model: GROQ_MODEL,
          messages: formattedMessages,
          temperature: 0.7,
          max_tokens: 1000,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error?.message || `Groq API error: ${response.status}`);
      }

      const data = await response.json();
      const aiResponse = data.choices[0].message.content;

      // Simulate RAG context (in production, this would come from your RAG system)
      let ragContext = null;
      if (includeRagContext) {
        ragContext = [{
          sourceId: `doc-${Date.now()}`,
          snippet: `Context retrieved for: "${messages[messages.length - 1].content.substring(0, 50)}..."`,
          metadata: {
            source: 'Knowledge Base',
            confidence: 0.92,
            timestamp: new Date().toISOString(),
            model: GROQ_MODEL,
            provider: 'Groq (FREE)',
          },
        }];
      }

      // Add slight delay for better UX
      await delay(300);

      return {
        content: aiResponse,
        context: ragContext,
      };

    } catch (error) {
      console.error('Groq API error:', error);
      throw new Error(`Failed to generate AI response: ${error.message}`);
    }
  },
};

export default groqService;
