#!/bin/bash

echo "��� Testing Merchant API..."
echo ""

BASE_URL="http://localhost:8080"

# Test health
echo "1. Testing Health..."
curl -s $BASE_URL/actuator/health | grep -q "UP" && echo "✅ Health OK" || echo "❌ Health FAIL"

# Test transactions
echo "2. Testing Transactions..."
curl -s $BASE_URL/transactions | grep -q "txn_" && echo "✅ Transactions OK" || echo "❌ Transactions FAIL"

# Test OpenAPI
echo "3. Testing OpenAPI..."
curl -s $BASE_URL/v3/api-docs | grep -q "openapi" && echo "✅ OpenAPI OK" || echo "❌ OpenAPI FAIL"

# Test rate limiting
echo "4. Testing Rate Limiting..."
count=0
for i in {1..105}; do
  status=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/transactions)
  if [ "$status" = "429" ]; then
    echo "✅ Rate Limit OK - Got 429 at request $i"
    count=1
    break
  fi
done
[ $count -eq 0 ] && echo "❌ Rate Limit FAIL"

echo ""
echo "✅ Tests Complete!"
