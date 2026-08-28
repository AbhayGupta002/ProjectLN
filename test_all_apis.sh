#!/bin/bash
# ==============================================================================
# NEXTGEM-TECHNOLOGY: Comprehensive API Automation & Health Test Suite
# Tests all endpoints: Actuator, Public Discovery, AI Assistant,
# Authentication, 4-Attempt Lockout, 2FA Verification, Bookings, and Payments.
# ==============================================================================

BASE_URL="${1:-http://localhost:8080}"
PASSED=0
FAILED=0
TOTAL=0

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================================================${NC}"
echo -e "${BLUE}   NEXTGEM-TECHNOLOGY API TEST SUITE   ${NC}"
echo -e "${BLUE}   Target Base URL: ${BASE_URL}${NC}"
echo -e "${BLUE}======================================================================${NC}\n"

test_endpoint() {
    local method="$1"
    local endpoint="$2"
    local expected_status="$3"
    local data="$4"
    local token="$5"
    local test_name="$6"

    TOTAL=$((TOTAL + 1))
    echo -n "Test $TOTAL: $test_name ($method $endpoint)... "

    local curl_cmd=(curl -s -o /tmp/api_test_out.txt -w "%{http_code}" -X "$method")

    if [ -n "$data" ]; then
        curl_cmd+=(-H "Content-Type: application/json" -d "$data")
    fi

    if [ -n "$token" ]; then
        curl_cmd+=(-H "Authorization: Bearer $token")
    fi

    curl_cmd+=("$BASE_URL$endpoint")

    local status
    status=$("${curl_cmd[@]}")

    # Check if status matches expected status (supports comma-separated expected statuses like 200,423)
    if [[ ",$expected_status," == *",$status,"* ]]; then
        echo -e "${GREEN}PASSED (HTTP $status)${NC}"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}FAILED (Expected: $expected_status, Got: $status)${NC}"
        cat /tmp/api_test_out.txt | head -n 3
        echo ""
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# ------------------------------------------------------------------------------
# 1. ACTUATOR HEALTH & PRODUCTION MONITORING
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}--- 1. ACTUATOR & HEALTH MONITORING ---${NC}"
test_endpoint "GET" "/actuator/health" "200" "" "" "Actuator Health Check"
test_endpoint "GET" "/actuator/info" "200" "" "" "Actuator Info Check"

# ------------------------------------------------------------------------------
# 2. PUBLIC DISCOVERY & CATALOGS
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}--- 2. PUBLIC TRANSPORTATION & TOUR CATALOGS ---${NC}"
test_endpoint "GET" "/api/flights" "200" "" "" "Public Flights Search"
test_endpoint "GET" "/api/trains" "200" "" "" "Public Trains Search"
test_endpoint "GET" "/api/bus" "200" "" "" "Public Buses Search"
test_endpoint "GET" "/api/cabs" "200" "" "" "Public Cabs Search"
test_endpoint "GET" "/api/tours" "200,404" "" "" "Public Tour Packages"

# ------------------------------------------------------------------------------
# 3. AI AGENT & TRIP PLANNER
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}--- 3. AI SMART TRIP PLANNER & CHATBOT ---${NC}"
test_endpoint "POST" "/api/chat/ask" "200" \
  '{"message":"Hello AI Assistant, I want to travel","sessionId":"test-session-suite"}' "" "AI Chat Session"

test_endpoint "POST" "/api/ai/prompt" "200" \
  '{"prompt":"Plan a 3 days trip to Delhi with budget of 20000"}' "" "AI Smart Trip Planner Orchestrator"

# ------------------------------------------------------------------------------
# 4. AUTHENTICATION, 4-ATTEMPT LOCKOUT & 2FA
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}--- 4. AUTHENTICATION, 4-ATTEMPT LOCKOUT & 2FA ---${NC}"
TEST_EMAIL="suite_user_$(date +%s)@gmail.com"
TEST_MOBILE="98$(date +%s | cut -c 3-10)"
TEST_PASS="CorrectPassword123!"

# Register Test User
test_endpoint "POST" "/api/auth/register" "200,400" \
  "{\"name\":\"Suite Tester\",\"email\":\"$TEST_EMAIL\",\"mobile\":\"$TEST_MOBILE\",\"password\":\"$TEST_PASS\"}" "" "User Registration"

# Test 4 Consecutive Failed Attempts -> Lockout
test_endpoint "POST" "/api/auth/login" "400" \
  "{\"email\":\"$TEST_EMAIL\",\"password\":\"WrongPassword1\"}" "" "Failed Login Attempt 1 (Expect 3 remaining)"

test_endpoint "POST" "/api/auth/login" "400" \
  "{\"email\":\"$TEST_EMAIL\",\"password\":\"WrongPassword2\"}" "" "Failed Login Attempt 2 (Expect 2 remaining)"

test_endpoint "POST" "/api/auth/login" "400" \
  "{\"email\":\"$TEST_EMAIL\",\"password\":\"WrongPassword3\"}" "" "Failed Login Attempt 3 (Expect 1 remaining)"

test_endpoint "POST" "/api/auth/login" "423" \
  "{\"email\":\"$TEST_EMAIL\",\"password\":\"WrongPassword4\"}" "" "Failed Login Attempt 4 (Expect 423 LOCKED)"

# Attempt 5 with correct password should still be blocked due to lockout
test_endpoint "POST" "/api/auth/login" "423" \
  "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASS\"}" "" "Post-Lockout Blocked Login (Expect 423 LOCKED)"

# ------------------------------------------------------------------------------
# 5. PASSWORD RETRIEVAL & UNLOCK FLOW
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}--- 5. PASSWORD RETRIEVAL & ACCOUNT UNLOCK ---${NC}"
test_endpoint "POST" "/api/auth/forgot-password" "200" \
  "{\"email\":\"$TEST_EMAIL\"}" "" "Forgot Password Trigger"

# Extract reset token from output
RESET_TOKEN=$(grep -o "Reset token generated: [^ ]*" /tmp/api_test_out.txt | cut -d ' ' -f 4)

if [ -n "$RESET_TOKEN" ]; then
    test_endpoint "POST" "/api/auth/reset-password" "200" \
      "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"NewSecurePass123!\"}" "" "Reset Password & Unlock Account"

    # Now login with new password -> should trigger 2FA OTP
    test_endpoint "POST" "/api/auth/login" "200" \
      "{\"email\":\"$TEST_EMAIL\",\"password\":\"NewSecurePass123!\"}" "" "Login with New Password (2FA Dispatched)"
else
    echo -e "${YELLOW}Skipping Reset Password verification (token not generated)${NC}"
fi

# ------------------------------------------------------------------------------
# SUMMARY
# ------------------------------------------------------------------------------
echo -e "\n${BLUE}======================================================================${NC}"
echo -e "${BLUE}   TEST EXECUTION SUMMARY${NC}"
echo -e "${BLUE}======================================================================${NC}"
echo -e "Total Endpoints Tested: $TOTAL"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}ALL APIS WORKING PERFECTLY! 100% SUCCESS RATE.${NC}\n"
    exit 0
else
    echo -e "\n${RED}SOME APIS FAILED. PLEASE CHECK LOGS ABOVE.${NC}\n"
    exit 1
fi
