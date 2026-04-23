# Quick Fix for Foreign Key Constraint Error

## Issue Analysis
From logs: Application is running successfully but getting foreign key constraint error when adding to wishlist.

**Error:** `Cannot add or update a child row: a foreign key constraint fails`
**Cause:** Product ID being used doesn't exist in `products` table
**Solution:** Use valid product IDs that exist in database

## Quick Testing Solution

### Step 1: Find Valid Product IDs
```bash
# Get all products to see what IDs exist
curl -X GET "http://localhost:8080/api/products?page=0&size=20" | python -m json.tool
```

**Look for `id` field in response:**
```json
{
  "content": [
    {
      "id": 1,        // VALID ID
      "name": "Product Name",
      "status": "active"
    },
    {
      "id": 2,        // VALID ID
      "name": "Another Product",
      "status": "active"
    }
  ],
  "totalElements": 15,
  "totalPages": 1
}
```

### Step 2: Use Valid Product ID
```bash
# Use ID 1 (or any ID from the response above)
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 1}'
```

### Step 3: Test Wishlist
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/123" | python -m json.tool
```

## Alternative: Check Database Directly

### Check What Products Exist
```sql
-- Check if products table has data
SELECT COUNT(*) FROM products;

-- Check specific product IDs
SELECT id, name, status FROM products WHERE id IN (1, 2, 3, 4, 5);

-- Check if products have images
SELECT p.id, p.name, COUNT(pi.id) as image_count 
FROM products p 
LEFT JOIN product_images pi ON p.id = pi.product_id 
WHERE p.status = 'active' 
GROUP BY p.id, p.name;
```

## If No Products in Database

### Add Test Products
```sql
-- Insert test products
INSERT INTO products (name, description, status, created_at, category_id, subcategory_id) 
VALUES 
('Test Product 1', 'Description 1', 'active', NOW(), 1, 1),
('Test Product 2', 'Description 2', 'active', NOW(), 1, 1),
('Test Product 3', 'Description 3', 'active', NOW(), 1, 1);

-- Insert test images
INSERT INTO product_images (product_id, image_path, is_primary, sort_order, created_at) 
VALUES 
(1, '/uploads/products/test1.jpg', true, 1, NOW()),
(1, '/uploads/products/test2.jpg', false, 2, NOW()),
(2, '/uploads/products/test3.jpg', true, 1, NOW()),
(3, '/uploads/products/test4.jpg', true, 1, NOW());

-- Insert test variants
INSERT INTO product_variants (product_id, color, size, selling_price, mrp_price, stock, created_at) 
VALUES 
(1, 'red', 'M', 999.00, 1299.00, 10, NOW()),
(2, 'blue', 'L', 1499.00, 1999.00, 15, NOW()),
(3, 'green', 'S', 799.00, 999.00, 20, NOW());
```

## Testing Commands

### Test with Product ID 1
```bash
# Step 1: Check product exists
curl -X GET "http://localhost:8080/api/products/1"

# Step 2: Add to wishlist
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 1}'

# Step 3: Get wishlist
curl -X GET "http://localhost:8080/api/wishlist/user/123"
```

### Test with Product ID 2
```bash
# Step 1: Check product exists
curl -X GET "http://localhost:8080/api/products/2"

# Step 2: Add to wishlist
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 2}'

# Step 3: Get wishlist
curl -X GET "http://localhost:8080/api/wishlist/user/123"
```

## Expected Success Response

### Should Look Like:
```json
{
  "wishlistId": 456,
  "productId": 1,
  "productName": "Test Product 1",
  "image": "https://flintnthread.in/uploads/products/test1.jpg",
  "imageUrl": "https://flintnthread.in/uploads/products/test1.jpg",
  "images": [
    {
      "id": 1,
      "productId": 1,
      "imagePath": "/uploads/products/test1.jpg",
      "imageUrl": "https://flintnthread.in/uploads/products/test1.jpg",
      "isPrimary": true,
      "sortOrder": 1
    }
  ],
  "size": "M",
  "color": "red",
  "inStock": true,
  "sellingPrice": 999.00,
  "mrpPrice": 1299.00,
  "addedAt": "2026-04-20T11:30:00"
}
```

## Quick Fix Summary

### The Issue:
- You're using a `productId` that doesn't exist in the database
- Application is working correctly by preventing invalid foreign keys

### The Solution:
1. **Find valid product IDs** using `GET /api/products`
2. **Use those IDs** in wishlist requests
3. **Or add test data** to database if no products exist

### Try These Commands:
```bash
# First, see what products exist
curl -X GET "http://localhost:8080/api/products?page=0&size=10"

# Then use a valid ID from that response
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 1}'
```

The foreign key error is actually working correctly - it's protecting your database from invalid data. You just need to use valid product IDs!
