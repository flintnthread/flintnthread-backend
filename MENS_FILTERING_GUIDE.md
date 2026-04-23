# Men's Category Product Filtering API Guide

## Problem Fixed
The routing conflict between `/filter` and `/{id}` has been resolved by using more specific endpoint paths.

## New Endpoints

### 1. Advanced Filtering (POST)
```
POST /api/products/search/filter
```

### 2. Simple Filtering (GET)
```
GET /api/products/search/filter
```

### 3. Dedicated Gender Filtering (NEW)
```
GET /api/products/gender/{gender}
```

## Men's Category Filtering Examples

### Example 1: Get Men's Products Only
```bash
GET /api/products/gender/men
```

### Example 2: Get Men's Products by Category ID
```bash
GET /api/products/gender/men?categoryId=28
```

### Example 3: Get Men's Products with Pagination
```bash
GET /api/products/gender/men?categoryId=28&page=0&size=20&sortBy=price&sortDirection=asc
```

### Example 4: Advanced Men's Filtering (POST)
```bash
POST /api/products/search/filter
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

### Example 5: Simple Men's Filtering (GET)
```bash
GET /api/products/search/filter?gender=men&categoryId=28&page=0&size=20
```

## Response Format
```json
{
  "content": [
    {
      "id": 1,
      "name": "Men's Shirt",
      "shortDescription": "Comfortable cotton shirt",
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
    "sort": {...}
  },
  "totalElements": 50,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

## Available Gender Values
- `men` - Men's products
- `women` - Women's products  
- `unisex` - Unisex products

## Sort Options
- `createdAt` - Sort by creation date (default)
- `name` - Sort by product name
- `sellingPrice` - Sort by price
- `updatedAt` - Sort by last update

## Frontend Integration Examples

### React/Vue/Angular Example
```javascript
// Get men's products by category
const getMensCategoryProducts = async (categoryId, page = 0) => {
  const response = await fetch(
    `/api/products/gender/men?categoryId=${categoryId}&page=${page}&size=20`
  );
  return response.json();
};

// Advanced men's filtering
const filterMensProducts = async (filters) => {
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
```

## Usage Scenarios

### Home Page - Men's Section
```bash
GET /api/products/gender/men?page=0&size=20&sortBy=createdAt&sortDirection=desc
```

### Category Page - Men's Clothing
```bash
GET /api/products/gender/men?categoryId=28&page=0&size=20
```

### Search Results - Men's Products
```bash
GET /api/products/search/filter?gender=men&keyword=shirt&page=0&size=20
```

### Filter by Price Range - Men's Products
```bash
POST /api/products/search/filter
{
  "genders": ["men"],
  "categoryId": 28,
  "minPrice": 500,
  "maxPrice": 2000,
  "page": 0,
  "size": 20
}
```

## Error Handling

- **400 Bad Request**: Invalid parameters (non-existent gender, invalid category ID)
- **404 Not Found**: No products found for the filters
- **200 OK**: Success with paginated results

## Testing the Fix

The routing conflict has been resolved by:
1. Moving filter endpoints to `/search/filter` path
2. Adding dedicated `/gender/{gender}` endpoint
3. Ensuring specific paths come before generic `/{id}` path

## All Available Endpoints Summary

- `GET /api/products/gender/men` - Men's products
- `GET /api/products/gender/men?categoryId=28` - Men's products by category
- `POST /api/products/search/filter` - Advanced filtering
- `GET /api/products/search/filter?gender=men` - Simple filtering
- `GET /api/products/category/28` - Products by category (all genders)
- `GET /api/products/main-category/28` - Products by main category

The routing conflict is now resolved and men's category filtering works perfectly!
