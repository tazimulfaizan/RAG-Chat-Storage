# 🚦 Rate Limiting Implementation Guide

## 🎯 **Best Approaches for Rate Limiting**

Since we removed the custom `Bucket4jRateLimitingFilter`, here are production-ready alternatives ranked by recommendation:

---

## ⭐ **Option 1: API Gateway Rate Limiting (RECOMMENDED for Production)**

### **Best for:** Production deployments, microservices, cloud environments

### **Why Best?**
✅ **Centralized** - One place to manage rate limiting for all services  
✅ **Scalable** - Works across multiple application instances  
✅ **No Code Changes** - Configure at infrastructure level  
✅ **DDoS Protection** - Built-in security features  
✅ **Professional** - Industry standard approach  

### **Implementation Options:**

#### **A. AWS API Gateway**

**Configuration:**
```yaml
# serverless.yml or CloudFormation
ApiGateway:
  UsagePlan:
    Throttle:
      BurstLimit: 100      # Max concurrent requests
      RateLimit: 60        # Requests per second
    Quota:
      Limit: 100000        # Monthly quota
      Period: MONTH
```

**Via AWS Console:**
1. Go to API Gateway → Usage Plans
2. Create Usage Plan
3. Set throttle: 60 requests/second
4. Set burst: 100 requests
5. Associate with API stage

**Cost:** ~$3.50 per million requests

#### **B. Kong API Gateway**

**Installation (Docker):**
```yaml
# docker-compose.yml (add to your existing file)
services:
  kong:
    image: kong:latest
    environment:
      KONG_DATABASE: postgres
      KONG_PG_HOST: postgres
      KONG_PROXY_ACCESS_LOG: /dev/stdout
      KONG_ADMIN_ACCESS_LOG: /dev/stdout
    ports:
      - "8000:8000"  # Proxy
      - "8001:8001"  # Admin API
    depends_on:
      - postgres
  
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: kong
      POSTGRES_USER: kong
      POSTGRES_PASSWORD: kong
```

**Rate Limiting Plugin:**
```bash
# Enable rate limiting
curl -X POST http://localhost:8001/plugins \
  --data "name=rate-limiting" \
  --data "config.minute=60" \
  --data "config.hour=3600" \
  --data "config.policy=local"
```

**Configuration:**
```yaml
plugins:
  - name: rate-limiting
    config:
      minute: 60
      hour: 3600
      day: 100000
      policy: redis  # Use Redis for distributed
      fault_tolerant: true
      hide_client_headers: false
```

**Cost:** Free (open source)

#### **C. Nginx Rate Limiting**

**Configuration:**
```nginx
# /etc/nginx/nginx.conf

http {
    # Define rate limit zone
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=60r/m;
    
    server {
        listen 80;
        server_name api.yourdomain.com;
        
        location /api/ {
            # Apply rate limiting
            limit_req zone=api_limit burst=10 nodelay;
            
            # Return 429 on rate limit
            limit_req_status 429;
            
            # Proxy to your app
            proxy_pass http://localhost:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
        
        location /health {
            # No rate limiting for health checks
            proxy_pass http://localhost:8080;
        }
    }
}
```

**Docker Nginx:**
```yaml
# docker-compose.yml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - app
  
  app:
    # Your Spring Boot app
    build: .
    expose:
      - "8080"
```

**Cost:** Free

#### **D. Cloudflare**

**Configuration:**
1. Add your domain to Cloudflare
2. Go to Security → WAF → Rate Limiting Rules
3. Create rule:
   - Path: `/api/*`
   - Rate: 60 requests per minute
   - Action: Block for 1 minute

**Cost:** Free tier available, Pro $20/month

---

## ⭐ **Option 2: Spring Cloud Gateway (RECOMMENDED for Microservices)**

### **Best for:** Spring Boot microservices architecture, distributed systems

### **Implementation:**

#### **Step 1: Add Dependencies**

```gradle
// build.gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway-mvc'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
}

// Add Spring Cloud BOM
ext {
    set('springCloudVersion', "2023.0.0")
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}
```

#### **Step 2: Configure in application.yml**

```yaml
spring:
  cloud:
    gateway:
      mvc:
        routes:
          - id: api_route
            uri: http://localhost:8080
            predicates:
              - Path=/api/**
            filters:
              - name: RequestRateLimiter
                args:
                  redis-rate-limiter.replenishRate: 60      # Tokens per second
                  redis-rate-limiter.burstCapacity: 100     # Max bucket size
                  redis-rate-limiter.requestedTokens: 1     # Tokens per request
                  key-resolver: "#{@userKeyResolver}"
  
  # Redis Configuration
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

#### **Step 3: Create Key Resolver**

```java
// src/main/java/com/example/ragchatstorage/config/RateLimitConfig.java
package com.example.ragchatstorage.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Rate limit by API key
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-KEY");
            if (apiKey != null) {
                return Mono.just(apiKey);
            }
            // Fallback to IP address
            return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        };
    }
}
```

#### **Step 4: Add Redis to Docker Compose**

```yaml
# docker-compose.yml
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes

volumes:
  redis-data:
```

**Advantages:**
- ✅ Distributed rate limiting (Redis-backed)
- ✅ Spring-native solution
- ✅ Works across multiple instances
- ✅ Highly configurable

---

## ⭐ **Option 3: Re-add Bucket4j (Simple, In-Memory)**

### **Best for:** Single-instance deployments, development, simple use cases

### **Implementation:**

#### **Step 1: Add Dependency**

```gradle
// build.gradle
dependencies {
    implementation 'com.bucket4j:bucket4j-core:8.10.1'
}
```

#### **Step 2: Create Filter**

```java
// src/main/java/com/example/ragchatstorage/filter/RateLimitFilter.java
package com.example.ragchatstorage.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String key = getClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Too many requests\"}");
        }
    }

    private String getClientKey(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-KEY");
        return apiKey != null ? apiKey : request.getRemoteAddr();
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillIntervally(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
```

#### **Step 3: Configure in application.yml**

```yaml
app:
  rate-limit:
    enabled: true
    requests-per-minute: ${RATE_LIMIT_REQUESTS_PER_MINUTE:60}
```

**Advantages:**
- ✅ Simple implementation
- ✅ No external dependencies
- ✅ Good for development/testing

**Disadvantages:**
- ❌ In-memory only (doesn't work across multiple instances)
- ❌ Lost on restart

---

## ⭐ **Option 4: Bucket4j with Redis (Distributed)**

### **Best for:** Multiple application instances with shared rate limit

### **Implementation:**

#### **Step 1: Add Dependencies**

```gradle
// build.gradle
dependencies {
    implementation 'com.bucket4j:bucket4j-core:8.10.1'
    implementation 'com.bucket4j:bucket4j-redis:8.10.1'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.redisson:redisson-spring-boot-starter:3.24.3'
}
```

#### **Step 2: Configure Redis**

```yaml
# application.yml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

app:
  rate-limit:
    requests-per-minute: 60
```

#### **Step 3: Create Rate Limit Service**

```java
// src/main/java/com/example/ragchatstorage/config/RateLimitService.java
package com.example.ragchatstorage.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RateLimitService {

    private final ProxyManager<String> proxyManager;
    
    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    public RateLimitService(RedissonClient redissonClient) {
        this.proxyManager = RedissonBasedProxyManager.builderFor(redissonClient).build();
    }

    public boolean allowRequest(String key) {
        Supplier<BucketConfiguration> configSupplier = () -> {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(requestsPerMinute)
                    .refillIntervally(requestsPerMinute, Duration.ofMinutes(1))
                    .build();
            
            return BucketConfiguration.builder()
                    .addLimit(limit)
                    .build();
        };

        Bucket bucket = proxyManager.builder().build(key, configSupplier);
        return bucket.tryConsume(1);
    }
}
```

**Advantages:**
- ✅ Distributed rate limiting
- ✅ Works across multiple instances
- ✅ Persistent (survives restarts)
- ✅ Highly scalable

---

## 📊 **Comparison Table**

| Solution | Complexity | Cost | Scalability | Distributed | Best For |
|----------|-----------|------|-------------|-------------|----------|
| **AWS API Gateway** | Low | $$$ | ⭐⭐⭐⭐⭐ | ✅ Yes | Production, AWS |
| **Kong Gateway** | Medium | Free | ⭐⭐⭐⭐⭐ | ✅ Yes | Production, Any Cloud |
| **Nginx** | Low | Free | ⭐⭐⭐⭐ | ✅ Yes* | Production, Simple |
| **Cloudflare** | Very Low | $ | ⭐⭐⭐⭐⭐ | ✅ Yes | Production, CDN |
| **Spring Cloud Gateway** | Medium | Free | ⭐⭐⭐⭐⭐ | ✅ Yes (Redis) | Microservices |
| **Bucket4j (In-Memory)** | Low | Free | ⭐⭐ | ❌ No | Development |
| **Bucket4j (Redis)** | Medium | Free | ⭐⭐⭐⭐ | ✅ Yes | Multi-instance |

*Nginx with Redis backend

---

## 🎯 **Recommendation by Use Case**

### **For Your RAG Chat Storage Service:**

#### **1. Development/Testing**
```yaml
# Quick Start - Add to application.yml
app:
  rate-limit:
    enabled: true
    requests-per-minute: 60
```
Use: **Bucket4j (In-Memory)** - Simple, no dependencies

#### **2. Single Instance Production**
Use: **Nginx** - Simple, reliable, free

#### **3. Multiple Instances**
Use: **Spring Cloud Gateway + Redis** - Spring-native, distributed

#### **4. Microservices Architecture**
Use: **Kong API Gateway** - Feature-rich, scales well

#### **5. AWS Deployment**
Use: **AWS API Gateway** - Managed, no maintenance

---

## 🚀 **Quick Start: Nginx Rate Limiting (Recommended)**

Since you're using Docker Compose, here's the simplest production-ready solution:

### **Step 1: Create nginx.conf**

```nginx
# nginx.conf
events {
    worker_connections 1024;
}

http {
    # Rate limiting zone: 10MB can track ~160k IPs
    limit_req_zone $binary_remote_addr zone=api_zone:10m rate=60r/m;
    
    # Custom error response for rate limiting
    limit_req_status 429;
    
    upstream backend {
        server app:8080;
    }
    
    server {
        listen 80;
        
        # Health check - no rate limit
        location /health {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
        
        # Actuator - no rate limit
        location /actuator/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
        
        # Swagger - no rate limit
        location /swagger-ui/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
        }
        
        # API endpoints - rate limited
        location /api/ {
            # 60 requests per minute, burst of 10
            limit_req zone=api_zone burst=10 nodelay;
            
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### **Step 2: Update docker-compose.yml**

```yaml
# docker-compose.yml
version: '3.8'

services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - app
    networks:
      - app-network

  app:
    build: ..
    expose:
      - "8080"
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://mongodb:27017/rag-chat-storage
      SECURITY_API_KEY: ${SECURITY_API_KEY:-changeme}
    depends_on:
      - mongodb
    networks:
      - app-network

  mongodb:
    image: mongo:7
    ports:
      - "27017:27017"
    volumes:
      - mongodb-data:/data/db
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  mongodb-data:
```

### **Step 3: Start Services**

```bash
docker-compose up
```

### **Step 4: Test Rate Limiting**

```bash
# Make rapid requests
for i in {1..65}; do
  echo "Request $i:"
  curl -w " - Status: %{http_code}\n" \
    -H "X-API-KEY: changeme" \
    http://localhost/api/v1/sessions?userId=test
  sleep 0.5
done
```

**Expected:**
- First 60 requests: ✅ 200 OK
- Next 5 requests: ❌ 429 Too Many Requests

---

## ✅ **Summary**

### **Recommended Approach:**

**For immediate use:** Add **Nginx** to your Docker Compose (5 minutes setup)

**For production:** Use **API Gateway** (AWS/Kong/Cloudflare) or **Spring Cloud Gateway with Redis**

### **Why Not Custom Filter?**

✅ **API Gateway handles it better** - Centralized, scalable  
✅ **Less code to maintain** - Infrastructure vs application concern  
✅ **Industry standard** - Proven approach  
✅ **Better performance** - Optimized for this purpose  

### **Files to Create:**

1. `nginx.conf` - Nginx configuration with rate limiting
2. Update `docker-compose.yml` - Add Nginx service

**That's it! No code changes to your Spring Boot app needed.** 🎉

---

## 📚 **Additional Resources**

- [Nginx Rate Limiting](http://nginx.org/en/docs/http/ngx_http_limit_req_module.html)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Bucket4j Documentation](https://bucket4j.com/)
- [Kong Rate Limiting](https://docs.konghq.com/hub/kong-inc/rate-limiting/)

---

**Choose the solution that fits your deployment model!** 🚀

