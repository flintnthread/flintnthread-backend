# Application Not Responding - Complete Troubleshooting Guide

## Issue: Application Not Responding to Any Requests

### Symptoms
- No response from any endpoint
- Connection timeout or connection refused
- Application appears to not be running

---

## Step-by-Step Troubleshooting

### Step 1: Check if Application is Running

#### Check Port 8080
```bash
# Windows
netstat -ano | findstr :8080

# Alternative
netstat -tulpn | grep :8080

# PowerShell
Get-NetTCPPort -LocalPort 8080
```

#### Expected Results
- **Running:** Should show Java process on port 8080
- **Not Running:** No output or connection refused

#### If Not Running:
```bash
cd "d:\App development\backendAPI\flintnthread-backend"
./mvnw spring-boot:run
```

### Step 2: Check Application Logs

#### Look for These Messages:
```
- Application startup completed
- Database connection established
- Server started on port 8080
- No errors during startup
- Mapping endpoints registered
```

#### Common Error Messages:
```
- Database connection failed
- Port 8080 already in use
- Application failed to start
- Bean creation errors
```

### Step 3: Test Basic Connectivity

#### Test with curl
```bash
# Basic connectivity test
curl -v http://localhost:8080/api/products

# With timeout
curl --connect-timeout 10 http://localhost:8080/api/products

# Test different endpoint
curl -v http://localhost:8080/api/products/recent
```

#### Test with PowerShell
```powershell
# PowerShell version
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method GET
    $response | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
```

### Step 4: Check Database Connection

#### Verify Database is Running
```sql
-- MySQL
SHOW DATABASES;
USE shopping;
SHOW TABLES;

-- Check connection
SELECT 1;
```

#### Check Application Properties
```properties
# Verify these settings in application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/shopping
spring.datasource.username=root
spring.datasource.password=Sravani@1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### Step 5: Check for Port Conflicts

#### Kill Process on Port 8080
```bash
# Windows
tasklist | findstr java
netstat -ano | findstr :8080

# Kill process
taskkill /F /PID <PID>

# Alternative port
# Change application.properties to use different port
server.port=8081
```

---

## Quick Fix Commands

### 1. Restart Everything
```bash
# Kill all Java processes
taskkill /F /IM java.exe

# Start fresh
cd "d:\App development\backendAPI\flintnthread-backend"
./mvnw clean spring-boot:run
```

### 2. Test with Different Port
```bash
# Start on port 8081
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# Test
curl http://localhost:8081/api/products
```

### 3. Check Application Status
```bash
# Check if Spring Boot is running
curl http://localhost:8080/actuator/health

# Should return:
{
  "status": "UP"
}
```

---

## Common Issues & Solutions

### Issue 1: Port Already in Use
**Symptoms:**
- Application fails to start
- "Port 8080 was already in use" error

**Solutions:**
```bash
# Find and kill process
netstat -ano | findstr :8080
taskkill /F /PID <PID>

# Or use different port
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Issue 2: Database Connection Failed
**Symptoms:**
- Application starts but can't connect to database
- "Connection refused" errors

**Solutions:**
```bash
# Start MySQL service
net start mysql

# Check credentials
mysql -u root -p -e "SHOW DATABASES;"

# Test connection
mysql -u root -p -e "USE shopping; SELECT 1;"
```

### Issue 3: Application Compilation Errors
**Symptoms:**
- Application won't start
- Compilation errors in console

**Solutions:**
```bash
# Clean and recompile
./mvnw clean compile

# Check specific errors
./mvnw compile 2>&1 | grep error
```

### Issue 4: Memory Issues
**Symptoms:**
- Application starts but becomes unresponsive
- OutOfMemoryError

**Solutions:**
```bash
# Increase memory
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx1024m -Xms512m"

# Or in application.properties
server.tomcat.max-threads=200
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

---

## Testing After Fix

### Test These Endpoints in Order:
```bash
# 1. Basic connectivity
curl http://localhost:8080/api/products

# 2. Simple endpoint
curl http://localhost:8080/api/products/recent

# 3. Filter endpoint
curl http://localhost:8080/api/products/filter-products?page=0&size=10

# 4. Wishlist endpoint
curl http://localhost:8080/api/wishlist/user/123

# 5. Add to wishlist
curl -X POST http://localhost:8080/api/wishlist/add \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 1}'
```

---

## Expected Working Response

### Should Look Like:
```json
{
  "content": [
    {
      "id": 1,
      "name": "Product Name",
      "status": "active"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### NOT Like:
```json
[]
```
```json
{
  "success": false,
  "message": "No static resource api/products",
  "data": null
}
```

---

## Final Steps

### 1. Check Application Status
```bash
# Is it running?
netstat -ano | findstr :8080

# What's in logs?
# Check console output for errors
```

### 2. Restart if Needed
```bash
cd "d:\App development\backendAPI\flintnthread-backend"
./mvnw clean spring-boot:run
```

### 3. Test Incrementally
```bash
# Start with basic endpoint
curl http://localhost:8080/api/products

# Then test wishlist
curl http://localhost:8080/api/wishlist/user/123

# Then test filtering
curl http://localhost:8080/api/products/filter-products?gender=men
```

---

## Tell Me What Happens

After trying these steps, report back:
1. **Does application start?** (Check console output)
2. **What port is it running on?** (netstat results)
3. **Do basic endpoints respond?** (curl results)
4. **Any error messages in console?** (Copy exact error)
5. **Database connection status?** (MySQL connection test)

Based on your specific symptoms, I can provide the exact fix needed!
