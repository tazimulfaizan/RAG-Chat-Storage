#!/bin/bash

echo "🚀 Setting up RAG Chat Frontend..."
echo ""

cd "$(dirname "$0")"

# Check if package.json exists
if [ ! -f "package.json" ]; then
    echo "❌ Error: package.json not found!"
    exit 1
fi

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Copy .env.example to .env if it doesn't exist
if [ ! -f ".env" ]; then
    echo "📝 Creating .env file..."
    cp .env.example .env
    echo "⚠️  Please edit .env and add your OpenAI API key!"
else
    echo "✅ .env file already exists"
fi

echo ""
echo "✅ Frontend setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Edit .env and add your OpenAI API key: nano .env"
echo "2. Start development server: npm run dev"
echo "3. Open http://localhost:3000"
echo ""
echo "🚀 Or run with Docker: cd .. && docker-compose up --build"

