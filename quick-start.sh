#!/bin/bash

echo "🚀 RAG Chat Storage - Quick Start Script"
echo "========================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check MongoDB
echo "📊 Checking MongoDB..."
if docker ps | grep -q mongo; then
    echo -e "${GREEN}✅ MongoDB is running on port 27017${NC}"
else
    echo -e "${YELLOW}⚠️  MongoDB not running. Starting it...${NC}"
    docker-compose up -d mongo
    sleep 5
fi

echo ""
echo "📋 System Status:"
echo "  • MongoDB:    Running on port 27017"
echo "  • Backend:    Will run on port 8082"
echo "  • Frontend:   Will run on port 3000"
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "🎯 To start the complete system:"
echo ""
echo -e "${GREEN}1. Start Backend:${NC}"
echo "   Run from IntelliJ (click Run button)"
echo "   OR from terminal:"
echo "   ./gradlew bootRun"
echo ""
echo -e "${GREEN}2. Start Frontend:${NC}"
echo "   cd frontend"
echo "   npm run dev"
echo ""
echo -e "${GREEN}3. Access Application:${NC}"
echo "   Frontend:  http://localhost:3000"
echo "   Backend:   http://localhost:8082"
echo "   Swagger:   http://localhost:8082/swagger-ui/index.html"
echo "   Mongo UI:  http://localhost:8081 (admin/admin)"
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "✅ Frontend Configuration:"
echo "   • API URL: http://localhost:8082"
echo "   • API Key: changeme"
echo "   • OpenAI: Configured"
echo ""
echo "✅ Backend Configuration:"
echo "   • Port: 8082"
echo "   • MongoDB: localhost:27017"
echo "   • Database: rag-chat-storage"
echo ""
echo "📚 Documentation:"
echo "   • Complete Guide: COMPLETE_SYSTEM_READY.md"
echo "   • Quick Start: QUICKSTART_FRONTEND.md"
echo "   • Port Config: SINGLE_PORT_CONFIG.md"
echo ""
echo -e "${GREEN}🎉 System is ready! Start backend and frontend now.${NC}"

