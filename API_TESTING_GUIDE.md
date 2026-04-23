# API Testing Guide for Flint & Thread Backend

## Authentication Overview

The application uses **JWT Token Authentication** with **OTP-based login**. Most product and category APIs are public and don't require authentication.

## Public APIs (No Authentication Required)

### Product APIs
```
GET /api/products                    - Get all products (paginated)
GET /api/products/{id}              - Get product by ID
GET /api/products/main-category/{mainCategoryId} - Get products by main category ID
GET /api/products/category/{id}     - Get products by category ID
GET /api/products/subcategory/{id}  - Get products by subcategory ID
GET /api/products/recent            - Get recent products
GET /api/products/popular           - Get popular products
GET /api/products/trending          - Get trending products
GET /api/products/search?q={query}  - Search products
```

### Category APIs
```
GET /api/categories/main            - Get main categories
GET /api/categories/tree            - Get category tree
GET /api/categories/{id}            - Get category by ID
GET /api/categories/{id}/subcategories - Get subcategories by parent ID
```

### Other Public APIs
```
GET /api/location/states            - Get states
GET /api/location/cities/{stateId}  - Get cities by state
GET /api/location/pincodes/{cityId} - Get pincodes by city
```

## Authentication Required APIs

For protected endpoints, you need to obtain a JWT token using OTP authentication.

### Step 1: Send OTP
```bash
POST /auth/send-otp
Content-Type: application/json

{
    "mobile": "9999999999",  // or "email": "test@example.com"
    "countryCode": "+91"
}
```

### Step 2: Verify OTP & Get JWT Token
```bash
POST /auth/verify-otp
Content-Type: application/json

{
    "mobile": "9999999999",
    "countryCode": "+91",
    "otp": "123456"
}
```

**Response:**
```json
{
    "success": true,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "role": "USER"
}
```

### Step 3: Use JWT Token in Protected APIs
```bash
GET /api/account/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Test Credentials

### Development/Test Users
You can create test users in the database with these mobile numbers for testing:

```sql
-- Insert test users (if needed)
INSERT INTO users (mobile, email, name, role, status, created_at) VALUES
('9999999991', 'test1@example.com', 'Test User 1', 'USER', 1, NOW()),
('9999999992', 'test2@example.com', 'Test User 2', 'USER', 1, NOW()),
('9999999993', 'admin@example.com', 'Admin User', 'ADMIN', 1, NOW());
```

### Default OTP for Testing
In development mode, you can often use `123456` as the default OTP for testing.

## API Testing Examples

### Test Your New Main Category Endpoint
```bash
# Test without authentication (public endpoint)
curl -X GET "http://localhost:8080/api/products/main-category/28" \
     -H "Content-Type: application/json"

# PowerShell version
Invoke-WebRequest -Uri "http://localhost:8080/api/products/main-category/28" -Method GET
```

### Test Protected Endpoint with Authentication
```bash
# 1. Send OTP
curl -X POST "http://localhost:8080/auth/send-otp" \
     -H "Content-Type: application/json" \
     -d '{"mobile": "9999999991", "countryCode": "+91"}'

# 2. Verify OTP (assuming OTP is 123456)
curl -X POST "http://localhost:8080/auth/verify-otp" \
     -H "Content-Type: application/json" \
     -d '{"mobile": "9999999991", "countryCode": "+91", "otp": "123456"}'

# 3. Use token to access protected API
curl -X GET "http://localhost:8080/api/account/profile" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Postman Collection Setup

### Environment Variables
```
base_url: http://localhost:8080
jwt_token: {{auth_response.token}}
```

### Sample Requests

1. **Get Products by Main Category**
   - Method: GET
   - URL: `{{base_url}}/api/products/main-category/28`
   - Headers: Content-Type: application/json

2. **Authentication Flow**
   - Send OTP: POST `{{base_url}}/auth/send-otp`
   - Verify OTP: POST `{{base_url}}/auth/verify-otp`
   - Save token to environment variable

## Important Notes

1. **Product APIs are public** - You don't need authentication for the main category endpoint
2. **JWT Token expires** - Tokens have an expiration time, you'll need to re-authenticate
3. **OTP delivery** - In development, OTP might be logged in console or use default values
4. **Database connection** - Ensure MySQL is running with the database configured in `application.properties`

## Troubleshooting

### 500 Internal Server Error
- Check application logs for SQL errors
- Verify database connection
- Ensure the category ID exists in the database

### 404 Not Found
- Verify the application is running on port 8080
- Check the endpoint URL is correct

### 401 Unauthorized
- Ensure JWT token is valid and not expired
- Check token is properly formatted in Authorization header

### 403 Forbidden
- Check user role has permission for the endpoint
- Some endpoints require ADMIN role

## Database Credentials (for reference)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shopping
spring.datasource.username=root
spring.datasource.password=Sravani@1234
```
