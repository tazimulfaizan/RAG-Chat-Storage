# ✅ Complete Environment-Based Configuration

## 🎉 **ALL Configuration in .env File!**

Your application reads **ALL** configurable values from a single `.env` file.

---

## 📊 **How It Works**

```
.env file (Single Source of Truth)
    ↓
Docker Compose reads .env
    ↓
├─────────────────────┬─────────────────────┐
│                     │                     │
Nginx                 Spring Boot           MongoDB
(nginx.conf.template) (application.yml)    (docker-compose)
    ↓                     ↓                     ↓
Rate Limiting         All App Config        Connection
```

---

## 📁 **File Structure**

```
rag-chat-storage/
├── .env                    ✅ SINGLE CONFIGURATION FILE (edit this!)
├── .env.example            📋 Documentation template (don't edit)
├── setup-env.sh            🛠️  Creates .env file
├── application.yml         ☕ Uses ${ENV_VAR:default}
├── nginx.conf.template     🌐 Uses ${ENV_VAR}
└── docker-compose.yml      🐳 Passes env vars
```

**Key Point:** Only `.env` is used by Docker. `.env.example` is just for reference.

---

## 📋 **.env File Contents**

Your `.env` file contains **ALL 30+ configurable values:**

```bash
# APPLICATION
APP_NAME=rag-chat-storage
SERVER_PORT=8080

# MONGODB
MONGODB_URI=mongodb://mongo:27017/rag-chat-storage

# CACHE
CACHE_TYPE=caffeine
CACHE_SPEC=maximumSize=1000,expireAfterWrite=10m,expireAfterAccess=5m,recordStats

# SECURITY
SECURITY_API_KEY=changeme
SECURITY_API_KEY_HEADER=X-API-KEY

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000
CORS_ALLOWED_METHODS=GET,POST,PATCH,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=*
CORS_MAX_AGE=3600

# PAGINATION
PAGINATION_DEFAULT_PAGE_SIZE=20
PAGINATION_MAX_PAGE_SIZE=100

# ACTUATOR
ACTUATOR_ENDPOINTS=health,info,metrics,caches
ACTUATOR_HEALTH_PROBES=true
ACTUATOR_CACHE_METRICS=true

# LOGGING
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_WEB=DEBUG
LOG_LEVEL_SECURITY=DEBUG
LOG_PATTERN_CONSOLE=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
LOG_PATTERN_FILE=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# ACCESS LOGS
ACCESS_LOG_ENABLED=true
ACCESS_LOG_PATTERN=%h %l %u %t \"%r\" %s %b %D
ACCESS_LOG_DIRECTORY=logs
ACCESS_LOG_PREFIX=access_log
ACCESS_LOG_SUFFIX=.log

# SWAGGER
SWAGGER_ENABLED=true
SWAGGER_API_DOCS_PATH=/v3/api-docs
SWAGGER_UI_PATH=/swagger-ui.html
SWAGGER_TITLE=RAG Chat Storage Microservice API
SWAGGER_VERSION=1.0
SWAGGER_DESCRIPTION=Production-ready backend microservice

# NGINX RATE LIMITING
RATE_LIMIT_PER_MINUTE=60
RATE_LIMIT_BURST=10
```

---

## 🚀 **Quick Start**

### **Option 1: Use Setup Script (Recommended)**
```bash
cd /Users/tazimul.faizan/Downloads/rag-chat-storage
./setup-env.sh
```

### **Option 2: Manual Edit**
```bash
nano .env
# Add all variables from above
```

### **Start Application**
```bash
docker-compose up --build
```

---

## 📊 **All Configurable Values**

| Category | Variables | Used By |
|----------|-----------|---------|
| **Application** | `APP_NAME`, `SERVER_PORT` | Spring Boot |
| **MongoDB** | `MONGODB_URI` | Spring Boot + Docker |
| **Cache** | `CACHE_TYPE`, `CACHE_SPEC` | Spring Boot (Caffeine) |
| **Security** | `SECURITY_API_KEY`, `SECURITY_API_KEY_HEADER` | Spring Security |
| **CORS** | `CORS_ALLOWED_ORIGINS`, `CORS_ALLOWED_METHODS`, etc. | Spring Boot |
| **Pagination** | `PAGINATION_DEFAULT_PAGE_SIZE`, `PAGINATION_MAX_PAGE_SIZE` | Controllers |
| **Actuator** | `ACTUATOR_ENDPOINTS`, `ACTUATOR_HEALTH_PROBES`, etc. | Spring Actuator |
| **Logging** | `LOG_LEVEL_*`, `LOG_PATTERN_*` | Logback |
| **Access Logs** | `ACCESS_LOG_*` | Tomcat |
| **Swagger** | `SWAGGER_*` | SpringDoc |
| **Rate Limiting** | `RATE_LIMIT_PER_MINUTE`, `RATE_LIMIT_BURST` | Nginx |

**Total: 30+ values in ONE file!**

---

## 🔄 **How to Change Configuration**

### **1. Edit .env:**
```bash
nano .env
```

### **2. Change values:**
```bash
# Example: Increase rate limit
RATE_LIMIT_PER_MINUTE=120

# Example: Change log level
LOG_LEVEL_APP=INFO

# Example: Disable Swagger
SWAGGER_ENABLED=false
```

### **3. Restart services:**
```bash
# Restart specific service
docker-compose restart nginx
docker-compose restart app

# Or restart all
docker-compose restart
```

### **4. Done!** ✅

---

## 🎯 **Configuration Examples**

### **Development:**
```bash
# .env
RATE_LIMIT_PER_MINUTE=300
LOG_LEVEL_ROOT=DEBUG
LOG_LEVEL_APP=TRACE
SWAGGER_ENABLED=true
CACHE_SPEC=maximumSize=100,expireAfterWrite=1m
```

### **Production:**
```bash
# .env
RATE_LIMIT_PER_MINUTE=60
LOG_LEVEL_ROOT=WARN
LOG_LEVEL_APP=INFO
SWAGGER_ENABLED=false
SECURITY_API_KEY=strong-prod-key-32-characters-min
MONGODB_URI=mongodb://user:pass@prod-mongo:27017/db
```

### **High Traffic:**
```bash
# .env
RATE_LIMIT_PER_MINUTE=1000
RATE_LIMIT_BURST=100
CACHE_SPEC=maximumSize=10000,expireAfterWrite=30m
PAGINATION_MAX_PAGE_SIZE=500
```

---

## ✅ **Verification**

### **Check .env exists:**
```bash
ls -la .env
```

### **View contents:**
```bash
cat .env
```

### **Test Docker reads it:**
```bash
docker-compose config | grep -A 3 "SECURITY_API_KEY"
```

### **Check app uses it:**
```bash
docker-compose up -d
docker logs rag-chat-storage-app | head -20
```

---

## 🔧 **How application.yml Uses .env**

### **Pattern:**
```yaml
property: ${ENV_VAR:default_value}
```

### **Example:**
```yaml
security:
  api-key: ${SECURITY_API_KEY:changeme}
```

**Flow:**
1. Docker Compose reads `SECURITY_API_KEY=changeme` from `.env`
2. Passes it to container as environment variable
3. Spring Boot reads `${SECURITY_API_KEY}` from environment
4. Falls back to `changeme` if not set

---

## 🆘 **Troubleshooting**

### **Issue: .env doesn't exist**
```bash
./setup-env.sh
```

### **Issue: Changes not applied**
```bash
docker-compose down
docker-compose up --build
```

### **Issue: Variables show as ${VAR}**
```bash
# Check .env syntax
cat .env | grep "="

# Restart Docker Compose
docker-compose restart
```

---

## 📚 **Environment-Specific Configs**

### **Multiple Environments:**

```bash
# Create environment-specific files
.env.dev
.env.staging
.env.prod

# Use specific environment
cp .env.prod .env
docker-compose up
```

### **Or use Docker Compose override:**
```bash
docker-compose --env-file .env.prod up
```

---

## ✅ **Summary**

### **Single Configuration File:**
✅ **Only .env** - All 30+ values in one place  
✅ **application.yml** - Uses environment variables  
✅ **nginx.conf.template** - Uses environment variables  
✅ **docker-compose.yml** - Reads .env automatically  

### **How to Configure:**
1. Edit `.env`
2. Run `docker-compose restart`
3. Done! ✅

### **Files:**
- **`.env`** - YOUR configuration (edit this!)
- **`.env.example`** - Documentation template (reference only)
- **`setup-env.sh`** - Creates .env file
- **`application.yml`** - Spring config (reads from .env)
- **`nginx.conf.template`** - Nginx config (reads from .env)

---

## 🎉 **Complete!**

**All configuration is in ONE file: `.env`**

```bash
# View config
cat .env

# Edit config
nano .env

# Apply changes
docker-compose restart

# Start app
docker-compose up
```

**No hardcoded values - everything is configurable!** ✅

