# Configuration Management - Property-Based Approach

## ✅ **Configuration Refactored to Use Properties**

All configurations are now managed through `application.yml` instead of hardcoded Java classes.

---

## 📁 **Configuration Structure**

### **Primary Configuration File**
```
src/main/resources/application.yml
```
This file contains ALL configurable settings for the application.

### **Supporting Classes** (Minimal Java Code)
```
src/main/java/com/example/ragchatstorage/config/
├── CorsConfig.java              - Minimal (reads from properties)
├── CorsProperties.java          - Property binding only
├── CacheConfig.java             - Just @EnableCaching (uses properties)
├── OpenApiConfig.java           - Security scheme only (info from properties)
├── PaginationProperties.java    - Property binding only
├── RateLimitingProperties.java  - Property binding only
└── SecurityProperties.java      - Property binding only
```

---

## 📋 **application.yml - Complete Configuration**

### **1. Spring Boot Configuration**
```yaml
spring:
  application:
    name: rag-chat-storage
  
  # MongoDB Configuration
  data:
    mongodb:
      uri: ${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/rag-chat-storage}
  
  # Cache Configuration (Caffeine)
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m,expireAfterAccess=5m,recordStats
```

### **2. Security Configuration**
```yaml
security:
  api-key: ${SECURITY_API_KEY:changeme}
  api-key-header: X-API-KEY
```

### **3. Rate Limiting Configuration**
```yaml
ratelimit:
  enabled: ${RATE_LIMIT_ENABLED:true}
  requests-per-minute: ${RATE_LIMIT_REQUESTS_PER_MINUTE:60}
```

### **4. Application Configuration**
```yaml
app:
  # CORS Configuration
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
    allowed-methods: GET,POST,PATCH,DELETE,OPTIONS
    allowed-headers: "*"
    max-age: 3600
  
  # Pagination Configuration
  pagination:
    default-page-size: ${PAGINATION_DEFAULT_PAGE_SIZE:20}
    max-page-size: ${PAGINATION_MAX_PAGE_SIZE:100}
```

### **5. Actuator Configuration**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
  endpoint:
    health:
      probes:
        enabled: true
  metrics:
    cache:
      instrument-cache: true
```

### **6. OpenAPI/Swagger Configuration**
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  info:
    title: RAG Chat Storage Microservice API
    version: 1.0
    description: Production-ready backend microservice to store chat sessions, messages, and RAG context
```

### **7. Logging Configuration**
```yaml
logging:
  level:
    root: INFO
    com.example.ragchatstorage: DEBUG
```

---

## 🔧 **Environment Variables**

All configurations support environment variable overrides:

| Property | Environment Variable | Default Value |
|----------|---------------------|---------------|
| `spring.data.mongodb.uri` | `SPRING_DATA_MONGODB_URI` | `mongodb://localhost:27017/rag-chat-storage` |
| `security.api-key` | `SECURITY_API_KEY` | `changeme` |
| `ratelimit.enabled` | `RATE_LIMIT_ENABLED` | `true` |
| `ratelimit.requests-per-minute` | `RATE_LIMIT_REQUESTS_PER_MINUTE` | `60` |
| `app.cors.allowed-origins` | `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` |
| `app.pagination.default-page-size` | `PAGINATION_DEFAULT_PAGE_SIZE` | `20` |
| `app.pagination.max-page-size` | `PAGINATION_MAX_PAGE_SIZE` | `100` |

---

## 📊 **What Changed?**

### **Before (Hardcoded)**
```java
// CorsConfig.java - Hardcoded values
.allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
.allowedHeaders("*")
.maxAge(3600);

// CacheConfig.java - Hardcoded values
Caffeine.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .expireAfterAccess(5, TimeUnit.MINUTES)
```

### **After (Property-Based)**
```yaml
# application.yml - All in one place
app:
  cors:
    allowed-methods: GET,POST,PATCH,DELETE,OPTIONS
    allowed-headers: "*"
    max-age: 3600

spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m,expireAfterAccess=5m
```

---

## ✅ **Benefits**

1. **Centralized Configuration**
   - All settings in one file
   - Easy to understand and maintain

2. **Environment-Specific Overrides**
   - Dev, staging, prod use same code
   - Only change environment variables

3. **No Code Changes for Config**
   - Change settings without recompiling
   - Update via config files or env vars

4. **Spring Boot Best Practices**
   - Uses Spring Boot's auto-configuration
   - Follows 12-factor app principles

5. **Easier Testing**
   - Override properties in test files
   - No need to mock configuration beans

---

## 🔄 **Configuration Flow**

```
application.yml
      ↓
Environment Variables (override defaults)
      ↓
Spring Boot Auto-Configuration
      ↓
@ConfigurationProperties Classes
      ↓
Injected into Components
```

---

## 📝 **Configuration Files Summary**

### **Java Classes (Minimal Code)**

**CacheConfig.java**
- Just `@EnableCaching` annotation
- All settings from `spring.cache.caffeine.spec`

**CorsConfig.java**
- Reads from `CorsProperties`
- All values from `app.cors.*`

**OpenApiConfig.java**
- Security scheme (must be annotation)
- Info loaded from `springdoc.info.*`

**Property Classes**
- `CorsProperties` - Binds `app.cors.*`
- `SecurityProperties` - Binds `security.*`
- `RateLimitingProperties` - Binds `ratelimit.*`
- `PaginationProperties` - Binds `app.pagination.*`

---

## 🎯 **How to Change Configuration**

### **Development**
Edit `application.yml` directly

### **Production**
Set environment variables:
```bash
export SECURITY_API_KEY="strong-production-key"
export CORS_ALLOWED_ORIGINS="https://app.example.com"
export RATE_LIMIT_REQUESTS_PER_MINUTE=120
```

### **Docker**
```yaml
# docker-compose.yml
environment:
  SECURITY_API_KEY: ${SECURITY_API_KEY}
  CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
```

### **Kubernetes**
```yaml
# ConfigMap or Secret
env:
  - name: SECURITY_API_KEY
    valueFrom:
      secretKeyRef:
        name: app-secrets
        key: api-key
```

---

## 📚 **Property Profiles**

Create environment-specific files:

```
application.yml          # Default
application-dev.yml      # Development
application-staging.yml  # Staging
application-prod.yml     # Production
```

Activate with:
```bash
java -jar app.jar --spring.profiles.active=prod
```

Or via environment:
```bash
export SPRING_PROFILES_ACTIVE=prod
```

---

## ✅ **Summary**

**Before**: 7 configuration classes with hardcoded values  
**After**: 1 property file + 7 minimal binding classes

**Benefits**:
- ✅ All config in `application.yml`
- ✅ Easy environment overrides
- ✅ No code changes for config updates
- ✅ Spring Boot best practices
- ✅ Cleaner, more maintainable code

**Configuration is now property-driven and production-ready!** 🚀

