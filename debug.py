import requests
import json
from datetime import datetime

BASE_URL = "http://localhost:8080"

# Global variables to store test data
test_user = {
    "username": "dat.nguyen1206@hcmut.edu.vn",
    "email": "dat.nguyen1206@hcmut.edu.vn",
    "password": "testpassword123",
    "new_password": "newpassword456"
}
jwt_token = None


def print_separator(title=""):
    """Print a separator line with optional title"""
    print("\n" + "=" * 70)
    if title:
        print(f"  {title}")
        print("=" * 70)


def print_response(response, test_name, request_body=None):
    """Print response details in a formatted way"""
    print(f"\n[{test_name}]")
    print(f"URL: {response.request.method} {response.url}")
    
    # Print request headers (excluding sensitive ones)
    print(f"\nRequest Headers:")
    headers = dict(response.request.headers)
    if 'Authorization' in headers:
        token = headers['Authorization']
        headers['Authorization'] = f"{token[:20]}...{token[-10:]}" if len(token) > 30 else token
    print(f"  {json.dumps(headers, indent=2)}")
    
    # Print request body
    if request_body:
        print(f"\nRequest Body:")
        print(f"  {json.dumps(request_body, indent=2)}")
    elif response.request.body:
        try:
            body = json.loads(response.request.body)
            print(f"\nRequest Body:")
            print(f"  {json.dumps(body, indent=2)}")
        except:
            print(f"\nRequest Body: {response.request.body}")
    
    # Print response
    print(f"\nStatus Code: {response.status_code}")
    print(f"\nResponse Body:")
    try:
        print(f"  {json.dumps(response.json(), indent=2)}")
    except:
        print(f"  {response.text}")
    
    # Determine if test passed
    if 200 <= response.status_code < 300:
        print("✅ PASSED")
    else:
        print("❌ FAILED")
    return response


# ============================================================================
# Public Endpoints Tests
# ============================================================================

def test_welcome():
    """Test GET / - Welcome endpoint"""
    print_separator("PUBLIC ENDPOINTS")
    response = requests.get(f"{BASE_URL}/")
    return print_response(response, "GET / - Welcome")


def test_health():
    """Test GET /health - Health check endpoint"""
    response = requests.get(f"{BASE_URL}/health")
    return print_response(response, "GET /health - Health Check")


# ============================================================================
# Authentication Tests
# ============================================================================

def test_register():
    """Test POST /auth/register - Register new user"""
    print_separator("AUTHENTICATION TESTS")
    response = requests.post(
        f"{BASE_URL}/auth/register",
        json={
            "username": test_user["username"],
            "email": test_user["email"],
            "password": test_user["password"]
        }
    )
    return print_response(response, "POST /auth/register - Register User")


def test_register_duplicate():
    """Test POST /auth/register - Register with duplicate username (should fail)"""
    response = requests.post(
        f"{BASE_URL}/auth/register",
        json={
            "username": test_user["username"],
            "email": f"another_{test_user['email']}",
            "password": test_user["password"]
        }
    )
    return print_response(response, "POST /auth/register - Duplicate Username (Expected Failure)")


def test_login():
    """Test POST /auth/login - Login with valid credentials"""
    global jwt_token
    response = requests.post(
        f"{BASE_URL}/auth/login",
        json={
            "username": test_user["username"],
            "password": test_user["password"]
        }
    )
    
    if response.status_code == 200:
        jwt_token = response.json().get("token")
        print(f"🔑 JWT Token stored: {jwt_token[:50]}...")
    
    return print_response(response, "POST /auth/login - Login")


def test_login_invalid():
    """Test POST /auth/login - Login with invalid credentials (should fail)"""
    response = requests.post(
        f"{BASE_URL}/auth/login",
        json={
            "username": test_user["username"],
            "password": "wrongpassword"
        }
    )
    return print_response(response, "POST /auth/login - Invalid Credentials (Expected Failure)")


# ============================================================================
# Password Management Tests
# ============================================================================

def test_change_password():
    """Test POST /auth/change-password - Change password with authentication"""
    print_separator("PASSWORD MANAGEMENT TESTS")
    
    if not jwt_token:
        print("❌ Skipping: No JWT token available. Run test_login() first.")
        return None
    
    response = requests.post(
        f"{BASE_URL}/auth/change-password",
        headers={"Authorization": f"Bearer {jwt_token}"},
        json={
            "oldPassword": test_user["password"],
            "newPassword": test_user["new_password"]
        }
    )
    
    # Update the password if successful
    if response.status_code == 200:
        test_user["password"] = test_user["new_password"]
    
    return print_response(response, "POST /auth/change-password - Change Password")


def test_change_password_wrong_old():
    """Test POST /auth/change-password - Change password with wrong old password (should fail)"""
    if not jwt_token:
        print("❌ Skipping: No JWT token available. Run test_login() first.")
        return None
    
    response = requests.post(
        f"{BASE_URL}/auth/change-password",
        headers={"Authorization": f"Bearer {jwt_token}"},
        json={
            "oldPassword": "wrongoldpassword",
            "newPassword": "anothernewpassword"
        }
    )
    return print_response(response, "POST /auth/change-password - Wrong Old Password (Expected Failure)")


def test_change_password_no_auth():
    """Test POST /auth/change-password - Change password without authentication (should fail)"""
    response = requests.post(
        f"{BASE_URL}/auth/change-password",
        json={
            "oldPassword": test_user["password"],
            "newPassword": "anothernewpassword"
        }
    )
    return print_response(response, "POST /auth/change-password - No Auth (Expected Failure)")


def test_forgot_password():
    """Test POST /auth/forgot-password - Request password reset"""
    response = requests.post(
        f"{BASE_URL}/auth/forgot-password",
        json={"email": test_user["email"]}
    )
    print("📧 Check the email inbox for reset token")
    return print_response(response, "POST /auth/forgot-password - Request Reset")


def test_forgot_password_invalid_email():
    """Test POST /auth/forgot-password - Request with non-existent email"""
    response = requests.post(
        f"{BASE_URL}/auth/forgot-password",
        json={"email": "nonexistent@example.com"}
    )
    return print_response(response, "POST /auth/forgot-password - Non-existent Email")


def test_reset_password(token=None):
    """Test POST /auth/reset-password - Reset password with token"""
    if not token:
        print("❌ Skipping: No reset token provided")
        print("   Run test_forgot_password() first and get the token from email")
        return None
    
    response = requests.post(
        f"{BASE_URL}/auth/reset-password",
        json={
            "token": token,
            "newPassword": "resetpassword123"
        }
    )
    return print_response(response, "POST /auth/reset-password - Reset with Token")


def test_reset_password_invalid_token():
    """Test POST /auth/reset-password - Reset with invalid token (should fail)"""
    response = requests.post(
        f"{BASE_URL}/auth/reset-password",
        json={
            "token": "invalid-token-12345",
            "newPassword": "resetpassword123"
        }
    )
    return print_response(response, "POST /auth/reset-password - Invalid Token (Expected Failure)")


# ============================================================================
# Test Suite Runner
# ============================================================================

def run_all_tests():
    """Run all API tests in sequence"""
    print_separator("🚀 STARTING API TEST SUITE")
    print(f"Base URL: {BASE_URL}")
    print(f"Test User: {test_user['username']}")
    print(f"Test Email: {test_user['email']}")
    
    # Public endpoints
    test_welcome()
    test_health()
    
    # Authentication
    test_register()
    test_register_duplicate()
    test_login()
    test_login_invalid()
    
    # Password management
    test_change_password()
    test_change_password_wrong_old()
    test_change_password_no_auth()
    test_forgot_password()
    test_forgot_password_invalid_email()
    test_reset_password_invalid_token()
    
    print_separator("✅ TEST SUITE COMPLETED")
    print("\n📝 Note: To test reset_password with valid token:")
    print("   1. Check email inbox for the reset token")
    print("   2. Run: test_reset_password('your-token-here')")


def run_basic_flow():
    """Run basic authentication flow (register -> login -> change password)"""
    print_separator("🔄 RUNNING BASIC FLOW TEST")
    
    test_welcome()
    test_register()
    test_login()
    test_change_password()
    
    print_separator("✅ BASIC FLOW COMPLETED")


def run_password_reset_flow(email="sliderboo2005@gmail.com"):
    """Run complete password reset flow"""
    print_separator("🔄 RUNNING PASSWORD RESET FLOW")
    
    # Request reset
    response = requests.post(
        f"{BASE_URL}/auth/forgot-password",
        json={"email": email}
    )
    print_response(response, "Request Password Reset")
    
    print("\n📧 Check your email for the reset token")
    print("   Then call: complete_password_reset('your-token', 'new-password')")
    
    print_separator("⏸️  PASSWORD RESET FLOW PAUSED")


def complete_password_reset(token, new_password):
    """Complete password reset with token from email"""
    print_separator("🔄 COMPLETING PASSWORD RESET")
    
    response = requests.post(
        f"{BASE_URL}/auth/reset-password",
        json={
            "token": token,
            "newPassword": new_password
        }
    )
    print_response(response, "Reset Password with Token")
    
    print_separator("✅ PASSWORD RESET COMPLETED")


# ============================================================================
# Interactive Menu
# ============================================================================

def show_menu():
    """Display interactive test menu"""
    print("\n" + "=" * 70)
    print("  ROORS API TEST SUITE")
    print("=" * 70)
    print("\n📋 Available Test Functions:")
    print("\n  Full Test Suites:")
    print("    run_all_tests()              - Run all API tests")
    print("    run_basic_flow()             - Run basic auth flow")
    print("    run_password_reset_flow()    - Run password reset flow")
    print("\n  Public Endpoints:")
    print("    test_welcome()               - GET /")
    print("    test_health()                - GET /health")
    print("\n  Authentication:")
    print("    test_register()              - POST /auth/register")
    print("    test_login()                 - POST /auth/login")
    print("\n  Password Management:")
    print("    test_change_password()       - POST /auth/change-password")
    print("    test_forgot_password()       - POST /auth/forgot-password")
    print("    test_reset_password(token)   - POST /auth/reset-password")
    print("\n  Utilities:")
    print("    show_menu()                  - Show this menu")
    print("=" * 70)
    print("\n💡 Quick Start: run_all_tests()")


# ============================================================================
# Main Entry Point
# ============================================================================

if __name__ == "__main__":
    # show_menu()
    # print("\n🎯 Running all tests automatically...\n")
    run_all_tests()

    # test_reset_password("6ef87a6d-c147-43f2-967d-5f433a807660")
