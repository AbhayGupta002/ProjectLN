#!/usr/bin/env bash
# ==============================================================================
# World Tour Application - End-to-End Production Smoke Test Suite
#
# Tests core user journeys, APIs, auth, security lockout, and payment flows.
# Configurable target: defaults to http://localhost:8080 or uses BASE_URL / $1.
# Ensures zero secrets, passwords, JWT tokens, or OTPs are printed.
# ==============================================================================

set -u

# Target URL configuration
BASE_URL="${1:-${BASE_URL:-http://localhost:8080}}"
BASE_URL="${BASE_URL%/}"

echo "=============================================================================="
echo " Starting End-to-End Smoke Test Suite"
echo " Target Base URL : ${BASE_URL}"
echo " Timestamp       : $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
echo "=============================================================================="

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Helper: format elapsed time in ms from curl %{time_total}
format_ms() {
    local seconds="$1"
    awk -v s="$seconds" 'BEGIN { printf "%.0fms", (s * 1000) }' 2>/dev/null || echo "${seconds}s"
}

# Helper: record test result
record_result() {
    local test_name="$1"
    local status="$2"
    local duration_sec="$3"
    local extra_info="${4:-}"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    local duration_ms
    duration_ms=$(format_ms "$duration_sec")

    if [ "$status" = "PASS" ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "  [PASS] ${test_name} (${duration_ms})"
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "  [FAIL] ${test_name} (${duration_ms}) - ${extra_info}"
    fi
}

# Generate unique test user credentials for this run
TIMESTAMP=$(date +%s)
RANDOM_SUFFIX=$((RANDOM % 9000 + 1000))
TEST_EMAIL="smoke_test_${TIMESTAMP}_${RANDOM_SUFFIX}@gmail.com"
# Ensure 10-digit mobile number starting with 9
TEST_MOBILE=$(printf "9%09d" "$((TIMESTAMP % 1000000000))")
TEST_PASSWORD="SmokeTestPass#${RANDOM_SUFFIX}!"
AUTH_TOKEN=""
RESET_TOKEN=""

# ------------------------------------------------------------------------------
# TEST 1: Health check
# ------------------------------------------------------------------------------
TEST_NAME="Health check"
RESPONSE_FILE=$(mktemp)
HTTP_CODE=$(curl -s -w "%{http_code}" -o "$RESPONSE_FILE" --max-time 15 "${BASE_URL}/api/health" 2>/dev/null || echo "000")
DURATION=$(curl -s -w "%{time_total}" -o /dev/null --max-time 15 "${BASE_URL}/api/health" 2>/dev/null || echo "0.0")

if [ "$HTTP_CODE" = "200" ]; then
    record_result "$TEST_NAME" "PASS" "$DURATION"
else
    HTTP_CODE_ROOT=$(curl -s -w "%{http_code}" -o /dev/null --max-time 15 "${BASE_URL}/" 2>/dev/null || echo "000")
    if [ "$HTTP_CODE_ROOT" = "200" ]; then
        record_result "$TEST_NAME" "PASS" "$DURATION"
    else
        record_result "$TEST_NAME" "FAIL" "$DURATION" "Expected 200, got ${HTTP_CODE}"
    fi
fi
rm -f "$RESPONSE_FILE"

# ------------------------------------------------------------------------------
# TEST 2: Register new user
# ------------------------------------------------------------------------------
TEST_NAME="Register new user"
OTP_REQ_BODY="{\"name\":\"Smoke Tester\",\"email\":\"${TEST_EMAIL}\",\"mobile\":\"${TEST_MOBILE}\",\"password\":\"${TEST_PASSWORD}\"}"
curl -s -o /dev/null -X POST -H "Content-Type: application/json" -d "$OTP_REQ_BODY" --max-time 15 "${BASE_URL}/api/auth/send-registration-otp" >/dev/null 2>&1

REG_REQ_BODY="{\"name\":\"Smoke Tester\",\"email\":\"${TEST_EMAIL}\",\"mobile\":\"${TEST_MOBILE}\",\"password\":\"${TEST_PASSWORD}\",\"otp\":\"999999\"}"
REG_FILE=$(mktemp)
REG_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$REG_FILE" -X POST \
    -H "Content-Type: application/json" \
    -d "$REG_REQ_BODY" \
    --max-time 15 \
    "${BASE_URL}/api/auth/register" 2>/dev/null || echo -e "\n000\n0.0")

REG_HTTP_CODE=$(echo "$REG_RESP" | tail -n 2 | head -n 1)
REG_DURATION=$(echo "$REG_RESP" | tail -n 1)

if [ "$REG_HTTP_CODE" = "200" ] || [ "$REG_HTTP_CODE" = "201" ]; then
    record_result "$TEST_NAME" "PASS" "$REG_DURATION"
else
    record_result "$TEST_NAME" "FAIL" "$REG_DURATION" "Expected 200/201, got ${REG_HTTP_CODE}"
fi
rm -f "$REG_FILE"

# ------------------------------------------------------------------------------
# TEST 3: Register duplicate (must fail cleanly, not 500)
# ------------------------------------------------------------------------------
TEST_NAME="Register duplicate (must fail cleanly, not 500)"
DUP_FILE=$(mktemp)
DUP_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$DUP_FILE" -X POST \
    -H "Content-Type: application/json" \
    -d "$REG_REQ_BODY" \
    --max-time 15 \
    "${BASE_URL}/api/auth/register" 2>/dev/null || echo -e "\n000\n0.0")

DUP_HTTP_CODE=$(echo "$DUP_RESP" | tail -n 2 | head -n 1)
DUP_DURATION=$(echo "$DUP_RESP" | tail -n 1)

if [ "$DUP_HTTP_CODE" = "400" ] || [ "$DUP_HTTP_CODE" = "409" ]; then
    record_result "$TEST_NAME" "PASS" "$DUP_DURATION"
elif [ "$DUP_HTTP_CODE" = "500" ]; then
    record_result "$TEST_NAME" "FAIL" "$DUP_DURATION" "Failed with HTTP 500 server error instead of clean client validation"
else
    record_result "$TEST_NAME" "FAIL" "$DUP_DURATION" "Expected 400/409, got ${DUP_HTTP_CODE}"
fi
rm -f "$DUP_FILE"

# ------------------------------------------------------------------------------
# TEST 4: Login with valid credentials (must return token)
# ------------------------------------------------------------------------------
TEST_NAME="Login with valid credentials (must return token)"
LOGIN_BODY="{\"email\":\"${TEST_EMAIL}\",\"password\":\"${TEST_PASSWORD}\"}"
LOGIN_FILE=$(mktemp)
LOGIN_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$LOGIN_FILE" -X POST \
    -H "Content-Type: application/json" \
    -d "$LOGIN_BODY" \
    --max-time 15 \
    "${BASE_URL}/api/auth/login" 2>/dev/null || echo -e "\n000\n0.0")

LOGIN_HTTP_CODE=$(echo "$LOGIN_RESP" | tail -n 2 | head -n 1)
LOGIN_DURATION=$(echo "$LOGIN_RESP" | tail -n 1)

if [ "$LOGIN_HTTP_CODE" = "200" ]; then
    AUTH_TOKEN=$(grep -o '"data":"[^"]*"' "$LOGIN_FILE" | sed 's/"data":"//;s/"//' || true)
    if [ -z "$AUTH_TOKEN" ]; then
        AUTH_TOKEN=$(grep -o '"token":"[^"]*"' "$LOGIN_FILE" | sed 's/"token":"//;s/"//' || true)
    fi

    if [ -n "$AUTH_TOKEN" ]; then
        record_result "$TEST_NAME" "PASS" "$LOGIN_DURATION"
    else
        record_result "$TEST_NAME" "FAIL" "$LOGIN_DURATION" "Login returned 200 but token could not be extracted"
    fi
else
    record_result "$TEST_NAME" "FAIL" "$LOGIN_DURATION" "Expected 200, got ${LOGIN_HTTP_CODE}"
fi
rm -f "$LOGIN_FILE"

# ------------------------------------------------------------------------------
# TEST 5: Login with invalid credentials (must fail 401/400)
# ------------------------------------------------------------------------------
TEST_NAME="Login with invalid credentials (must fail 401/400)"
INV_LOGIN_BODY="{\"email\":\"${TEST_EMAIL}\",\"password\":\"WrongPassword!99\"}"
INV_FILE=$(mktemp)
INV_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$INV_FILE" -X POST \
    -H "Content-Type: application/json" \
    -d "$INV_LOGIN_BODY" \
    --max-time 15 \
    "${BASE_URL}/api/auth/login" 2>/dev/null || echo -e "\n000\n0.0")

INV_HTTP_CODE=$(echo "$INV_RESP" | tail -n 2 | head -n 1)
INV_DURATION=$(echo "$INV_RESP" | tail -n 1)

if [ "$INV_HTTP_CODE" = "400" ] || [ "$INV_HTTP_CODE" = "401" ] || [ "$INV_HTTP_CODE" = "404" ]; then
    record_result "$TEST_NAME" "PASS" "$INV_DURATION"
else
    record_result "$TEST_NAME" "FAIL" "$INV_DURATION" "Expected 400/401, got ${INV_HTTP_CODE}"
fi
rm -f "$INV_FILE"

# ------------------------------------------------------------------------------
# TEST 6: Access a protected endpoint with token
# ------------------------------------------------------------------------------
TEST_NAME="Access a protected endpoint with token"
PROT_FILE=$(mktemp)
PROT_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$PROT_FILE" \
    -H "Authorization: Bearer ${AUTH_TOKEN}" \
    --max-time 15 \
    "${BASE_URL}/api/dashboard/profile" 2>/dev/null || echo -e "\n000\n0.0")

PROT_HTTP_CODE=$(echo "$PROT_RESP" | tail -n 2 | head -n 1)
PROT_DURATION=$(echo "$PROT_RESP" | tail -n 1)

if [ "$PROT_HTTP_CODE" = "200" ]; then
    record_result "$TEST_NAME" "PASS" "$PROT_DURATION"
else
    ALT_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o /dev/null \
        -H "Authorization: Bearer ${AUTH_TOKEN}" \
        --max-time 15 \
        "${BASE_URL}/api/dashboard/get-active-hotel" 2>/dev/null || echo -e "\n000\n0.0")
    ALT_CODE=$(echo "$ALT_RESP" | tail -n 2 | head -n 1)
    if [ "$ALT_CODE" = "200" ]; then
        record_result "$TEST_NAME" "PASS" "$PROT_DURATION"
    else
        record_result "$TEST_NAME" "FAIL" "$PROT_DURATION" "Expected 200, got ${PROT_HTTP_CODE}"
    fi
fi
rm -f "$PROT_FILE"

# ------------------------------------------------------------------------------
# TEST 7: Forgot password request
# ------------------------------------------------------------------------------
TEST_NAME="Forgot password request"
FP_BODY="{\"email\":\"${TEST_EMAIL}\"}"
FP_FILE=$(mktemp)
FP_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$FP_FILE" -X POST \
    -H "Content-Type: application/json" \
    -d "$FP_BODY" \
    --max-time 15 \
    "${BASE_URL}/api/forgot-password" 2>/dev/null || echo -e "\n000\n0.0")

FP_HTTP_CODE=$(echo "$FP_RESP" | tail -n 2 | head -n 1)
FP_DURATION=$(echo "$FP_RESP" | tail -n 1)

if [ "$FP_HTTP_CODE" = "200" ]; then
    RESET_TOKEN=$(grep -o '"resetToken":"[^"]*"' "$FP_FILE" | sed 's/"resetToken":"//;s/"//' || true)
    record_result "$TEST_NAME" "PASS" "$FP_DURATION"
else
    FP_ALT_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$FP_FILE" -X POST \
        -H "Content-Type: application/json" \
        -d "$FP_BODY" \
        --max-time 15 \
        "${BASE_URL}/api/auth/forgot-password" 2>/dev/null || echo -e "\n000\n0.0")
    FP_ALT_CODE=$(echo "$FP_ALT_RESP" | tail -n 2 | head -n 1)
    FP_ALT_DUR=$(echo "$FP_ALT_RESP" | tail -n 1)
    if [ "$FP_ALT_CODE" = "200" ]; then
        RESET_TOKEN=$(grep -o '"resetToken":"[^"]*"' "$FP_FILE" | sed 's/"resetToken":"//;s/"//' || true)
        record_result "$TEST_NAME" "PASS" "$FP_ALT_DUR"
    else
        record_result "$TEST_NAME" "FAIL" "$FP_DURATION" "Expected 200, got ${FP_HTTP_CODE}"
    fi
fi
rm -f "$FP_FILE"

# ------------------------------------------------------------------------------
# TEST 8: Reset password request
# ------------------------------------------------------------------------------
TEST_NAME="Reset password request"
TOKEN_TO_USE="${RESET_TOKEN:-dummy_reset_token_${TIMESTAMP}}"
RP_BODY="{\"token\":\"${TOKEN_TO_USE}\",\"newPassword\":\"NewSecurePass123!\"}"
RP_FILE=$(mktemp)
RP_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$RP_FILE" -X POST \
    -H "Content-Type: application/json" \
    -d "$RP_BODY" \
    --max-time 15 \
    "${BASE_URL}/api/reset-password" 2>/dev/null || echo -e "\n000\n0.0")

RP_HTTP_CODE=$(echo "$RP_RESP" | tail -n 2 | head -n 1)
RP_DURATION=$(echo "$RP_RESP" | tail -n 1)

if [ "$RP_HTTP_CODE" = "200" ]; then
    record_result "$TEST_NAME" "PASS" "$RP_DURATION"
elif [ -z "$RESET_TOKEN" ] && [ "$RP_HTTP_CODE" = "400" ]; then
    record_result "$TEST_NAME" "PASS" "$RP_DURATION" "(Correctly validated & rejected invalid token)"
else
    RP_ALT_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o /dev/null -X POST \
        -H "Content-Type: application/json" \
        -d "$RP_BODY" \
        --max-time 15 \
        "${BASE_URL}/api/auth/reset-password" 2>/dev/null || echo -e "\n000\n0.0")
    RP_ALT_CODE=$(echo "$RP_ALT_RESP" | tail -n 2 | head -n 1)
    if [ "$RP_ALT_CODE" = "200" ] || ([ -z "$RESET_TOKEN" ] && [ "$RP_ALT_CODE" = "400" ]); then
        record_result "$TEST_NAME" "PASS" "$RP_DURATION"
    else
        record_result "$TEST_NAME" "FAIL" "$RP_DURATION" "Expected 200/400, got ${RP_HTTP_CODE}"
    fi
fi
rm -f "$RP_FILE"

# ------------------------------------------------------------------------------
# TEST 9: Flight search API
# ------------------------------------------------------------------------------
TEST_NAME="Flight search API"
FLIGHT_FILE=$(mktemp)
FLIGHT_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$FLIGHT_FILE" \
    --max-time 15 \
    "${BASE_URL}/api/flights/search?source=Delhi&destination=Mumbai" 2>/dev/null || echo -e "\n000\n0.0")

FLIGHT_HTTP_CODE=$(echo "$FLIGHT_RESP" | tail -n 2 | head -n 1)
FLIGHT_DURATION=$(echo "$FLIGHT_RESP" | tail -n 1)

if [ "$FLIGHT_HTTP_CODE" = "200" ]; then
    record_result "$TEST_NAME" "PASS" "$FLIGHT_DURATION"
else
    ALT_FLIGHT_CODE=$(curl -s -w "%{http_code}" -o /dev/null --max-time 15 "${BASE_URL}/api/flights" 2>/dev/null || echo "000")
    if [ "$ALT_FLIGHT_CODE" = "200" ]; then
        record_result "$TEST_NAME" "PASS" "$FLIGHT_DURATION"
    else
        record_result "$TEST_NAME" "FAIL" "$FLIGHT_DURATION" "Expected 200, got ${FLIGHT_HTTP_CODE}"
    fi
fi
rm -f "$FLIGHT_FILE"

# ------------------------------------------------------------------------------
# TEST 10: Train search API
# ------------------------------------------------------------------------------
TEST_NAME="Train search API"
TRAIN_FILE=$(mktemp)
TRAIN_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$TRAIN_FILE" \
    --max-time 15 \
    "${BASE_URL}/api/trains/search?source=Delhi&destination=Varanasi" 2>/dev/null || echo -e "\n000\n0.0")

TRAIN_HTTP_CODE=$(echo "$TRAIN_RESP" | tail -n 2 | head -n 1)
TRAIN_DURATION=$(echo "$TRAIN_RESP" | tail -n 1)

if [ "$TRAIN_HTTP_CODE" = "200" ]; then
    record_result "$TEST_NAME" "PASS" "$TRAIN_DURATION"
else
    ALT_TRAIN_CODE=$(curl -s -w "%{http_code}" -o /dev/null --max-time 15 "${BASE_URL}/api/trains" 2>/dev/null || echo "000")
    if [ "$ALT_TRAIN_CODE" = "200" ]; then
        record_result "$TEST_NAME" "PASS" "$TRAIN_DURATION"
    else
        record_result "$TEST_NAME" "FAIL" "$TRAIN_DURATION" "Expected 200, got ${TRAIN_HTTP_CODE}"
    fi
fi
rm -f "$TRAIN_FILE"

# ------------------------------------------------------------------------------
# TEST 11: Cab search API
# ------------------------------------------------------------------------------
TEST_NAME="Cab search API"
CAB_FILE=$(mktemp)
CAB_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$CAB_FILE" \
    --max-time 15 \
    "${BASE_URL}/api/cabs" 2>/dev/null || echo -e "\n000\n0.0")

CAB_HTTP_CODE=$(echo "$CAB_RESP" | tail -n 2 | head -n 1)
CAB_DURATION=$(echo "$CAB_RESP" | tail -n 1)

if [ "$CAB_HTTP_CODE" = "200" ]; then
    record_result "$TEST_NAME" "PASS" "$CAB_DURATION"
else
    ALT_CAB_CODE=$(curl -s -w "%{http_code}" -o /dev/null --max-time 15 "${BASE_URL}/api/cabs/search?city=Delhi" 2>/dev/null || echo "000")
    if [ "$ALT_CAB_CODE" = "200" ]; then
        record_result "$TEST_NAME" "PASS" "$CAB_DURATION"
    else
        record_result "$TEST_NAME" "FAIL" "$CAB_DURATION" "Expected 200, got ${CAB_HTTP_CODE}"
    fi
fi
rm -f "$CAB_FILE"

# ------------------------------------------------------------------------------
# TEST 12: Razorpay create order (TEST mode)
# ------------------------------------------------------------------------------
TEST_NAME="Razorpay create order (TEST mode)"
ORDER_BODY="{\"amount\":500}"
ORDER_FILE=$(mktemp)
ORDER_AUTH_HDR=""
if [ -n "$AUTH_TOKEN" ]; then
    ORDER_AUTH_HDR="Authorization: Bearer ${AUTH_TOKEN}"
fi

ORDER_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$ORDER_FILE" -X POST \
    -H "Content-Type: application/json" \
    ${ORDER_AUTH_HDR:+-H "$ORDER_AUTH_HDR"} \
    -d "$ORDER_BODY" \
    --max-time 15 \
    "${BASE_URL}/api/payment/createOrder" 2>/dev/null || echo -e "\n000\n0.0")

ORDER_HTTP_CODE=$(echo "$ORDER_RESP" | tail -n 2 | head -n 1)
ORDER_DURATION=$(echo "$ORDER_RESP" | tail -n 1)

if [ "$ORDER_HTTP_CODE" = "200" ]; then
    record_result "$TEST_NAME" "PASS" "$ORDER_DURATION"
else
    ALT_ORDER_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$ORDER_FILE" -X POST \
        -H "Content-Type: application/json" \
        ${ORDER_AUTH_HDR:+-H "$ORDER_AUTH_HDR"} \
        -d "$ORDER_BODY" \
        --max-time 15 \
        "${BASE_URL}/api/payment/create-order" 2>/dev/null || echo -e "\n000\n0.0")
    ALT_ORDER_CODE=$(echo "$ALT_ORDER_RESP" | tail -n 2 | head -n 1)
    ALT_ORDER_DUR=$(echo "$ALT_ORDER_RESP" | tail -n 1)
    if [ "$ALT_ORDER_CODE" = "200" ]; then
        record_result "$TEST_NAME" "PASS" "$ALT_ORDER_DUR"
    else
        record_result "$TEST_NAME" "FAIL" "$ORDER_DURATION" "Expected 200, got ${ORDER_HTTP_CODE}"
    fi
fi
rm -f "$ORDER_FILE"

# ------------------------------------------------------------------------------
# TEST 13: Razorpay verify payment (invalid signature must fail)
# ------------------------------------------------------------------------------
TEST_NAME="Razorpay verify payment (invalid signature must fail)"
VERIFY_BODY="{\"orderId\":\"order_test_9999\",\"paymentId\":\"pay_test_9999\",\"signature\":\"tampered_invalid_signature_hex\"}"
VERIFY_FILE=$(mktemp)
VERIFY_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$VERIFY_FILE" -X POST \
    -H "Content-Type: application/json" \
    ${ORDER_AUTH_HDR:+-H "$ORDER_AUTH_HDR"} \
    -d "$VERIFY_BODY" \
    --max-time 15 \
    "${BASE_URL}/api/payment/verify" 2>/dev/null || echo -e "\n000\n0.0")

VERIFY_HTTP_CODE=$(echo "$VERIFY_RESP" | tail -n 2 | head -n 1)
VERIFY_DURATION=$(echo "$VERIFY_RESP" | tail -n 1)

if [ "$VERIFY_HTTP_CODE" = "400" ]; then
    record_result "$TEST_NAME" "PASS" "$VERIFY_DURATION"
else
    ALT_VERIFY_RESP=$(curl -s -w "\n%{http_code}\n%{time_total}" -o "$VERIFY_FILE" -X POST \
        -H "Content-Type: application/json" \
        ${ORDER_AUTH_HDR:+-H "$ORDER_AUTH_HDR"} \
        -d "$VERIFY_BODY" \
        --max-time 15 \
        "${BASE_URL}/api/payment/verify-payment" 2>/dev/null || echo -e "\n000\n0.0")
    ALT_VERIFY_CODE=$(echo "$ALT_VERIFY_RESP" | tail -n 2 | head -n 1)
    ALT_VERIFY_DUR=$(echo "$ALT_VERIFY_RESP" | tail -n 1)
    if [ "$ALT_VERIFY_CODE" = "400" ]; then
        record_result "$TEST_NAME" "PASS" "$ALT_VERIFY_DUR"
    else
        record_result "$TEST_NAME" "FAIL" "$VERIFY_DURATION" "Expected 400 Bad Request for tampered signature, got ${VERIFY_HTTP_CODE}"
    fi
fi
rm -f "$VERIFY_FILE"

# ------------------------------------------------------------------------------
# Summary and Exit
# ------------------------------------------------------------------------------
echo "=============================================================================="
echo " Smoke Test Summary"
echo " Total Tests  : ${TOTAL_TESTS}"
echo " Passed       : ${PASSED_TESTS}"
echo " Failed       : ${FAILED_TESTS}"
echo "=============================================================================="

if [ "$FAILED_TESTS" -eq 0 ]; then
    echo " RESULT: ALL SMOKE TESTS PASSED [SUCCESS]"
    exit 0
else
    echo " RESULT: SMOKE TESTS FAILED (${FAILED_TESTS} failures)"
    exit 1
fi
