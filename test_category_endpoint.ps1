# Test Category Products Endpoint
Write-Host "Testing GET /api/products/category/28" -ForegroundColor Green

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products/category/28" -Method GET
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response Length: $($response.Content.Length) characters" -ForegroundColor Yellow
    
    # Try to parse as JSON
    try {
        $data = $response.Content | ConvertFrom-Json
        Write-Host "JSON parsed successfully!" -ForegroundColor Green
        if ($data.content) {
            Write-Host "Products found: $($data.content.Count)" -ForegroundColor Cyan
        } elseif ($data -is [array]) {
            Write-Host "Products found: $($data.Count)" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "Raw Response: $($response.Content.Substring(0, 200))..." -ForegroundColor White
    }
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

Write-Host "`nTesting basic products endpoint for comparison..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -Method GET
    Write-Host "Basic endpoint Status Code: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Basic endpoint Error: $($_.Exception.Message)" -ForegroundColor Red
}
