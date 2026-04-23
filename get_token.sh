#!/bin/bash

echo "=== Step 1: Send OTP ==="
curl -X POST http://localhost:8080/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sravanisurampalli612@gmail.com"
  }'

echo ""
echo "=== Step 2: Verify OTP (replace YOUR_OTP_HERE) ==="
echo "curl -X POST http://localhost:8080/auth/verify-otp \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{"
echo "    \"email\": \"sravanisurampalli612@gmail.com\","
echo "    \"otp\": \"YOUR_OTP_HERE\""
echo "  }'"
