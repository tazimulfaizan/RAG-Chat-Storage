#!/bin/bash

# =============================================================================
# RAG Chat Storage - Complete Startup Script
# =============================================================================
# This script starts the entire application stack:
# - MongoDB (Docker)
# - Backend (Spring Boot)
# - Frontend (React/Vite)
# =============================================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project paths
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$PROJECT_DIR/frontend"
BACKEND_PID_FILE="$PROJECT_DIR/.backend.pid"
FRONTEND_PID_FILE="$PROJECT_DIR/.frontend.pid"

# =============================================================================
# Helper Functions
# =============================================================================

print_banner() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════════╗"
    echo "║                                                                  ║"
    echo "║           🚀 RAG Chat Storage - Startup Script 🚀               ║"
    echo "║                                                                  ║"
    echo "╚══════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_step() {
    echo -e "${BLUE}▶ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if port is in use
check_port() {
    lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1
}

# Kill process on port
kill_port() {
    local port=$1
    if check_port $port; then
        print_warning "Port $port is already in use. Killing existing process..."
        lsof -ti:$port | xargs kill -9 2>/dev/null || true
        sleep 2
    fi
}

# Cleanup function
cleanup() {
    echo ""
    print_step "Shutting down services..."

    # Kill backend
    if [ -f "$BACKEND_PID_FILE" ]; then
        BACKEND_PID=$(cat "$BACKEND_PID_FILE")
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            print_step "Stopping backend (PID: $BACKEND_PID)..."
            kill $BACKEND_PID 2>/dev/null || true
        fi
        rm -f "$BACKEND_PID_FILE"
    fi

    # Kill frontend
    if [ -f "$FRONTEND_PID_FILE" ]; then
        FRONTEND_PID=$(cat "$FRONTEND_PID_FILE")
        if ps -p $FRONTEND_PID > /dev/null 2>&1; then
            print_step "Stopping frontend (PID: $FRONTEND_PID)..."
            kill $FRONTEND_PID 2>/dev/null || true
        fi
        rm -f "$FRONTEND_PID_FILE"
    fi

    # Stop Docker containers
    print_step "Stopping Docker containers..."
    cd "$PROJECT_DIR"
    docker-compose down 2>/dev/null || true

    print_success "All services stopped"
    exit 0
}

# Set up cleanup trap
trap cleanup SIGINT SIGTERM EXIT

# =============================================================================
# Pre-flight Checks
# =============================================================================

preflight_checks() {
    print_step "Running pre-flight checks..."

    # Check Docker
    if ! command_exists docker; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_success "Docker is ready"

    # Check Java
    if ! command_exists java; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    print_success "Java is ready"

    # Check Node.js
    if ! command_exists node; then
        print_error "Node.js is not installed. Please install Node.js first."
        exit 1
    fi
    print_success "Node.js is ready"

    # Check npm
    if ! command_exists npm; then
        print_error "npm is not installed. Please install npm first."
        exit 1
    fi
    print_success "npm is ready"

    # Check if .env exists
    if [ ! -f "$PROJECT_DIR/.env" ]; then
        print_warning ".env file not found. Using defaults..."
    else
        print_success ".env file found"
    fi

    # Check if frontend/.env exists
    if [ ! -f "$FRONTEND_DIR/.env" ]; then
        print_warning "frontend/.env file not found. Using defaults..."
    else
        print_success "frontend/.env file found"
    fi
}

# =============================================================================
# Start Services
# =============================================================================

start_mongodb() {
    print_step "Starting MongoDB..."

    cd "$PROJECT_DIR"

    # Check if MongoDB container is already running
    if docker ps | grep -q rag-chat-mongo; then
        print_success "MongoDB is already running"
        return
    fi

    # Start only MongoDB
    docker-compose up -d mongo

    # Wait for MongoDB to be ready
    print_step "Waiting for MongoDB to be ready..."
    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if docker exec rag-chat-mongo mongosh --eval "db.adminCommand('ping')" >/dev/null 2>&1; then
            print_success "MongoDB is ready"
            return
        fi
        attempt=$((attempt + 1))
        sleep 1
        echo -n "."
    done

    print_error "MongoDB failed to start"
    exit 1
}

start_mongo_express() {
    print_step "Starting Mongo Express..."

    cd "$PROJECT_DIR"
    docker-compose up -d mongo-express

    print_success "Mongo Express started at http://localhost:8081 (admin/admin)"
}

start_backend() {
    print_step "Starting Backend (Spring Boot)..."

    cd "$PROJECT_DIR"

    # Kill any process on port 8082
    kill_port 8082

    # Build the project
    print_step "Building backend..."
    ./gradlew build -x test > /dev/null 2>&1

    # Start Spring Boot application in background
    print_step "Starting Spring Boot application..."
    ./gradlew bootRun > backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > "$BACKEND_PID_FILE"

    # Wait for backend to be ready
    print_step "Waiting for backend to be ready..."
    local max_attempts=60
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:8082/actuator/health >/dev/null 2>&1; then
            print_success "Backend is ready at http://localhost:8082"
            return
        fi
        attempt=$((attempt + 1))
        sleep 1
        echo -n "."
    done

    print_error "Backend failed to start. Check backend.log for details."
    exit 1
}

start_frontend() {
    print_step "Starting Frontend (React/Vite)..."

    cd "$FRONTEND_DIR"

    # Kill any process on port 3000
    kill_port 3000

    # Check if node_modules exists
    if [ ! -d "node_modules" ]; then
        print_step "Installing frontend dependencies..."
        npm install
    fi

    # Start Vite dev server in background
    print_step "Starting Vite dev server..."
    npm run dev > ../frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > "$FRONTEND_PID_FILE"

    # Wait for frontend to be ready
    print_step "Waiting for frontend to be ready..."
    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:3000 >/dev/null 2>&1; then
            print_success "Frontend is ready at http://localhost:3000"
            return
        fi
        attempt=$((attempt + 1))
        sleep 1
        echo -n "."
    done

    print_warning "Frontend may still be starting. Check frontend.log for details."
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    print_banner

    # Run pre-flight checks
    preflight_checks
    echo ""

    # Start services in order
    start_mongodb
    echo ""

    start_mongo_express
    echo ""

    start_backend
    echo ""

    start_frontend
    echo ""

    # Print summary
    echo -e "${GREEN}"
    echo "╔══════════════════════════════════════════════════════════════════╗"
    echo "║                                                                  ║"
    echo "║                  ✅ ALL SERVICES STARTED! ✅                     ║"
    echo "║                                                                  ║"
    echo "╚══════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"

    echo ""
    echo -e "${BLUE}🌐 Access Your Application:${NC}"
    echo ""
    echo -e "  ${GREEN}Frontend (AI Chat):${NC}        http://localhost:3000"
    echo -e "  ${GREEN}Backend API:${NC}               http://localhost:8082"
    echo -e "  ${GREEN}API Documentation:${NC}         http://localhost:8082/swagger-ui/index.html"
    echo -e "  ${GREEN}Database Manager:${NC}          http://localhost:8081 (admin/admin)"
    echo -e "  ${GREEN}Health Check:${NC}              http://localhost:8082/actuator/health"
    echo ""

    echo -e "${BLUE}📋 Logs:${NC}"
    echo ""
    echo -e "  Backend:  tail -f $PROJECT_DIR/backend.log"
    echo -e "  Frontend: tail -f $PROJECT_DIR/frontend.log"
    echo ""

    echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}"
    echo ""

    # Keep script running and monitor processes
    while true; do
        # Check if backend is still running
        if [ -f "$BACKEND_PID_FILE" ]; then
            BACKEND_PID=$(cat "$BACKEND_PID_FILE")
            if ! ps -p $BACKEND_PID > /dev/null 2>&1; then
                print_error "Backend has stopped unexpectedly!"
                cleanup
            fi
        fi

        # Check if frontend is still running
        if [ -f "$FRONTEND_PID_FILE" ]; then
            FRONTEND_PID=$(cat "$FRONTEND_PID_FILE")
            if ! ps -p $FRONTEND_PID > /dev/null 2>&1; then
                print_error "Frontend has stopped unexpectedly!"
                cleanup
            fi
        fi

        sleep 5
    done
}

# Run main function
main

