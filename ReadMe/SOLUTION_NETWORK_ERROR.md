# 🚨 SOLUTION: Frontend Not Starting - Network Error

## 🎯 **Problem Identified:**

1. ❌ **Docker daemon not running** - Need to start Docker Desktop
2. ❌ **Ports already in use** - Killed conflicting processes (✅ DONE)
3. ❌ **MongoDB not starting** - Docker needs to be running

---

## ✅ **STEP-BY-STEP FIX:**

### **Step 1: Start Docker Desktop**

1. Open **Docker Desktop** application
2. Wait for Docker to fully start (whale icon in menu bar should be steady)
3. Verify Docker is running:
   ```bash
   docker ps
   ```

---

### **Step 2: Start Backend Services**

```bash
cd /Users/tazimul.faizan/Downloads/rag-chat-storage

# Start MongoDB and Backend
docker-compose up -d mongo app

# Wait 30 seconds for services to start
sleep 30

# Check if they're running
docker-compose ps
```

**Expected output:** Both `mongo` and `app` should show "Up" status

---

### **Step 3: Verify Backend is Working**

```bash
# Test backend health
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}
```

---

### **Step 4: Start Frontend**

```bash
cd /Users/tazimul.faizan/Downloads/rag-chat-storage/frontend

# Make sure dependencies are installed
npm install

# Start frontend
npm run dev
```

**Frontend will start at:** http://localhost:3000

---

## 🔍 **Troubleshooting the "Network Error"**

### **Issue:** Frontend shows "Failed to load sessions: Network Error"

**Causes:**
1. ❌ Backend not running on port 8080
2. ❌ CORS not configured
3. ❌ Wrong API URL

**Solutions:**

### **1. Check Backend is Running:**
```bash
curl http://localhost:8080/actuator/health
```

If this fails, backend is not running. Start it:
```bash
docker-compose up -d app
docker-compose logs -f app
```

### **2. Check Frontend .env File:**
```bash
cat /Users/tazimul.faizan/Downloads/rag-chat-storage/frontend/.env
```

Should contain:
```env
VITE_API_URL=http://localhost:8080
VITE_API_KEY=changeme
VITE_OPENAI_API_KEY=your-openai-api-key-here
```

### **3. Check Backend CORS:**
```bash
grep CORS /Users/tazimul.faizan/Downloads/rag-chat-storage/.env
```

Should contain:
```env
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

---

## 🚀 **COMPLETE START SEQUENCE:**

Copy and paste these commands **one by one**:

```bash
# 1. Make sure Docker Desktop is running first!
docker ps

# 2. Go to project directory
cd /Users/tazimul.faizan/Downloads/rag-chat-storage

# 3. Stop everything
docker-compose down

# 4. Start backend services
docker-compose up -d mongo app

# 5. Wait for services to start
echo "Waiting 30 seconds for backend to start..."
sleep 30

# 6. Test backend
curl http://localhost:8080/actuator/health

# 7. Start frontend in a new terminal
cd frontend
npm run dev
```

---

## 📊 **Expected Results:**

### **After Step 6 (Backend Health Check):**
```json
{"status":"UP","components":{"diskSpace":{"status":"UP"},"mongo":{"status":"UP"},"ping":{"status":"UP"}}}
```

### **After Step 7 (Frontend Start):**
```
  VITE v5.4.21  ready in 823 ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

---

## ✅ **Verification Checklist:**

```bash
# 1. Docker is running
docker ps
# Should show containers

# 2. MongoDB is running
docker logs rag-chat-mongo
# Should show "Waiting for connections"

# 3. Backend is running
curl http://localhost:8080/actuator/health
# Should return {"status":"UP"}

# 4. Frontend is running
# Open http://localhost:3000
# Should show the chat interface (no error)

# 5. Can create session
# Click "+ New Chat" in the UI
# Should work without errors
```

---

## 🆘 **Still Getting "Network Error"?**

### **Check Browser Console:**
1. Open browser DevTools (F12)
2. Go to Console tab
3. Look for errors

**Common errors:**

#### **CORS Error:**
```
Access to XMLHttpRequest at 'http://localhost:8080' from origin 'http://localhost:3000' 
has been blocked by CORS policy
```

**Fix:** Add to backend `.env`:
```bash
echo "CORS_ALLOWED_ORIGINS=http://localhost:3000" >> /Users/tazimul.faizan/Downloads/rag-chat-storage/.env
docker-compose restart app
```

#### **Connection Refused:**
```
GET http://localhost:8080/api/v1/sessions?userId=demo-user net::ERR_CONNECTION_REFUSED
```

**Fix:** Backend not running
```bash
docker-compose up -d app
docker-compose logs -f app
```

---

## 📝 **Quick Reference:**

| Service | Port | Check Command | URL |
|---------|------|---------------|-----|
| Frontend | 3000 | `lsof -i :3000` | http://localhost:3000 |
| Backend | 8080 | `curl localhost:8080/actuator/health` | http://localhost:8080 |
| MongoDB | 27017 | `docker logs rag-chat-mongo` | Internal |

---

## 🎯 **Next Steps:**

1. ✅ **Start Docker Desktop**
2. ✅ **Run the complete start sequence above**
3. ✅ **Test backend health**
4. ✅ **Start frontend**
5. ✅ **Open http://localhost:3000**
6. ✅ **Click "+ New Chat"**
7. ✅ **Start chatting!**

---

**The issue is Docker is not running. Start Docker Desktop first, then follow the steps above!** 🐳

