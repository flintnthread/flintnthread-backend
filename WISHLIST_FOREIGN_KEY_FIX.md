# Foreign Key Constraint Fix - Wishlist

## Issue Fixed
**Error:** Foreign key constraint fails when adding product to wishlist
**Cause:** Trying to insert product_id that doesn't exist in products table
**Solution:** Added product existence validation before adding to wishlist

## Changes Made

### Updated addToWishlist Method
```java
@Override
public WishlistResponse addToWishlist(Long userId, Long productId) {

    // Check if product exists before adding to wishlist
    Product product = productService.getProduct(productId);
    if (product == null) {
        throw new RuntimeException("Product not found with ID: " + productId);
    }

    Wishlist wishlist = wishlistRepository
            .findByUserIdAndProductId(userId, productId)
            .orElseGet(() -> {
                Wishlist w = new Wishlist();
                
                // SET USER
                User user = new User();
                user.setId(userId);
                w.setUser(user);

                // SET PRODUCT (using actual product found)
                w.setProduct(product);

                return wishlistRepository.save(w);
            });

    return buildResponse(wishlist);
}
```

## How It Works

### Before Fix:
```java
// Created new Product with only ID (could be invalid)
Product product = new Product();
product.setId(productId);
w.setProduct(product); // Foreign key constraint if ID doesn't exist
```

### After Fix:
```java
// Fetch actual product from database
Product product = productService.getProduct(productId);
if (product == null) {
    throw new RuntimeException("Product not found with ID: " + productId);
}
w.setProduct(product); // Guaranteed to exist
```

## Testing

### 1. Test with Valid Product ID
```bash
# First check what products exist
curl -X GET "http://localhost:8080/api/products" | python -m json.tool

# Use a valid product ID from the response
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 1}'
```

### 2. Test with Invalid Product ID
```bash
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 999999}'
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Product not found with ID: 999999",
  "data": null
}
```

### 3. Test with Valid Product ID
```bash
# After finding valid product ID
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 1}'
```

**Expected Response:**
```json
{
  "wishlistId": 456,
  "productId": 1,
  "productName": "Actual Product Name",
  "image": "https://flintnthread.in/uploads/products/product1.jpg",
  "imageUrl": "https://flintnthread.in/uploads/products/product1.jpg",
  "images": [...],
  "size": "M",
  "color": "blue",
  "inStock": true,
  "sellingPrice": 999.00,
  "mrpPrice": 1299.00,
  "addedAt": "2026-04-20T11:30:00"
}
```

## Finding Valid Product IDs

### Step 1: Get Available Products
```bash
curl -X GET "http://localhost:8080/api/products?page=0&size=10" | python -m json.tool
```

### Step 2: Extract Product IDs
Look for `id` field in the response:
```json
{
  "content": [
    {
      "id": 1,  // Use this ID
      "name": "Product Name",
      "status": "active"
    },
    {
      "id": 2,  // Or this ID
      "name": "Another Product",
      "status": "active"
    }
  ]
}
```

### Step 3: Add to Wishlist
```bash
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "productId": 1}'
```

## Error Messages

### Product Not Found
```json
{
  "success": false,
  "message": "Product not found with ID: 999999",
  "data": null
}
```

### Success
```json
{
  "wishlistId": 456,
  "productId": 1,
  "productName": "Product Name",
  "image": "https://flintnthread.in/uploads/products/product1.jpg",
  "imageUrl": "https://flintnthread.in/uploads/products/product1.jpg",
  "images": [...]
}
```

## Quick Test Commands

### Find Valid Product First
```bash
# Get first product
curl -X GET "http://localhost:8080/api/products?page=0&size=1" | python -m json.tool

# Extract ID and use it
PRODUCT_ID=1

# Add to wishlist
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 123, \"productId\": $PRODUCT_ID}"
```

### Test User Wishlist
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/123" | python -m json.tool
```

## Benefits

### 1. Data Integrity
- Prevents invalid product IDs in wishlist
- Maintains foreign key constraints
- Ensures wishlist only contains existing products

### 2. Better Error Handling
- Clear error messages for invalid products
- Prevents database constraint errors
- Improves user experience

### 3. Consistent Data
- Always uses actual product data
- Guaranteed product information accuracy
- Prevents orphaned wishlist items

## Summary

The foreign key constraint error is now fixed by:
1. **Validating product existence** before adding to wishlist
2. **Using actual product entity** instead of placeholder
3. **Providing clear error messages** for invalid products
4. **Maintaining data integrity** in the database

Test with valid product IDs from your existing products!
