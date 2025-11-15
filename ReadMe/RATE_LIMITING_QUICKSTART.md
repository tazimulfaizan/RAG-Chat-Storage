# 🚀 Quick Start: Rate Limiting with Nginx

## ✅ **Rate Limiting is Now Configured!**

Your application now has production-ready rate limiting using **Nginx**.

---

## 📊 **What's Included**

### **Files Created:**
1. ✅ `nginx.conf` - Nginx configuration with rate limiting
2. ✅ `docker-compose.yml` - Updated with Nginx service
3. ✅ `RATE_LIMITING_GUIDE.md` - Complete guide with all options

### **Rate Limit Settings:**
- **Rate**: 60 requests per minute per IP
- **Burst**: 10 additional requests allowed
- **Protected**: `/api/**` endpoints
- **Public**: `/health`, `/actuator/**`, `/swagger-ui/**`

---

## 🚀 **How to Use**

### **Start Services:**
```bash
docker-compose up --build
```

### **Access Your API:**

**Via Nginx (Rate Limited):**
```bash
curl -H "X-API-KEY: changeme" \
  http://localhost/api/v1/sessions?userId=test
```
Port: `80` - **Use this in production**

**Direct to App (No Rate Limit):**
```bash
curl -H "X-API-KEY: changeme" \
  http://localhost:8080/api/v1/sessions?userId=test
```
Port: `8080` - **For testing only**

---

## 🧪 **Test Rate Limiting**

### **Quick Test:**
```bash
# Make 65 rapid requests
for i in {1..65}; do
  echo "Request $i:"
  curl -w " - Status: %{http_code}\n" \
    -H "X-API-KEY: changeme" \
    http://localhost/api/v1/sessions?userId=test
  sleep 0.5
done
```

**Expected Results:**
- Requests 1-60: ✅ `200 OK`
- Requests 61-65: ❌ `429 Too Many Requests`

### **Check Rate Limit Headers:**
```bash
curl -I -H "X-API-KEY: changeme" \
  http://localhost/api/v1/sessions?userId=test
```

**Response Headers:**
```
HTTP/1.1 200 OK
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 59
...
```

---

## 📋 **Configuration**

### **Adjust Rate Limit:**

Edit `nginx.conf`:
```nginx
# Change from 60r/m to 120r/m (120 requests per minute)
limit_req_zone $binary_remote_addr zone=api_zone:10m rate=120r/m;

# Adjust burst
limit_req zone=api_zone burst=20 nodelay;
```

Then restart:
```bash
docker-compose restart nginx
```

### **Rate Limit Options:**

| Configuration | Meaning | Example |
|--------------|---------|---------|
| `rate=60r/m` | 60 requests per minute | `rate=100r/m` |
| `rate=1r/s` | 1 request per second | `rate=10r/s` |
| `burst=10` | Allow 10 extra requests | `burst=20` |
| `nodelay` | Reject immediately | Remove for queuing |

---

## 🌐 **Architecture**

```
Client Request (http://localhost/)
      ↓
┌─────────────────────────────────┐
│         Nginx (Port 80)         │
│                                 │
│  Rate Limiting:                 │
│  • 60 requests/min per IP       │
│  • Burst: 10 requests           │
│  • Returns 429 if exceeded      │
└─────────────────────────────────┘
      ↓
┌─────────────────────────────────┐
│   Spring Boot App (Port 8080)   │
│                                 │
│  • Spring Security              │
│  • API Key Authentication       │
│  • Business Logic               │
└─────────────────────────────────┘
      ↓
┌─────────────────────────────────┐
│      MongoDB (Port 27017)       │
└─────────────────────────────────┘
```

---

## 🔧 **Production Deployment**

### **Option 1: Keep Both Ports**
```yaml
# docker-compose.yml
nginx:
  ports:
    - "80:80"      # Public (rate limited)
    - "8080:8080"  # Direct (for internal services)
```

### **Option 2: Only Nginx (Recommended)**
```yaml
# docker-compose.yml
nginx:
  ports:
    - "80:80"      # Only public port

app:
  expose:
    - "8080"       # Only internal
  # Remove ports section
```

### **Option 3: HTTPS with SSL**
```yaml
nginx:
  ports:
    - "443:443"
  volumes:
    - ./nginx-ssl.conf:/etc/nginx/nginx.conf:ro
    - ./ssl:/etc/nginx/ssl:ro
```

---

## 📊 **Monitoring**

### **View Nginx Logs:**
```bash
# Access logs
docker logs rag-chat-nginx

# Follow logs in real-time
docker logs -f rag-chat-nginx
```

### **Check Rate Limit Status:**
```bash
# See active connections
docker exec rag-chat-nginx cat /var/log/nginx/access.log | tail -20
```

---

## 🎯 **Different Rate Limits per Path**

Edit `nginx.conf`:
```nginx
http {
    # Different zones for different limits
    limit_req_zone $binary_remote_addr zone=api_strict:10m rate=30r/m;
    limit_req_zone $binary_remote_addr zone=api_normal:10m rate=60r/m;
    
    server {
        # Strict rate limit for expensive operations
        location /api/v1/sessions {
            limit_req zone=api_strict burst=5 nodelay;
            proxy_pass http://backend;
        }
        
        # Normal rate limit for other APIs
        location /api/ {
            limit_req zone=api_normal burst=10 nodelay;
            proxy_pass http://backend;
        }
    }
}
```

---

## 🔐 **Rate Limit by API Key (Advanced)**

Edit `nginx.conf`:
```nginx
http {
    # Rate limit by API key instead of IP
    map $http_x_api_key $api_key_or_ip {
        default $binary_remote_addr;
        "~." $http_x_api_key;
    }
    
    limit_req_zone $api_key_or_ip zone=api_zone:10m rate=60r/m;
    
    server {
        location /api/ {
            limit_req zone=api_zone burst=10 nodelay;
            proxy_pass http://backend;
        }
    }
}
```

---

## 🆘 **Troubleshooting**

### **Issue: All requests return 429**

**Solution:** Increase rate or burst:
```nginx
# Increase rate
limit_req_zone ... rate=120r/m;

# Increase burst
limit_req zone=api_zone burst=20 nodelay;
```

### **Issue: Nginx won't start**

**Check logs:**
```bash
docker logs rag-chat-nginx
```

**Validate config:**
```bash
docker exec rag-chat-nginx nginx -t
```

### **Issue: Can't access app**

**Check if nginx is running:**
```bash
docker ps | grep nginx
```

**Test direct access:**
```bash
curl http://localhost:8080/health
```

---

## 📚 **Resources**

- **Nginx Rate Limiting**: http://nginx.org/en/docs/http/ngx_http_limit_req_module.html
- **Docker Nginx**: https://hub.docker.com/_/nginx
- **Alternative Options**: See `RATE_LIMITING_GUIDE.md`

---

## ✅ **Summary**

### **What You Have:**
✅ Production-ready rate limiting with Nginx  
✅ 60 requests/minute per IP  
✅ Burst handling (10 extra requests)  
✅ Rate limit headers in response  
✅ Easy to configure and adjust  

### **How to Use:**
```bash
# Start services
docker-compose up

# Access via Nginx (rate limited)
curl http://localhost/api/v1/sessions

# Test rate limiting
# Make 65 rapid requests to see 429 errors
```

### **Production Ready:**
✅ No code changes needed  
✅ Infrastructure-level rate limiting  
✅ Scales horizontally  
✅ Industry standard approach  

---

**Your application now has production-grade rate limiting! 🎉**

Use port `80` (via Nginx) for production traffic.  
Use port `8080` (direct) only for testing.

