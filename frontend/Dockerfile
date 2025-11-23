FROM node:18-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code and .env file
COPY . .

# Accept build arguments for Vite environment variables
ARG VITE_GROQ_API_KEY
ARG VITE_GROQ_MODEL
ARG VITE_API_URL
ARG VITE_API_KEY
ARG VITE_DEFAULT_USER_ID

# Set as environment variables - these will be read by Vite at runtime
ENV VITE_GROQ_API_KEY=${VITE_GROQ_API_KEY}
ENV VITE_GROQ_MODEL=${VITE_GROQ_MODEL:-llama-3.3-70b-versatile}
ENV VITE_API_URL=${VITE_API_URL:-http://localhost}
ENV VITE_API_KEY=${VITE_API_KEY:-changeme}
ENV VITE_DEFAULT_USER_ID=${VITE_DEFAULT_USER_ID:-demo-user}

# Expose port
EXPOSE 3000

# Use dev mode which reads environment variables at runtime
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]


