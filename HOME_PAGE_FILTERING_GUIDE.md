# Home Page Product Filtering API Guide

## Overview
Comprehensive filtering system for home page products with support for sort, category, gender, price range, colors, sizes, and more.

## Available Endpoints

### 1. POST Filter (Recommended for complex filters)
```
POST /api/products/filter
Content-Type: application/json
```

### 2. GET Filter (Simple query parameters)
```
GET /api/products/filter?categoryId=28&gender=men&sortBy=price&sortDirection=asc
```

## Filter Parameters

### Search & Pagination
- `keyword` - Search in product name, description
- `page` - Page number (default: 0)
- `size` - Page size (default: 10)
- `sortBy` - Sort field (default: createdAt)
- `sortDirection` - Sort direction: asc/desc (default: desc)

### Category Filters
- `categoryId` - Category ID
- `subcategoryId` - Subcategory ID

### Product Attributes
- `gender` - Gender filter (men, women, unisex)
- `fabrics` - Fabric types list
- `colors` - Color names list
- `sizes` - Size values list

### Price Range
- `minPrice` - Minimum price
- `maxPrice` - Maximum price

### Other Filters
- `sellerId` - Filter by seller
- `inStock` - Only products with stock (true/false)
- `minRating` - Minimum rating (future implementation)

## Usage Examples

### Example 1: Filter by Category and Gender
```bash
POST /api/products/filter
{
  "categoryId": 28,
  "genders": ["men"],
  "page": 0,
  "size": 20,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

### Example 2: Filter by Price Range and Colors
```bash
POST /api/products/filter
{
  "minPrice": 500,
  "maxPrice": 2000,
  "colors": ["red", "blue"],
  "inStock": true,
  "sortBy": "sellingPrice",
  "sortDirection": "asc"
}
```

### Example 3: Search with Multiple Filters
```bash
POST /api/products/filter
{
  "keyword": "shirt",
  "categoryId": 28,
  "genders": ["men"],
  "minPrice": 300,
  "maxPrice": 1500,
  "sizes": ["M", "L"],
  "page": 0,
  "size": 10,
  "sortBy": "name",
  "sortDirection": "asc"
}
```

### Example 4: GET Request with Query Parameters
```bash
GET /api/products/filter?categoryId=28&gender=men&sortBy=price&sortDirection=asc&page=0&size=10
```

### Example 5: Home Page Default View
```bash
POST /api/products/filter
{
  "page": 0,
  "size": 20,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

## Response Format
```json
{
  "content": [
    {
      "id": 1,
      "name": "Product Name",
      "shortDescription": "Description",
      "categoryId": 28,
      "subcategoryId": 15,
      "gender": "men",
      "variants": [
        {
          "id": 1,
          "color": "red",
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
    "pageSize": 10,
    "sort": {...}
  },
  "totalElements": 150,
  "totalPages": 15,
  "first": true,
  "last": false
}
```

## Sort Options
- `createdAt` - Sort by creation date
- `name` - Sort by product name
- `sellingPrice` - Sort by price (via variants)
- `updatedAt` - Sort by last update

## Home Page Implementation Guide

### Frontend Integration

#### 1. Initial Load
```javascript
// Load initial products
const response = await fetch('/api/products/filter', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    page: 0,
    size: 20,
    sortBy: 'createdAt',
    sortDirection: 'desc'
  })
});
```

#### 2. Apply Category Filter
```javascript
// Filter by category
const filterByCategory = async (categoryId) => {
  const response = await fetch('/api/products/filter', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      categoryId: categoryId,
      page: 0,
      size: 20,
      sortBy: 'createdAt',
      sortDirection: 'desc'
    })
  });
};
```

#### 3. Apply Gender Filter
```javascript
// Filter by gender
const filterByGender = async (gender) => {
  const response = await fetch('/api/products/filter', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      genders: [gender],
      page: 0,
      size: 20,
      sortBy: 'createdAt',
      sortDirection: 'desc'
    })
  });
};
```

#### 4. Apply Multiple Filters
```javascript
// Apply multiple filters
const applyFilters = async (filters) => {
  const requestBody = {
    page: 0,
    size: 20,
    sortBy: 'createdAt',
    sortDirection: 'desc'
  };

  if (filters.categoryId) requestBody.categoryId = filters.categoryId;
  if (filters.gender) requestBody.genders = [filters.gender];
  if (filters.minPrice) requestBody.minPrice = filters.minPrice;
  if (filters.maxPrice) requestBody.maxPrice = filters.maxPrice;
  if (filters.colors?.length) requestBody.colors = filters.colors;
  if (filters.sizes?.length) requestBody.sizes = filters.sizes;

  const response = await fetch('/api/products/filter', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requestBody)
  });

  return response.json();
};
```

#### 5. Sort Products
```javascript
// Sort products
const sortProducts = async (sortBy, sortDirection = 'desc') => {
  const response = await fetch('/api/products/filter', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      page: 0,
      size: 20,
      sortBy: sortBy,
      sortDirection: sortDirection
    })
  });
};
```

## Performance Considerations

1. **Indexing**: Ensure database has indexes on frequently filtered fields
2. **Pagination**: Always use pagination for large datasets
3. **Distinct Results**: Query automatically handles distinct results when joining variants
4. **Active Products**: Only returns products with status "active"

## Error Handling

- **400 Bad Request**: Invalid filter parameters
- **500 Internal Server Error**: Database or server issues
- **200 OK**: Successful response with paginated results

## Testing

Use the provided test scripts or Postman collection to test different filter combinations.

## Future Enhancements

- Rating filter implementation
- Advanced search with weights
- Faceted search results
- Performance optimization with caching
