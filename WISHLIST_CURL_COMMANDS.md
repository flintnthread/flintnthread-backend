# Wishlist Curl Commands - Complete Testing Guide

## Base URL
```
http://localhost:8080
```

---

## 1. Get User Wishlist (with Image URLs)

### Basic Request
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/123" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### With Pretty Output
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/123" \
  -H "Content-Type: application/json" \
  | python -m json.tool
```

### Windows PowerShell Version
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/wishlist/user/123" `
  -Method GET `
  -Headers @{"Content-Type"="application/json"} | ConvertTo-Json -Depth 10
```

---

## 2. Add Product to Wishlist

### Basic Request
```bash
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "productId": 456
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

### With Pretty Output
```bash
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "productId": 456
  }' \
  | python -m json.tool
```

### Windows PowerShell Version
```powershell
$body = @{
    userId = 123
    productId = 456
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/wishlist/add" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body | ConvertTo-Json -Depth 10
```

---

## 3. Remove from Wishlist

### Basic Request
```bash
curl -X DELETE "http://localhost:8080/api/wishlist/remove?userId=123&productId=456" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### Windows PowerShell Version
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/wishlist/remove?userId=123&productId=456" `
  -Method DELETE `
  -Headers @{"Content-Type"="application/json"}
```

---

## 4. Check Product in Wishlist

### Basic Request
```bash
curl -X GET "http://localhost:8080/api/wishlist/check?userId=123&productId=456" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### Windows PowerShell Version
```powershell
(Invoke-RestMethod -Uri "http://localhost:8080/api/wishlist/check?userId=123&productId=456" `
  -Method GET `
  -Headers @{"Content-Type"="application/json"}).ToString()
```

---

## Expected Responses

### Get User Wishlist Response (Enhanced with Image URLs)
```json
[
  {
    "wishlistId": 456,
    "productId": 789,
    "productName": "Men's Premium Shirt",
    "image": "https://flintnthread.in/uploads/products/shirt1.jpg",
    "imageUrl": "https://flintnthread.in/uploads/products/shirt1.jpg",
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

### Add to Wishlist Response
```json
{
  "wishlistId": 456,
  "productId": 789,
  "productName": "Men's Premium Shirt",
  "image": "https://flintnthread.in/uploads/products/shirt1.jpg",
  "imageUrl": "https://flintnthread.in/uploads/products/shirt1.jpg",
  "images": [...],
  "size": "M",
  "color": "blue",
  "inStock": true,
  "sellingPrice": 999.00,
  "mrpPrice": 1299.00,
  "addedAt": "2026-04-20T10:30:00"
}
```

### Remove from Wishlist Response
```json
"Product removed from wishlist"
```

### Check Product in Wishlist Response
```json
true
```
or
```json
false
```

---

## Complete Testing Scenario

### Step 1: Add Product to Wishlist
```bash
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "productId": 456
  }'
```

### Step 2: Check if Product is in Wishlist
```bash
curl -X GET "http://localhost:8080/api/wishlist/check?userId=123&productId=456"
```

### Step 3: Get User Wishlist (with Image URLs)
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/123" | python -m json.tool
```

### Step 4: Remove from Wishlist
```bash
curl -X DELETE "http://localhost:8080/api/wishlist/remove?userId=123&productId=456"
```

### Step 5: Verify Removal
```bash
curl -X GET "http://localhost:8080/api/wishlist/check?userId=123&productId=456"
```

---

## Testing Different Users

### User 123
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/123"
```

### User 456
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/456"
```

### User 789
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/789"
```

---

## Error Handling Tests

### Invalid User ID
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/999999" \
  -w "\nHTTP Status: %{http_code}\n"
```

### Invalid Product ID
```bash
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "productId": 999999
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

### Missing Parameters
```bash
curl -X POST "http://localhost:8080/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

---

## Image URL Verification

### Check Image URLs in Response
```bash
curl -X GET "http://localhost:8080/api/wishlist/user/123" | \
  python -c "
import json, sys
data = json.load(sys.stdin)
for item in data:
    print('Product:', item['productName'])
    print('Image URL:', item.get('imageUrl', 'N/A'))
    print('Primary Image:', item.get('image', 'N/A'))
    if item.get('images'):
        print('All Images:')
        for img in item['images']:
            print(f'  - {img.get(\"imageUrl\", \"N/A\")} (Primary: {img.get(\"isPrimary\", False)})')
    print('---')
"
```

### Test Image URL Accessibility
```bash
# Extract first image URL and test accessibility
curl -X GET "http://localhost:8080/api/wishlist/user/123" | \
  python -c "
import json, sys, urllib.request
data = json.load(sys.stdin)
if data and data[0].get('imageUrl'):
    url = data[0]['imageUrl']
    print('Testing URL:', url)
    try:
        response = urllib.request.urlopen(url)
        print('Status:', response.getcode())
        print('Accessible: YES')
    except Exception as e:
        print('Accessible: NO -', str(e))
else:
    print('No image URL found')
"
```

---

## Batch Testing Script

### Complete Wishlist Test Script
```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
USER_ID=123
PRODUCT_ID=456

echo "=== Wishlist Testing Script ==="
echo

echo "1. Adding product to wishlist..."
curl -X POST "$BASE_URL/api/wishlist/add" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": $USER_ID, \"productId\": $PRODUCT_ID}" \
  | python -m json.tool

echo
echo "2. Checking if product is in wishlist..."
curl -X GET "$BASE_URL/api/wishlist/check?userId=$USER_ID&productId=$PRODUCT_ID"

echo
echo "3. Getting user wishlist with image URLs..."
curl -X GET "$BASE_URL/api/wishlist/user/$USER_ID" | python -m json.tool

echo
echo "4. Removing product from wishlist..."
curl -X DELETE "$BASE_URL/api/wishlist/remove?userId=$USER_ID&productId=$PRODUCT_ID"

echo
echo "5. Verifying removal..."
curl -X GET "$BASE_URL/api/wishlist/check?userId=$USER_ID&productId=$PRODUCT_ID"

echo
echo "=== Testing Complete ==="
```

---

## Windows PowerShell Complete Test

### PowerShell Test Script
```powershell
$BASE_URL = "http://localhost:8080"
$USER_ID = 123
$PRODUCT_ID = 456

Write-Host "=== Wishlist Testing Script ===" -ForegroundColor Green
Write-Host

Write-Host "1. Adding product to wishlist..." -ForegroundColor Cyan
$body = @{ userId = $USER_ID; productId = $PRODUCT_ID } | ConvertTo-Json
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/wishlist/add" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. Checking if product is in wishlist..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/wishlist/check?userId=$USER_ID&productId=$PRODUCT_ID" -Method GET
    Write-Host "Product in wishlist: $response"
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n3. Getting user wishlist with image URLs..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/wishlist/user/$USER_ID" -Method GET
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n4. Removing product from wishlist..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/wishlist/remove?userId=$USER_ID&productId=$PRODUCT_ID" -Method DELETE
    Write-Host $response
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n5. Verifying removal..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/wishlist/check?userId=$USER_ID&productId=$PRODUCT_ID" -Method GET
    Write-Host "Product in wishlist: $response"
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Testing Complete ===" -ForegroundColor Green
```

---

## Quick Test Commands

### Test All Wishlist Endpoints
```bash
# 1. Get wishlist
curl -X GET "http://localhost:8080/api/wishlist/user/123"

# 2. Add to wishlist
curl -X POST "http://localhost:8080/api/wishlist/add" -H "Content-Type: application/json" -d '{"userId": 123, "productId": 456}'

# 3. Check wishlist
curl -X GET "http://localhost:8080/api/wishlist/check?userId=123&productId=456"

# 4. Remove from wishlist
curl -X DELETE "http://localhost:8080/api/wishlist/remove?userId=123&productId=456"
```

### Test Image URLs
```bash
# Get wishlist and check image URLs
curl -X GET "http://localhost:8080/api/wishlist/user/123" | python -m json.tool
```

All wishlist curl commands are ready with enhanced image URL functionality!
