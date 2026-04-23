# Postman Testing Guide - Flint & Thread API

## Base URL
```
http://localhost:8080
```

## Authentication
Most endpoints are public (no authentication required). For protected endpoints, use JWT token from OTP authentication.

---

## Product Filtering Endpoints

### 1. Get All Products (Basic)
```
GET /api/products
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `sort` (optional): Sort field and direction

**Example:**
```
GET http://localhost:8080/api/products?page=0&size=20&sort=createdAt,desc
```

---

### 2. Simple Filter Endpoint (NEW)
```
GET /api/products/filter-products
```

**Query Parameters:**
- `categoryId` (optional): Filter by category ID
- `gender` (optional): Filter by gender (men, women, unisex)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Examples:**

**Men's Category Products:**
```
GET http://localhost:8080/api/products/filter-products?categoryId=28&gender=men&page=0&size=20
```

**All Men's Products:**
```
GET http://localhost:8080/api/products/filter-products?gender=men&page=0&size=20
```

**Category Products Only:**
```
GET http://localhost:8080/api/products/filter-products?categoryId=28&page=0&size=20
```

---

### 3. Gender-Specific Endpoint (NEW)
```
GET /api/products/gender/{gender}
```

**Path Parameters:**
- `gender`: Gender value (men, women, unisex)

**Query Parameters:**
- `categoryId` (optional): Filter by category ID
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sortBy` (optional): Sort field (default: createdAt)
- `sortDirection` (optional): Sort direction (default: desc)

**Examples:**

**Men's Products:**
```
GET http://localhost:8080/api/products/gender/men?page=0&size=20
```

**Men's Category Products:**
```
GET http://localhost:8080/api/products/gender/men?categoryId=28&page=0&size=20&sortBy=price&sortDirection=asc
```

**Women's Products by Price:**
```
GET http://localhost:8080/api/products/gender/women?sortBy=sellingPrice&sortDirection=desc&page=0&size=20
```

---

### 4. Advanced Filter Endpoint (NEW)
```
POST /api/products/search/filter
```

**Headers:**
```
Content-Type: application/json
```

**Body Examples:**

**Basic Men's Filter:**
```json
{
  "genders": ["men"],
  "categoryId": 28,
  "page": 0,
  "size": 20,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

**Complex Filter with Price Range:**
```json
{
  "genders": ["men"],
  "categoryId": 28,
  "minPrice": 500,
  "maxPrice": 2000,
  "colors": ["blue", "black"],
  "sizes": ["M", "L"],
  "inStock": true,
  "page": 0,
  "size": 20,
  "sortBy": "sellingPrice",
  "sortDirection": "asc"
}
```

**Search with Gender:**
```json
{
  "keyword": "shirt",
  "genders": ["men"],
  "minPrice": 300,
  "maxPrice": 1500,
  "page": 0,
  "size": 20,
  "sortBy": "name",
  "sortDirection": "asc"
}
```

---

### 5. Category-Based Endpoints

**Products by Category:**
```
GET /api/products/category/{categoryId}
```
**Example:**
```
GET http://localhost:8080/api/products/category/28?page=0&size=20
```

**Products by Main Category:**
```
GET /api/products/main-category/{mainCategoryId}
```
**Example:**
```
GET http://localhost:8080/api/products/main-category/28
```

**Products by Subcategory:**
```
GET /api/products/subcategory/{subcategoryId}
```
**Example:**
```
GET http://localhost:8080/api/products/subcategory/15
```

---

### 6. Other Product Endpoints

**Search Products:**
```
GET /api/products/search?q={keyword}
```
**Example:**
```
GET http://localhost:8080/api/products/search?q=shirt
```

**Get Product by ID:**
```
GET /api/products/{productId}
```
**Example:**
```
GET http://localhost:8080/api/products/123
```

**Recent Products:**
```
GET /api/products/recent
```

**Popular Products:**
```
GET /api/products/popular
```

**Trending Products:**
```
GET /api/products/trending
```

---

## Wishlist Endpoints

### 1. Get User Wishlist
```
GET /api/wishlist/user/{userId}
```
**Example:**
```
GET http://localhost:8080/api/wishlist/user/123
```

**Response Format (Enhanced with Image URLs):**
```json
[
  {
    "wishlistId": 456,
    "productId": 789,
    "productName": "Men's Premium Shirt",
    "image": "https://flintnthread.in/uploads/products/shirt1.jpg",
    "images": [
      {
        "id": 1,
        "productId": 789,
        "imagePath": "/uploads/products/shirt1.jpg",
        "imageUrl": "https://flintnthread.in/uploads/products/shirt1.jpg",
        "isPrimary": true,
        "sortOrder": 1
      },
      {
        "id": 2,
        "productId": 789,
        "imagePath": "/uploads/products/shirt2.jpg",
        "imageUrl": "https://flintnthread.in/uploads/products/shirt2.jpg",
        "isPrimary": false,
        "sortOrder": 2
      }
    ],
    "size": "M",
    "color": "blue",
    "inStock": true,
    "sellingPrice": 999.00,
    "mrpPrice": 1299.00,
    "addedAt": "2026-04-20T10:30:00"
  }
]
```

---

### 2. Add to Wishlist
```
POST /api/wishlist/add
```

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "userId": 123,
  "productId": 789
}
```

---

### 3. Remove from Wishlist
```
DELETE /api/wishlist/remove
```

**Query Parameters:**
- `userId`: User ID
- `productId`: Product ID

**Example:**
```
DELETE http://localhost:8080/api/wishlist/remove?userId=123&productId=789
```

---

### 4. Check Product in Wishlist
```
GET /api/wishlist/check
```

**Query Parameters:**
- `userId`: User ID
- `productId`: Product ID

**Example:**
```
GET http://localhost:8080/api/wishlist/check?userId=123&productId=789
```

**Response:**
```json
true  // or false
```

---

## Category Endpoints

### 1. Get Main Categories
```
GET /api/categories/main
```

### 2. Get Category Tree
```
GET /api/categories/tree
```

### 3. Get Category by ID
```
GET /api/categories/{categoryId}
```
**Example:**
```
GET http://localhost:8080/api/categories/28
```

### 4. Get Subcategories by Category
```
GET /api/categories/{categoryId}/subcategories
```
**Example:**
```
GET http://localhost:8080/api/categories/28/subcategories
```

---

## Authentication Endpoints (If Needed)

### 1. Send OTP
```
POST /auth/send-otp
```

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "mobile": "9999999991",
  "countryCode": "+91"
}
```

### 2. Verify OTP & Get Token
```
POST /auth/verify-otp
```

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "mobile": "9999999991",
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

---

## Postman Collection Setup

### Environment Variables
```
base_url: http://localhost:8080
user_id: 123
category_id: 28
product_id: 789
```

### Sample Collection Structure

#### 1. Products Folder
- Get All Products
- Filter Products (Simple)
- Filter Products (Advanced)
- Gender Filter
- Category Filter
- Search Products

#### 2. Wishlist Folder
- Get User Wishlist
- Add to Wishlist
- Remove from Wishlist
- Check in Wishlist

#### 3. Categories Folder
- Get Main Categories
- Get Category Tree
- Get Subcategories

---

## Testing Scenarios

### Scenario 1: Home Page - Men's Section
1. **Get Men's Products:**
   ```
   GET http://localhost:8080/api/products/gender/men?page=0&size=20
   ```

2. **Get Men's Category Products:**
   ```
   GET http://localhost:8080/api/products/gender/men?categoryId=28&page=0&size=20
   ```

3. **Advanced Men's Filter:**
   ```
   POST http://localhost:8080/api/products/search/filter
   {
     "genders": ["men"],
     "categoryId": 28,
     "minPrice": 500,
     "maxPrice": 2000,
     "page": 0,
     "size": 20
   }
   ```

### Scenario 2: Wishlist Management
1. **Add Product to Wishlist:**
   ```
   POST http://localhost:8080/api/wishlist/add
   {
     "userId": 123,
     "productId": 789
   }
   ```

2. **Get User Wishlist with Images:**
   ```
   GET http://localhost:8080/api/wishlist/user/123
   ```

3. **Remove from Wishlist:**
   ```
   DELETE http://localhost:8080/api/wishlist/remove?userId=123&productId=789
   ```

### Scenario 3: Category Browsing
1. **Get Main Categories:**
   ```
   GET http://localhost:8080/api/categories/main
   ```

2. **Get Category Products:**
   ```
   GET http://localhost:8080/api/products/category/28?page=0&size=20
   ```

3. **Get Subcategories:**
   ```
   GET http://localhost:8080/api/categories/28/subcategories
   ```

---

## Common Response Formats

### Success Response (Products)
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

### Success Response (Single Item)
```json
{
  "id": 789,
  "name": "Product Name",
  "images": [...],
  "variants": [...]
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## Tips for Postman Testing

1. **Use Environment Variables** for base URL and common IDs
2. **Save Responses** to examine the structure
3. **Test Pagination** by changing page and size parameters
4. **Test Sorting** with different sortBy and sortDirection values
5. **Test Filters** by combining multiple parameters
6. **Check Image URLs** in wishlist responses
7. **Verify Error Handling** with invalid parameters

---

## Quick Start URLs

**Test these first:**
1. `GET http://localhost:8080/api/products` - Basic products
2. `GET http://localhost:8080/api/products/gender/men` - Men's products
3. `GET http://localhost:8080/api/wishlist/user/123` - User wishlist
4. `GET http://localhost:8080/api/categories/main` - Categories

These will verify your API is working correctly!
