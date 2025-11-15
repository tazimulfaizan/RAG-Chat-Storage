#!/bin/bash

echo "🚀 Starting RAG Chat Storage - Complete System"
echo "=============================================="
echo ""

# Check if we're in the right directory
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

# Check if .env exists
if [ ! -f "frontend/.env" ]; then
    echo "❌ Error: frontend/.env not found"
    echo "Please create frontend/.env with your OpenAI API key"
    exit 1
fi

echo "✅ Configuration found"
echo ""
echo "🐳 Starting services with Docker Compose..."
echo ""

# Start all services
docker-compose up --build

echo ""
echo "=============================================="
echo "🎉 All services started!"
echo ""
echo "Access your application:"
echo "  🎨 Frontend:      http://localhost:3000"
echo "  🔌 Backend API:   http://localhost:8080"
echo "  📖 Swagger Docs:  http://localhost:8080/swagger-ui/index.html"
echo "  🗄️  Mongo Express: http://localhost:8081 (admin/admin)"
echo ""
echo "Press Ctrl+C to stop all services"
echo "=============================================="

