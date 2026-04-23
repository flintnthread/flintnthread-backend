# Immediate Working Solution for Men's Category Filtering

## Current Issue
The application hasn't been restarted to load new controller mappings, so new endpoints aren't accessible yet.

## Immediate Working Solutions (Using Existing Endpoints)

### Solution 1: Use Category Endpoint + Frontend Filtering
```bash
GET /api/products/category/28?page=0&size=50
```
Then filter by gender in frontend:
```javascript
const response = await fetch('/api/products/category/28?page=0&size=50');
const products = response.content.filter(product => product.gender === 'men');
```

### Solution 2: Use Search Endpoint with Gender Keyword
```bash
GET /api/products/search?q=men&categoryId=28
```

### Solution 3: Use Main Category Endpoint (if 28 is a main category)
```bash
GET /api/products/main-category/28
```

### Solution 4: Get All Products and Filter Multiple Categories
If you know men's category IDs:
```bash
GET /api/products/category/28  // Men's main category
GET /api/products/category/29  // Men's subcategory 1
GET /api/products/category/30  // Men's subcategory 2
```

## Frontend Implementation (Working with Current API)

### React/Vue/Angular Example
```javascript
// Get men's products using existing category endpoint
const getMensProducts = async (categoryId, page = 0) => {
  try {
    // Get all products from category
    const response = await fetch(
      `/api/products/category/${categoryId}?page=${page}&size=50`
    );
    const data = await response.json();
    
    // Filter by gender on frontend
    const mensProducts = data.content.filter(product => 
      product.gender && product.gender.toLowerCase() === 'men'
    );
    
    return {
      ...data,
      content: mensProducts,
      totalElements: mensProducts.length
    };
  } catch (error) {
    console.error('Error fetching men products:', error);
    return { content: [], totalElements: 0 };
  }
};

// Alternative: Search for men's products
const searchMensProducts = async (categoryId, keyword = '') => {
  const response = await fetch(
    `/api/products/search?q=${keyword}&page=0&size=50`
  );
  return response.json();
};
```

### Database Query Alternative
If you have database access, you can create a temporary view:

```sql
-- Create temporary men's category mapping
SELECT p.*, c.category_name 
FROM products p
JOIN categories c ON p.category_id = c.id
WHERE p.gender = 'men' AND c.category_name LIKE '%men%'
ORDER BY p.created_at DESC;
```

## Quick Test Commands

### Test Existing Working Endpoints:
```bash
# Test category endpoint (should work)
curl http://localhost:8080/api/products/category/28

# Test search endpoint (should work)  
curl http://localhost:8080/api/products/search?q=shirt

# Test main category endpoint (should work)
curl http://localhost:8080/api/products/main-category/28
```

## Temporary Fix Summary

Since the application needs restart for new endpoints:

1. **Use existing `/api/products/category/28`** and filter by gender in frontend
2. **Use `/api/products/search?q=men`** for men's products
3. **Use `/api/products/main-category/28`** if 28 is main category
4. **Restart application** to enable new filter endpoints

## When You Restart Application

After restart, these will work:
- ✅ `GET /api/products/filter-products?categoryId=28&gender=men`
- ✅ `GET /api/products/gender/men?categoryId=28`
- ✅ `POST /api/products/search/filter` with gender filter

## Immediate Action Required

**Restart the application** to enable all new filtering endpoints:
```bash
cd "d:\App development\backendAPI\flintnthread-backend"
./mvnw spring-boot:run
```

Until then, use the existing working solutions above for men's category filtering.
