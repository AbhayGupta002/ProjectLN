#!/usr/bin/env python3
"""
NEXTGEM-TECHNOLOGY: Comprehensive API Automation Test Suite
Zero-dependency test runner using Python's standard library (urllib).
"""

import urllib.request
import urllib.error
import json
import time
import sys

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
PASSED = 0
FAILED = 0
TOTAL = 0

def test(method, endpoint, expected_codes, data=None, token=None, title=""):
    global PASSED, FAILED, TOTAL
    TOTAL += 1
    url = f"{BASE_URL}{endpoint}"
    print(f"[{TOTAL:02d}] {title} ({method} {endpoint})... ", end="", flush=True)

    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    body_bytes = json.dumps(data).encode("utf-8") if data is not None else None

    req = urllib.request.Request(url, data=body_bytes, headers=headers, method=method)
    status_code = None
    response_body = ""

    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            status_code = resp.getcode()
            response_body = resp.read().decode("utf-8", errors="ignore")
    except urllib.error.HTTPError as e:
        status_code = e.code
        response_body = e.read().decode("utf-8", errors="ignore")
    except Exception as ex:
        print(f"\033[91mFAILED: Connection error ({ex})\033[0m")
        FAILED += 1
        return None, ""

    expected_list = expected_codes if isinstance(expected_codes, list) else [expected_codes]
    if status_code in expected_list:
        print(f"\033[92mPASSED (HTTP {status_code})\033[0m")
        PASSED += 1
        return status_code, response_body
    else:
        print(f"\033[91mFAILED (Expected {expected_list}, Got {status_code})\033[0m")
        print(f"    Response snippet: {response_body[:150]}")
        FAILED += 1
        return status_code, response_body

def main():
    print("=" * 70)
    print("      NEXTGEM-TECHNOLOGY API AUTOMATION TEST SUITE")
    print(f"      Target Host: {BASE_URL}")
    print("=" * 70)

    # 1. Health & Actuator
    test("GET", "/actuator/health", [200], title="Actuator Health Check")
    test("GET", "/actuator/info", [200], title="Actuator Info Check")

    # 2. Public Catalogs
    test("GET", "/api/flights", [200], title="Public Flights Search")
    test("GET", "/api/trains", [200], title="Public Trains Search")
    test("GET", "/api/bus", [200], title="Public Buses Search")
    test("GET", "/api/cabs", [200], title="Public Cabs Search")
    test("GET", "/api/tours", [200, 404], title="Public Tours Search")

    # 3. AI Smart Assistant
    test("POST", "/api/chat/ask", [200],
         data={"message": "I need help with travel", "sessionId": "py-test-session"},
         title="AI Conversational Chatbot")

    test("POST", "/api/ai/prompt", [200],
         data={"prompt": "Plan 3 days trip to Delhi with budget of 25000"},
         title="AI Smart Trip Planner Orchestrator")

    # 4. Authentication, Lockout & 2FA
    test_email = f"py_suite_{int(time.time())}@gmail.com"
    test_pwd = "SuitePassword123!"

    test("POST", "/api/auth/register", [200, 400],
         data={"name": "Python Tester", "email": test_email, "mobile": "9998887776", "password": test_pwd},
         title="User Registration")

    # 4 consecutive wrong password attempts -> account lockout
    test("POST", "/api/auth/login", [400], data={"email": test_email, "password": "wrong1"}, title="Failed Login Attempt 1")
    test("POST", "/api/auth/login", [400], data={"email": test_email, "password": "wrong2"}, title="Failed Login Attempt 2")
    test("POST", "/api/auth/login", [400], data={"email": test_email, "password": "wrong3"}, title="Failed Login Attempt 3")
    test("POST", "/api/auth/login", [423], data={"email": test_email, "password": "wrong4"}, title="Failed Login Attempt 4 (LOCKOUT)")
    test("POST", "/api/auth/login", [423], data={"email": test_email, "password": test_pwd}, title="Post-Lockout Blocked Login")

    # 5. Forgot Password & Unlock
    _, forgot_resp = test("POST", "/api/auth/forgot-password", [200], data={"email": test_email}, title="Forgot Password Trigger")

    token = None
    if "Reset token generated:" in forgot_resp:
        token = forgot_resp.split("Reset token generated:")[1].strip()

    if token:
        test("POST", "/api/auth/reset-password", [200],
             data={"token": token, "newPassword": "NewPassword456!"},
             title="Reset Password & Account Unlock")

        test("POST", "/api/auth/login", [200],
             data={"email": test_email, "password": "NewPassword456!"},
             title="Login with New Password (2FA Trigger)")

    print("=" * 70)
    print(f"Total Tests: {TOTAL} | Passed: {PASSED} | Failed: {FAILED}")
    if FAILED == 0:
        print("\033[92mSUCCESS: ALL APIS VERIFIED AND OPERATIONAL!\033[0m")
    else:
        print("\033[91mFAILURES DETECTED: PLEASE REVIEW API RESPONSES.\033[0m")
    print("=" * 70)

if __name__ == "__main__":
    main()
