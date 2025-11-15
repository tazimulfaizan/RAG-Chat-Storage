#!/bin/bash

#######################################################
# RAG Chat Storage - Clean Build Artifacts
# Removes node_modules, .gradle, build folders, etc.
#######################################################

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo "========================================================"
echo "  🧹 RAG Chat Storage - Cleaning Build Artifacts"
echo "========================================================"
echo ""

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Track what we're cleaning
CLEANED=0

#######################################################
# 1. Clean Frontend (node_modules)
#######################################################

if [ -d "frontend/node_modules" ]; then
    echo "🗑️  Removing frontend/node_modules..."
    rm -rf frontend/node_modules
    echo -e "${GREEN}✅ Removed frontend/node_modules${NC}"
    CLEANED=$((CLEANED + 1))
else
    echo "⏭️  frontend/node_modules not found (skipping)"
fi

#######################################################
# 2. Clean Gradle Cache
#######################################################

if [ -d ".gradle" ]; then
    echo "🗑️  Removing .gradle folder..."
    rm -rf .gradle
    echo -e "${GREEN}✅ Removed .gradle${NC}"
    CLEANED=$((CLEANED + 1))
else
    echo "⏭️  .gradle not found (skipping)"
fi

#######################################################
# 3. Clean Build Output
#######################################################

if [ -d "build" ]; then
    echo "🗑️  Removing build folder..."
    rm -rf build
    echo -e "${GREEN}✅ Removed build${NC}"
    CLEANED=$((CLEANED + 1))
else
    echo "⏭️  build not found (skipping)"
fi

#######################################################
# 4. Clean Frontend Build Output
#######################################################

if [ -d "frontend/dist" ]; then
    echo "🗑️  Removing frontend/dist..."
    rm -rf frontend/dist
    echo -e "${GREEN}✅ Removed frontend/dist${NC}"
    CLEANED=$((CLEANED + 1))
else
    echo "⏭️  frontend/dist not found (skipping)"
fi

#######################################################
# 5. Clean Log Files
#######################################################

if [ -f "backend.log" ]; then
    echo "🗑️  Removing backend.log..."
    rm backend.log
    echo -e "${GREEN}✅ Removed backend.log${NC}"
    CLEANED=$((CLEANED + 1))
fi

if [ -f "frontend.log" ]; then
    echo "🗑️  Removing frontend.log..."
    rm frontend.log
    echo -e "${GREEN}✅ Removed frontend.log${NC}"
    CLEANED=$((CLEANED + 1))
fi

#######################################################
# 6. Clean PID Files
#######################################################

if [ -f ".backend.pid" ]; then
    rm .backend.pid
    echo "🗑️  Removed .backend.pid"
    CLEANED=$((CLEANED + 1))
fi

if [ -f ".frontend.pid" ]; then
    rm .frontend.pid
    echo "🗑️  Removed .frontend.pid"
    CLEANED=$((CLEANED + 1))
fi

#######################################################
# 7. Clean Gradle Wrapper Cache (Optional)
#######################################################

if [ -d "$HOME/.gradle/caches" ]; then
    echo ""
    echo -e "${YELLOW}⚠️  Found Gradle global cache at ~/.gradle/caches${NC}"
    read -p "Do you want to clean global Gradle cache? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  Removing ~/.gradle/caches..."
        rm -rf "$HOME/.gradle/caches"
        echo -e "${GREEN}✅ Removed global Gradle cache${NC}"
        CLEANED=$((CLEANED + 1))
    else
        echo "⏭️  Skipped global Gradle cache"
    fi
fi

#######################################################
# 8. Clean npm Cache (Optional)
#######################################################

echo ""
echo -e "${YELLOW}⚠️  npm cache exists${NC}"
read -p "Do you want to clean npm cache? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️  Cleaning npm cache..."
    npm cache clean --force
    echo -e "${GREEN}✅ Cleaned npm cache${NC}"
    CLEANED=$((CLEANED + 1))
else
    echo "⏭️  Skipped npm cache"
fi

#######################################################
# 9. Summary
#######################################################

echo ""
echo "========================================================"
echo ""

if [ $CLEANED -gt 0 ]; then
    echo -e "${GREEN}✅ Cleanup Complete!${NC}"
    echo "   Cleaned $CLEANED item(s)"
else
    echo -e "${BLUE}ℹ️  Nothing to clean (already clean)${NC}"
fi

echo ""
echo "📊 Disk space freed up:"
df -h . | tail -1 | awk '{print "   Available: " $4}'

echo ""
echo "🔄 To rebuild after cleaning:"
echo "   Backend:  ./gradlew build"
echo "   Frontend: cd frontend && npm install"
echo "   Both:     ./start.sh"
echo ""
echo "========================================================"
echo ""

