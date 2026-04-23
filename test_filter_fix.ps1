# Test Filter Endpoints After Routing Fix
Write-Host "Testing Filter Endpoints..." -ForegroundColor Green

# Test 1: POST filter endpoint
Write-Host "`n1. Testing POST /api/products/filter" -ForegroundColor Cyan
try {
    $body = @{
        categoryId = 28
        genders = @("men")
        page = 0
        size = 10
        sortBy = "createdAt"
        sortDirection = "desc"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/products/filter" -Method POST -Body $body -ContentType "application/json"
    Write-Host "✅ POST filter endpoint works!" -ForegroundColor Green
    Write-Host "Products found: $($response.content.Count)" -ForegroundColor White
} catch {
    Write-Host "❌ POST filter error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: GET filter endpoint
Write-Host "`n2. Testing GET /api/products/filter" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/products/filter?categoryId=28&gender=men" -Method GET
    Write-Host "✅ GET filter endpoint works!" -ForegroundColor Green
    Write-Host "Products found: $($response.content.Count)" -ForegroundColor White
} catch {
    Write-Host "❌ GET filter error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Verify /{id} endpoint still works
Write-Host "`n3. Testing GET /api/products/1 (ID endpoint)" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method GET
    Write-Host "✅ ID endpoint works!" -ForegroundColor Green
    Write-Host "Product ID: $($response.id)" -ForegroundColor White
} catch {
    Write-Host "❌ ID endpoint error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
