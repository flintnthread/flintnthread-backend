#!/bin/bash

echo "=== POST Address with Bearer Token ==="
echo "Replace NEW_JWT_TOKEN_HERE with the token from verify-otp response"
echo ""

curl -X POST http://localhost:8080/api/addresses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer NEW_JWT_TOKEN_HERE" \
  -d '{
    "name": "sankarp",
    "email": "sankarp036@gmail.com",
    "phone": "6305015198",
    "addressLine1": "Street 1",
    "addressLine2": "Near temple",
    "city": "Hyderabad",
    "state": "Telangana",
    "country": "India",
    "pincode": "500001",
    "addressType": "home",
    "isDefault": true
  }'
