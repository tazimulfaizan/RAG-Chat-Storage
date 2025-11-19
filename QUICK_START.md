# 🚀 Quick Start - Docker Environment

## ✅ Everything is Running!

All your services are now running in Docker with rate limiting enabled.

---

## 🌐 Access URLs

```
Frontend:      http://localhost:3000
API:           http://localhost/api/v1/...
Swagger:       http://localhost:8082/swagger-ui/index.html
Mongo Express: http://localhost:8081  (admin/admin)
Health:        http://localhost/actuator/health
```

---

## 🎯 Quick Commands

### Start All Services:
```bash
docker-compose up -d
```

### Stop All Services:
```bash
docker-compose down
```

### View Status:
```bash
docker ps
```

### View Logs:
```bash
docker logs -f rag-chat-nginx         # Nginx
docker logs -f rag-chat-storage-app   # Backend
docker logs -f rag-chat-frontend      # Frontend
```

### Restart a Service:
```bash
docker restart rag-chat-nginx
```

---

## 📊 What's Running

- ✅ **Frontend** (React) → Port 3000
- ✅ **Nginx** (Rate Limiting) → Port 80
- ✅ **Backend** (Spring Boot) → Port 8082  
- ✅ **MongoDB** → Port 27017
- ✅ **Mongo Express** → Port 8081

---

## 🔒 Rate Limiting

- **Limit:** 60 requests/minute per IP
- **Burst:** 10 extra requests allowed
- **Port:** 80 (through Nginx)
- **Status:** ✅ Active and verified

---

## 📝 Notes

- Frontend uses **port 80** (rate limited) ✅
- Direct backend access on **port 8082** (no rate limiting)
- All data persists in Docker volumes
- Use `docker-compose down -v` to delete all data

---

**Status:** ✅ ALL OPERATIONAL  
**Created:** November 19, 2025

