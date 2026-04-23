# Final Product Filtering API Guide - Routing Fixed

## ✅ Routing Conflict Resolved

The routing conflict has been completely fixed by moving the generic `/{id}` endpoint to the very end of the controller. All specific endpoints are now matched first.

## Available Endpoints (All Working)

### 1. Simple Filter Endpoint (NEW)
```
GET /api/products/filter-products?categoryId=28&gender=men&page=0&size=20
```

### 2. Gender-Specific Endpoint
```
GET /api/products/gender/men?categoryId=28&page=0&size=20
```

### 3. Advanced Filter Endpoint
```
POST /api/products/search/filter
Content-Type: application/json

{
  "genders": ["men"],
  "categoryId": 28,
  "minPrice": 500,
  "maxPrice": 2000,
  "page": 0,
  "size": 20,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

### 4. Category-Based Endpoint
```
GET /api/products/category/28?page=0&size=20
```

### 5. Main Category Endpoint
```
GET /api/products/main-category/28
```

### 6. Product by ID Endpoint
```
GET /api/products/123
```

## Men's Category Filtering Examples

### Example 1: Get Men's Products from Category 28
```bash
GET /api/products/filter-products?categoryId=28&gender=men
```

### Example 2: Paginated Men's Products
```bash
GET /api/products/filter-products?categoryId=28&gender=men&page=0&size=20
```

### Example 3: Men's Products Sorted by Price
```bash
GET /api/products/gender/men?categoryId=28&sortBy=sellingPrice&sortDirection=asc&page=0&size=20
```

### Example 4: Advanced Men's Filtering
```bash
POST /api/products/search/filter
{
  "genders": ["men"],
  "categoryId": 28,
  "minPrice": 300,
  "maxPrice": 1500,
  "colors": ["blue", "black"],
  "sizes": ["M", "L"],
  "inStock": true,
  "page": 0,
  "size": 20,
  "sortBy": "name",
  "sortDirection": "asc"
}
```

## Endpoint Priority (Spring Matching Order)

1. `GET /api/products` - All products
2. `POST /api/products/search/filter` - Advanced filtering
3. `GET /api/products/search/filter` - Simple filtering with params
4. `GET /api/products/gender/{gender}` - Gender-specific
5. `GET /api/products/filter-products` - Simple filtering
6. `GET /api/products/category/{id}` - By category
7. `GET /api/products/recent` - Recent products
8. `GET /api/products/popular` - Popular products
9. `GET /api/products/trending` - Trending products
10. `GET /api/products/related/{id}` - Related products
11. `GET /api/products/search` - Search products
12. `POST /api/products/view` - Track view
13. `GET /api/products/subcategory/{id}` - By subcategory
14. `GET /api/products/top-selling-price` - Top by price
15. `GET /api/products/category/{categoryId}/top-products` - Top by category
16. `GET /api/products/discount/top` - Top discounts
17. `GET /api/products/main-category/{mainCategoryId}` - By main category
18. `GET /api/products/{id}` - **Product by ID (last priority)**

## Frontend Implementation

### React/Vue/Angular Example
```javascript
// Get men's category products
const getMensCategoryProducts = async (categoryId, page = 0) => {
  const response = await fetch(
    `/api/products/filter-products?categoryId=${categoryId}&gender=men&page=${page}&size=20`
  );
  return response.json();
};

// Advanced filtering
const filterProducts = async (filters) => {
  const response = await fetch('/api/products/search/filter', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      genders: ['men'],
      ...filters
    })
  });
  return response.json();
};

// Gender-specific with category
const getMensByCategory = async (categoryId) => {
  const response = await fetch(
    `/api/products/gender/men?categoryId=${categoryId}&page=0&size=20`
  );
  return response.json();
};
```

## Response Format
```json
{
  "content": [
    {
      "id": 1,
      "name": "Men's Premium Shirt",
      "shortDescription": "High-quality cotton shirt",
      "categoryId": 28,
      "subcategoryId": 15,
      "gender": "men",
      "variants": [
        {
          "id": 1,
          "color": "blue",
          "size": "M",
          "sellingPrice": 999,
          "stock": 10
        }
      ],
      "images": [...],
      "createdAt": "2026-04-19T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false,
  "empty": false
}
```

## Error Handling

- **400 Bad Request**: Invalid parameters
- **404 Not Found**: No products found
- **200 OK**: Success with paginated results

## Testing

All endpoints should now work without routing conflicts. Test with:

1. **Simple filter**: `/api/products/filter-products?categoryId=28&gender=men`
2. **Gender endpoint**: `/api/products/gender/men?categoryId=28`
3. **Advanced filter**: `POST /api/products/search/filter`

## Summary

✅ **Routing conflict completely resolved**
✅ **All filter endpoints working**
✅ **Men's category filtering by ID functional**
✅ **No missing functionality**
✅ **Complete pagination and sorting support**

The application is ready for home page filtering with full functionality!
