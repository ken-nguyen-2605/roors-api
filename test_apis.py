import requests
import json
import sys

# Configuration
BASE_URL = "http://localhost:8080"
USERNAME = "username"
PASSWORD = "password"
EMAIL = "dat.nguyen1206@hcmut.edu.vn"

# Global variables
token = None
headers = {}

def login():
    global token, headers
    url = f"{BASE_URL}/api/auth/login"
    data = {
        "username": USERNAME,
        "password": PASSWORD
    }
    try:
        response = requests.post(url, json=data)
        if response.status_code == 200:
            result = response.json()
            token = result.get("token")
            if token:
                headers = {"Authorization": f"Bearer {token}"}
                print("✓ Login successful")
                return True
            else:
                print("✗ Login failed: No token in response")
                return False
        else:
            print(f"✗ Login failed: {response.status_code} - {response.text}")
            return False
    except Exception as e:
        print(f"✗ Login error: {e}")
        return False

def test_endpoint(method, url, data=None, expected_status=None, description="", print_response=False):
    try:
        if method.upper() == "GET":
            response = requests.get(url, headers=headers)
        elif method.upper() == "POST":
            response = requests.post(url, json=data, headers=headers)
        elif method.upper() == "PUT":
            response = requests.put(url, json=data, headers=headers)
        elif method.upper() == "PATCH":
            response = requests.patch(url, json=data, headers=headers)
        elif method.upper() == "DELETE":
            response = requests.delete(url, headers=headers)
        else:
            print(f"✗ Unsupported method: {method}")
            return False

        status = "✓" if expected_status is None or response.status_code == expected_status else "✗"
        print(f"{status} {method.upper()} {url} - {response.status_code} {description}")
        
        if print_response and response.status_code < 400:
            try:
                json_data = response.json()
                if isinstance(json_data, list):
                    print(f"    Returned {len(json_data)} items")
                elif isinstance(json_data, dict):
                    if 'content' in json_data and 'totalElements' in json_data:
                        print(f"    Returned page with {len(json_data.get('content', []))} items, total: {json_data.get('totalElements', 'unknown')}")
                    elif 'totalElements' in json_data:
                        print(f"    Total elements: {json_data.get('totalElements')}")
                    else:
                        print(f"    Response keys: {list(json_data.keys())}")
                else:
                    print(f"    Response: {str(json_data)[:100]}...")
            except:
                print(f"    Response: {response.text[:100]}...")
        
        if response.status_code >= 400:
            print(f"    Error: {response.text[:200]}...")
        return response.status_code < 400
    except Exception as e:
        print(f"✗ {method.upper()} {url} - Error: {e}")
        return False

def main():
    print("Starting API Tests for Roors API")
    print("=" * 50)

    # Test public endpoints first
    print("\n1. Testing Public Endpoints")
    print("-" * 30)

    test_endpoint("GET", f"{BASE_URL}/", description="Welcome page")
    test_endpoint("GET", f"{BASE_URL}/health", description="Health check")

    # Menu endpoints (public)
    print("\n2. Testing Menu Endpoints (Public)")
    print("-" * 30)

    test_endpoint("GET", f"{BASE_URL}/api/categories", description="Get all categories", print_response=True)
    test_endpoint("GET", f"{BASE_URL}/api/categories/active", description="Get active categories", print_response=True)
    test_endpoint("GET", f"{BASE_URL}/api/menu", description="Get all menu items", print_response=True)
    test_endpoint("GET", f"{BASE_URL}/api/menu/featured", description="Get featured items", print_response=True)
    test_endpoint("GET", f"{BASE_URL}/api/menu/top-rated", description="Get top rated items", print_response=True)
    test_endpoint("GET", f"{BASE_URL}/api/menu/popular", description="Get popular items", print_response=True)
    test_endpoint("GET", f"{BASE_URL}/api/menu/search?keyword=pizza", description="Search menu items", print_response=True)
    test_endpoint("GET", f"{BASE_URL}/api/menu/filter/price?minPrice=10&maxPrice=50", description="Filter by price", print_response=True)

    # Payment methods (public)
    test_endpoint("GET", f"{BASE_URL}/api/payments/methods", description="Get payment methods", print_response=True)

    # Login
    print("\n3. Logging in...")
    print("-" * 30)
    if not login():
        print("Cannot proceed with authenticated tests")
        return

    # Test authenticated endpoints
    print("\n4. Testing Authenticated Endpoints")
    print("-" * 30)

    # Auth endpoints
    test_endpoint("GET", f"{BASE_URL}/api/auth/me", description="Get current user", print_response=True)

    # Get current user info for user endpoints
    try:
        response = requests.get(f"{BASE_URL}/api/auth/me", headers=headers)
        if response.status_code == 200:
            user_data = response.json()
            user_id = user_data.get("id")
            print(f"Current user ID: {user_id}")
        else:
            user_id = None
    except:
        user_id = None

    if user_id:
        test_endpoint("GET", f"{BASE_URL}/api/users/{user_id}", description="Get user profile", print_response=True)

    # Get some menu data for testing
    try:
        response = requests.get(f"{BASE_URL}/api/menu?page=0&size=5")
        if response.status_code == 200:
            menu_data = response.json()
            menu_items = menu_data.get("content", [])
            if menu_items:
                menu_id = menu_items[0].get("id")
                print(f"Sample menu item ID: {menu_id}")
            else:
                menu_id = None
        else:
            menu_id = None
    except:
        menu_id = None

    if menu_id:
        test_endpoint("GET", f"{BASE_URL}/api/menu/{menu_id}", description="Get menu item by ID", print_response=True)

    # Get category data
    try:
        response = requests.get(f"{BASE_URL}/api/categories")
        if response.status_code == 200:
            categories = response.json()
            if categories:
                category_id = categories[0].get("id")
                print(f"Sample category ID: {category_id}")
            else:
                category_id = None
        else:
            category_id = None
    except:
        category_id = None

    if category_id:
        test_endpoint("GET", f"{BASE_URL}/api/categories/{category_id}", description="Get category by ID", print_response=True)
        test_endpoint("GET", f"{BASE_URL}/api/menu/category/{category_id}", description="Get menu by category", print_response=True)

    # Orders
    test_endpoint("GET", f"{BASE_URL}/api/orders", description="Get user orders", print_response=True)

    # Admin endpoints (may fail if not admin)
    print("\n5. Testing Admin Endpoints")
    print("-" * 30)
    test_endpoint("GET", f"{BASE_URL}/admin/logs", description="Get logs (admin only)")
    test_endpoint("GET", f"{BASE_URL}/api/admin/statistics/dashboard", description="Get dashboard stats (admin only)")

    print("\n" + "=" * 50)
    print("API Testing completed!")

if __name__ == "__main__":
    main()