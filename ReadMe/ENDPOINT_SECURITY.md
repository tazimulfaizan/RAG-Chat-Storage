# 🔐 Endpoint Security Configuration

## ✅ **SecurityProperties.java DELETED**

All security configuration is now in `application.yml` with `@Value` injection.

---

## 🛡️ **How Endpoint Security Works**

### **Security Filter Chain**

```
Incoming Request
      ↓
1. ApiKeyAuthFilter (Order: 1)
   • Checks API key for /api/** endpoints
   • Reads from: security.api-key in application.yml
   • Header: X-API-KEY (or custom from security.api-key-header)
      ↓
2. Bucket4jRateLimitingFilter (Order: 2)
   • Rate limiting (60 requests/min default)
   • Reads from: ratelimit.* in application.yml
      ↓
3. LoggingFilter (Order: 3)
   • Request/response logging
      ↓
4. Your Controller
   • Business logic
```

---

## 📁 **Where Endpoints Are Secured**

### **File: ApiKeyAuthFilter.java**

```java
@Component
@Order(1)  // First filter in chain
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${security.api-key}")
    private String apiKey;
    
    @Value("${security.api-key-header:X-API-KEY}")
    private String apiKeyHeader;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Protect only /api/** endpoints
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(...) {
        String providedKey = request.getHeader(apiKeyHeader);
        
        if (!apiKey.equals(providedKey)) {
            // Return 401 Unauthorized
            unauthorized(response, "Invalid or missing API key");
            return;
        }
        
        // Continue to next filter
        filterChain.doFilter(request, response);
    }
}
```

**Location:** `src/main/java/com/example/ragchatstorage/filter/ApiKeyAuthFilter.java`

---

## 📋 **Configuration in application.yml**

```yaml
security:
  # API Key for authentication (required)
  api-key: ${SECURITY_API_KEY:changeme}
  
  # HTTP Header name for API key (optional, defaults to X-API-KEY)
  api-key-header: X-API-KEY
```

### **Environment Variable Override:**

```bash
# Production
export SECURITY_API_KEY="your-strong-production-api-key-here"

# Docker
docker run -e SECURITY_API_KEY="prod-key" ...

# docker-compose.yml
environment:
  SECURITY_API_KEY: ${SECURITY_API_KEY}
```

---

## 🔒 **Protected vs Unprotected Endpoints**

### **Protected (Requires API Key)**

All `/api/**` endpoints require the `X-API-KEY` header:

```bash
✅ POST   /api/v1/sessions
✅ GET    /api/v1/sessions
✅ PATCH  /api/v1/sessions/{id}/rename
✅ PATCH  /api/v1/sessions/{id}/favorite
✅ DELETE /api/v1/sessions/{id}
✅ POST   /api/v1/sessions/{id}/messages
✅ GET    /api/v1/sessions/{id}/messages
```

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/v1/sessions?userId=user-123 \
  -H "X-API-KEY: changeme"
```

### **Unprotected (No API Key Required)**

Public endpoints accessible without authentication:

```bash
❌ GET /health
❌ GET /actuator/health
❌ GET /actuator/info
❌ GET /actuator/metrics
❌ GET /actuator/caches
❌ GET /swagger-ui/index.html
❌ GET /v3/api-docs
```

**Example Request:**
```bash
curl http://localhost:8080/health
# No X-API-KEY header needed
```

---

## 🔧 **How to Customize Security**

### **1. Change API Key**

**application.yml:**
```yaml
security:
  api-key: my-custom-key
```

Or via environment:
```bash
export SECURITY_API_KEY="my-custom-key"
```

### **2. Change Header Name**

**application.yml:**
```yaml
security:
  api-key-header: Authorization
```

**Request:**
```bash
curl -H "Authorization: changeme" http://localhost:8080/api/v1/sessions
```

### **3. Protect Additional Paths**

**Edit ApiKeyAuthFilter.java:**
```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    
    // Protect /api/** and /admin/**
    return !path.startsWith("/api/") && !path.startsWith("/admin/");
}
```

### **4. Allow Specific Paths Without Auth**

**Edit ApiKeyAuthFilter.java:**
```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    
    // Allow specific API endpoints without auth
    if (path.equals("/api/v1/public/status")) {
        return true;
    }
    
    return !path.startsWith("/api/");
}
```

### **5. Disable Security (Development Only)**

**application.yml:**
```yaml
security:
  api-key: ""  # Empty key disables security
```

Or remove the filter temporarily:
```java
// Comment out @Component
// @Component
@Order(1)
public class ApiKeyAuthFilter extends OncePerRequestFilter {
```

---

## 🚀 **Advanced Security Options**

### **Option 1: Multiple API Keys (Per User/Client)**

**Update ApiKeyAuthFilter.java:**
```java
@Value("${security.api-keys}")
private List<String> apiKeys;

@Override
protected void doFilterInternal(...) {
    String providedKey = request.getHeader(apiKeyHeader);
    
    if (!apiKeys.contains(providedKey)) {
        unauthorized(response, "Invalid API key");
        return;
    }
    
    filterChain.doFilter(request, response);
}
```

**application.yml:**
```yaml
security:
  api-keys: key1,key2,key3
```

### **Option 2: Spring Security (Full OAuth2/JWT)**

If you need OAuth2, JWT, or role-based security:

**Add dependency:**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

**Create SecurityConfig.java:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        
        return http.build();
    }
}
```

### **Option 3: API Key with Roles**

Store API keys with roles in database:

**application.yml:**
```yaml
security:
  api-keys:
    - key: admin-key-123
      role: ADMIN
    - key: user-key-456
      role: USER
```

**Filter checks role:**
```java
if (apiKey.getRole().equals("ADMIN")) {
    // Allow admin operations
}
```

---

## 📊 **Security Architecture**

```
┌─────────────────────────────────────────────────────────┐
│                    Client Request                       │
│              (curl, browser, mobile app)                │
└─────────────────────────────────────────────────────────┘
                         ↓
         Header: X-API-KEY: changeme
                         ↓
┌─────────────────────────────────────────────────────────┐
│              1. ApiKeyAuthFilter                        │
│  • Checks if path starts with /api/                     │
│  • Validates API key from header                        │
│  • Returns 401 if invalid/missing                       │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│          2. Bucket4jRateLimitingFilter                  │
│  • Rate limiting (60 req/min)                           │
│  • Returns 429 if exceeded                              │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│                3. LoggingFilter                         │
│  • Logs request/response                                │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              4. Your Controller                         │
│  • ChatSessionController                                │
│  • ChatMessageController                                │
└─────────────────────────────────────────────────────────┘
```

---

## 🧪 **Testing Security**

### **Test 1: Valid API Key**
```bash
curl -H "X-API-KEY: changeme" \
  http://localhost:8080/api/v1/sessions?userId=user-123

# Expected: 200 OK with session list
```

### **Test 2: Missing API Key**
```bash
curl http://localhost:8080/api/v1/sessions?userId=user-123

# Expected: 401 Unauthorized
# Response: {"status":401,"error":"Unauthorized","message":"Invalid or missing API key"}
```

### **Test 3: Invalid API Key**
```bash
curl -H "X-API-KEY: wrong-key" \
  http://localhost:8080/api/v1/sessions?userId=user-123

# Expected: 401 Unauthorized
```

### **Test 4: Public Endpoints**
```bash
curl http://localhost:8080/health

# Expected: 200 OK (no API key needed)
```

---

## 📝 **Summary**

### **Where Security is Configured:**

1. **Filter:** `ApiKeyAuthFilter.java` (validates API keys)
2. **Config:** `application.yml` (security.api-key, security.api-key-header)
3. **Protected:** All `/api/**` endpoints
4. **Unprotected:** `/health`, `/actuator/**`, `/swagger-ui/**`

### **How to Change Security:**

- **API Key:** Set `SECURITY_API_KEY` environment variable
- **Header Name:** Change `security.api-key-header` in application.yml
- **Protected Paths:** Edit `shouldNotFilter()` in ApiKeyAuthFilter.java
- **Advanced:** Add Spring Security for OAuth2/JWT

### **No Config Classes Needed:**

✅ All configuration in `application.yml`  
✅ Filter uses `@Value` injection  
✅ No SecurityProperties.java needed  

---

## 🎯 **Quick Reference**

**Secure an endpoint:**
- Put it under `/api/` path
- Client must send `X-API-KEY` header

**Make endpoint public:**
- Don't put it under `/api/` path
- Or modify `shouldNotFilter()` method

**Change API key:**
```bash
export SECURITY_API_KEY="new-key"
```

**Disable security (dev only):**
```yaml
security:
  api-key: ""
```

---

**Your endpoints are secured by ApiKeyAuthFilter.java using configuration from application.yml!** 🔐

