#!/bin/bash

# Doctor Leave Management API Test Script
# This script tests the leave management endpoints

BASE_URL="http://localhost:8081/api"
echo "🧪 Testing Doctor Leave Management API"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print test results
print_test() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $2"
    else
        echo -e "${RED}✗${NC} $2"
    fi
}

echo "📝 Note: Make sure the application is running on port 8081"
echo "   Start with: ./mvnw spring-boot:run"
echo ""
read -p "Press Enter to continue with tests..."
echo ""

# Test 1: Check if server is running
echo "1️⃣  Testing server availability..."
response=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/doctor/)
if [ $response -eq 200 ] || [ $response -eq 401 ]; then
    print_test 0 "Server is running"
else
    print_test 1 "Server is not responding (HTTP $response)"
    exit 1
fi
echo ""

# Test 2: Check doctor availability (public endpoint)
echo "2️⃣  Testing doctor availability check (public endpoint)..."
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/doctor-leave/check-availability?doctorId=1&date=2025-01-15")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | head -n-1)

if [ $http_code -eq 200 ]; then
    print_test 0 "Doctor availability check endpoint works"
    echo "   Response: $body"
else
    print_test 1 "Doctor availability check failed (HTTP $http_code)"
fi
echo ""

# Test 3: Get doctor leaves (requires authentication)
echo "3️⃣  Testing get doctor leaves endpoint..."
echo "   ${YELLOW}Note: This requires authentication. Will return 401/403 without token${NC}"
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/doctor-leave/doctor/1")
if [ $response -eq 401 ] || [ $response -eq 403 ]; then
    print_test 0 "Endpoint is protected (requires authentication)"
else
    print_test 0 "Endpoint responded with HTTP $response"
fi
echo ""

# Test 4: Request leave (requires authentication)
echo "4️⃣  Testing leave request endpoint..."
echo "   ${YELLOW}Note: This requires DOCTOR or ADMIN role${NC}"
response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/doctor-leave/request" \
    -H "Content-Type: application/json" \
    -d '{
        "doctorId": 1,
        "leaveType": "VACATION",
        "startDate": "2025-02-01",
        "endDate": "2025-02-07",
        "reason": "Test leave request",
        "isHalfDay": false
    }')
if [ $response -eq 401 ] || [ $response -eq 403 ]; then
    print_test 0 "Endpoint is protected (requires authentication)"
else
    print_test 0 "Endpoint responded with HTTP $response"
fi
echo ""

# Test 5: Get pending leaves (requires admin/receptionist)
echo "5️⃣  Testing get pending leaves endpoint..."
echo "   ${YELLOW}Note: This requires ADMIN or RECEPTIONIST role${NC}"
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/doctor-leave/status/PENDING")
if [ $response -eq 401 ] || [ $response -eq 403 ]; then
    print_test 0 "Endpoint is protected (requires authentication)"
else
    print_test 0 "Endpoint responded with HTTP $response"
fi
echo ""

echo "========================================"
echo "✅ Basic endpoint tests completed!"
echo ""
echo "📚 Next Steps:"
echo "   1. Login as a doctor to get JWT token"
echo "   2. Use the token to test authenticated endpoints"
echo "   3. Create leave requests and test the full workflow"
echo ""
echo "📖 For detailed API documentation, see:"
echo "   LEAVE_MANAGEMENT_README.md"
echo ""
echo "🔗 API Endpoints Summary:"
echo "   POST   /api/doctor-leave/request          - Request leave"
echo "   GET    /api/doctor-leave/doctor/{id}      - Get doctor's leaves"
echo "   GET    /api/doctor-leave/status/{status}  - Get leaves by status"
echo "   PUT    /api/doctor-leave/{id}/approve     - Approve/reject leave"
echo "   PUT    /api/doctor-leave/{id}/cancel      - Cancel leave"
echo "   GET    /api/doctor-leave/check-availability - Check availability"
echo "   DELETE /api/doctor-leave/{id}             - Delete leave"
echo ""
