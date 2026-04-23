# API Test Script for Flint & Thread Backend
# Run this PowerShell script to test the main category endpoint

$BaseUrl = "http://localhost:8080"

Write-Host "=== Flint & Thread API Test ===" -ForegroundColor Green
Write-Host "Testing Main Category Products Endpoint..." -ForegroundColor Yellow

# Test 1: Get products by main category ID (your new endpoint)
try {
    Write-Host "`n1. Testing GET /api/products/main-category/28" -ForegroundColor Cyan
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/products/main-category/28" -Method GET -Headers @{"Content-Type"="application/json"} -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor White
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

# Test 2: Get all products (to verify API is working)
try {
    Write-Host "`n2. Testing GET /api/products (basic endpoint)" -ForegroundColor Cyan
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/products" -Method GET -Headers @{"Content-Type"="application/json"} -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    $data = $response.Content | ConvertFrom-Json
    Write-Host "Found $($data.content.Count) products" -ForegroundColor White
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Get categories (to verify category data exists)
try {
    Write-Host "`n3. Testing GET /api/categories/main" -ForegroundColor Cyan
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/categories/main" -Method GET -Headers @{"Content-Type"="application/json"} -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    $data = $response.Content | ConvertFrom-Json
    Write-Host "Found $($data.Count) main categories" -ForegroundColor White
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
Write-Host "Check API_TESTING_GUIDE.md for more testing options" -ForegroundColor Yellow
