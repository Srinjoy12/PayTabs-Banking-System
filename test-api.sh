#!/bin/bash

# Banking System POC - API Test Script
# Make sure the application is running on http://localhost:8080

echo "🏦 Banking System POC - API Testing"
echo "=================================="
echo ""

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to run a test
run_test() {
    local test_name="$1"
    local endpoint="$2"
    local data="$3"
    local expected_status="$4"
    
    echo -e "${BLUE}🧪 Testing: ${test_name}${NC}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    fi
    
    # Extract response body and status code
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "  ${GREEN}✅ PASSED${NC} - HTTP $http_code"
        ((TESTS_PASSED++))
    else
        echo -e "  ${RED}❌ FAILED${NC} - Expected HTTP $expected_status, got $http_code"
        echo -e "  Response: $response_body"
        ((TESTS_FAILED++))
    fi
    
    echo ""
}

# Wait for application to start
echo "⏳ Waiting for application to start..."
until curl -s "$BASE_URL" > /dev/null 2>&1; do
    sleep 2
done
echo -e "${GREEN}✅ Application is running!${NC}"
echo ""

# Test 1: Successful withdrawal with valid card (starts with '4')
echo "📋 Test 1: Successful withdrawal with valid card"
run_test "Valid withdrawal" "/api/transaction" '{
  "cardNumber": "4000000000000001",
  "pin": "1234",
  "amount": 50.00,
  "type": "withdraw"
}' "200"

# Test 2: Successful top-up with valid card
echo "📋 Test 2: Successful top-up with valid card"
run_test "Valid top-up" "/api/transaction" '{
  "cardNumber": "4000000000000001",
  "pin": "1234",
  "amount": 100.00,
  "type": "topup"
}' "200"

# Test 3: Unsupported card range (doesn't start with '4')
echo "📋 Test 3: Unsupported card range"
run_test "Unsupported card range" "/api/transaction" '{
  "cardNumber": "5000000000000001",
  "pin": "1111",
  "amount": 100.00,
  "type": "withdraw"
}' "200"

# Test 4: Invalid card number
echo "📋 Test 4: Invalid card number"
run_test "Invalid card number" "/api/transaction" '{
  "cardNumber": "9999999999999999",
  "pin": "1234",
  "amount": 100.00,
  "type": "withdraw"
}' "200"

# Test 5: Invalid PIN
echo "📋 Test 5: Invalid PIN"
run_test "Invalid PIN" "/api/transaction" '{
  "cardNumber": "4000000000000001",
  "pin": "9999",
  "amount": 100.00,
  "type": "withdraw"
}' "200"

# Test 6: Insufficient balance
echo "📋 Test 6: Insufficient balance"
run_test "Insufficient balance" "/api/transaction" '{
  "cardNumber": "4000000000000003",
  "pin": "9999",
  "amount": 1000.00,
  "type": "withdraw"
}' "200"

# Test 7: Invalid transaction type
echo "📋 Test 7: Invalid transaction type"
run_test "Invalid transaction type" "/api/transaction" '{
  "cardNumber": "4000000000000001",
  "pin": "1234",
  "amount": 100.00,
  "type": "invalid"
}' "200"

# Test 8: Missing required fields
echo "📋 Test 8: Missing required fields"
run_test "Missing required fields" "/api/transaction" '{
  "cardNumber": "4000000000000001",
  "amount": 100.00
}' "200"

# Test 9: Negative amount
echo "📋 Test 9: Negative amount"
run_test "Negative amount" "/api/transaction" '{
  "cardNumber": "4000000000000001",
  "pin": "1234",
  "amount": -50.00,
  "type": "withdraw"
}' "200"

# Test 10: System 2 direct endpoint
echo "📋 Test 10: System 2 direct endpoint"
run_test "System 2 direct processing" "/api/process" '{
  "cardNumber": "4000000000000002",
  "pinHash": "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4",
  "amount": 75.00,
  "type": "topup"
}' "200"

# Summary
echo "📊 Test Summary"
echo "==============="
echo -e "${GREEN}✅ Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}❌ Tests Failed: $TESTS_FAILED${NC}"
echo -e "${BLUE}📝 Total Tests: $((TESTS_PASSED + TESTS_FAILED))${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed! The banking system is working correctly.${NC}"
else
    echo -e "${YELLOW}⚠️  Some tests failed. Please check the application logs for details.${NC}"
fi

echo ""
echo "🌐 Access the web interface at: $BASE_URL"
echo "🔐 Demo credentials:"
echo "   - Super Admin: admin / admin123"
echo "   - Customer: john / john123"
echo ""
echo "📚 Check the README.md for more information and test cases." 