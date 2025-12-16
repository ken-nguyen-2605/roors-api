# Roors API Documentation

This document provides detailed documentation for all available endpoints in the Roors API.

---

## Table of Contents

1.  [Authentication](#authentication)
2.  [Users](#users)
3.  [Categories](#categories)
4.  [Menu Items](#menu-items)
5.  [Orders](#orders)
6.  [Payments](#payments)
7.  [Admin](#admin)
8.  [General](#general)

---

## Authentication

Base Path: `/api/auth`

All endpoints related to user authentication and account management.

### 1. Register User

- **Method**: `POST`
- **Endpoint**: `/api/auth/register`
- **Description**: Registers a new user account. An email verification link will be sent.
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }
  ```
- **Success Response (201 Created)**:
  ```json
  {
    "username": "testuser",
    "email": "test@example.com",
    "message": "User registered successfully. Please check your email to verify your account."
  }
  ```
- **Error Response (400 Bad Request)**:
  ```json
  {
    "error": "Username is already taken",
    "status": 400
  }
  ```

### 2. Login User

- **Method**: `POST`
- **Endpoint**: `/api/auth/login`
- **Description**: Authenticates a user and returns a JWT token.
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "password": "password123"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "token": "jwt.token.string"
  }
  ```
- **Error Response (401 Unauthorized)**:
  ```json
  {
    "error": "Invalid username or password",
    "status": 401
  }
  ```
- **Error Response (403 Forbidden)**:
  ```json
  {
    "error": "Email not verified. Please verify your email before logging in.",
    "status": 403
  }
  ```

### 3. Change Password

- **Method**: `POST`
- **Endpoint**: `/api/auth/change-password`
- **Description**: Allows an authenticated user to change their password.
- **Authentication**: Requires JWT Bearer Token.
- **Request Body**:
  ```json
  {
    "oldPassword": "password123",
    "newPassword": "newPassword456"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "message": "Password changed successfully"
  }
  ```

### 4. Forgot Password

- **Method**: `POST`
- **Endpoint**: `/api/auth/forgot-password`
- **Description**: Initiates the password reset process for a user by sending a reset link to their email.
- **Request Body**:
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "message": "If the email exists, a password reset link has been sent"
  }
  ```

### 5. Reset Password

- **Method**: `POST`
- **Endpoint**: `/api/auth/reset-password`
- **Description**: Resets the user's password using a token from the forgot password email.
- **Request Body**:
  ```json
  {
    "token": "reset-token-from-email",
    "newPassword": "newSecurePassword123"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "message": "Password has been reset successfully"
  }
  ```

### 6. Resend Verification Email

- **Method**: `POST`
- **Endpoint**: `/api/auth/resend-verification`
- **Description**: Resends the account verification email.
- **Request Body**:
  ```json
  {
    "email": "unverified-user@example.com"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "message": "Verification email has been resent"
  }
  ```

### 7. Verify Email

- **Method**: `GET`
- **Endpoint**: `/api/auth/verify-email`
- **Description**: Verifies a user's email address using the token from the verification email.
- **Query Parameters**:
  - `token` (string, required): The verification token.
- **Success Response (200 OK)**:
  ```json
  {
    "message": "Email has been verified successfully"
  }
  ```

### 8. Get Current User

- **Method**: `GET`
- **Endpoint**: `/api/auth/me`
- **Description**: Retrieves the profile of the currently authenticated user.
- **Authentication**: Requires JWT Bearer Token.
- **Success Response (200 OK)**:
  ```json
  {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "verified": true
  }
  ```

### 9. Logout

- **Method**: `POST`
- **Endpoint**: `/api/auth/logout`
- **Description**: Logs out the user by clearing the security context. The client is responsible for deleting the JWT.
- **Authentication**: Requires JWT Bearer Token.
- **Success Response (200 OK)**:
  ```json
  {
    "message": "Logged out successfully"
  }
  ```

---

## Users

Base Path: `/api/users`

### 1. Get User Profile by ID

- **Method**: `GET`
- **Endpoint**: `/api/users/{id}`
- **Description**: Retrieves public profile information for a specific user.
- **Path Parameters**:
  - `id` (long, required): The ID of the user.
- **Success Response (200 OK)**:
  ```json
  {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "verified": true
  }
  ```

### 2. Update User Profile

- **Method**: `PATCH`
- **Endpoint**: `/api/users/{id}`
- **Description**: Updates a user's own profile information. The user is identified by the authentication token.
- **Authentication**: Requires JWT Bearer Token.
- **Path Parameters**:
  - `id` (long, required): The ID of the user to update. Must match the authenticated user's ID.
- **Request Body**:
  ```json
  {
    "email": "new-email@example.com"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "id": 1,
    "username": "testuser",
    "email": "new-email@example.com",
    "verified": false
  }
  ```

---

## Categories

Base Path: `/api/categories`

### 1. Get All Categories

- **Method**: `GET`
- **Endpoint**: `/api/categories`
- **Description**: Retrieves a list of all menu item categories.
- **Success Response (200 OK)**:
  ```json
  [
    {
      "id": 1,
      "name": "Appetizers",
      "slug": "appetizers",
      "description": "Delicious starters",
      "active": true
    }
  ]
  ```

### 2. Get Active Categories

- **Method**: `GET`
- **Endpoint**: `/api/categories/active`
- **Description**: Retrieves a list of all active (visible) categories.

### 3. Get Category by ID or Slug

- **Method**: `GET`
- **Endpoint**: `/api/categories/{id}` or `/api/categories/slug/{slug}`
- **Description**: Retrieves a single category by its ID or slug.

### 4. Create Category

- **Method**: `POST`
- **Endpoint**: `/api/categories`
- **Description**: Creates a new category. (Admin only)
- **Authentication**: Requires Admin role.
- **Request Body**:
  ```json
  {
    "name": "Desserts",
    "description": "Sweet treats"
  }
  ```

### 5. Update Category

- **Method**: `PUT`
- **Endpoint**: `/api/categories/{id}`
- **Description**: Updates an existing category. (Admin only)
- **Authentication**: Requires Admin role.

### 6. Delete Category

- **Method**: `DELETE`
- **Endpoint**: `/api/categories/{id}`
- **Description**: Deletes a category. (Admin only)
- **Authentication**: Requires Admin role.

---

## Menu Items

Base Path: `/api/menu`

### 1. Get All Menu Items

- **Method**: `GET`
- **Endpoint**: `/api/menu`
- **Description**: Retrieves a paginated list of all menu items.
- **Query Parameters**:
  - `page` (int, optional, default: 0)
  - `size` (int, optional, default: 10)
  - `sortBy` (string, optional, default: "name")
  - `sortDir` (string, optional, default: "asc")

### 2. Get Menu Items by Category

- **Method**: `GET`
- **Endpoint**: `/api/menu/category/{categoryId}`
- **Description**: Retrieves a paginated list of menu items within a specific category.

### 3. Get Menu Item by ID or Slug

- **Method**: `GET`
- **Endpoint**: `/api/menu/{id}` or `/api/menu/slug/{slug}`
- **Description**: Retrieves a single menu item by its ID or slug.

### 4. Search Menu Items

- **Method**: `GET`
- **Endpoint**: `/api/menu/search`
- **Description**: Searches for menu items by a keyword.
- **Query Parameters**:
  - `keyword` (string, required)

### 5. Filter by Price

- **Method**: `GET`
- **Endpoint**: `/api/menu/filter/price`
- **Description**: Filters menu items within a given price range.
- **Query Parameters**:
  - `minPrice` (BigDecimal, required)
  - `maxPrice` (BigDecimal, required)

### 6. Get Special Menu Items

- **Method**: `GET`
- **Endpoints**:
  - `/api/menu/featured`
  - `/api/menu/top-rated`
  - `/api/menu/popular`
- **Description**: Retrieves lists of featured, top-rated, or popular menu items.

### 7. Create Menu Item (Admin)

- **Method**: `POST`
- **Endpoint**: `/api/menu`
- **Description**: Creates a new menu item.
- **Authentication**: Requires Admin role.
- **Request Body**:
  ```json
  {
    "name": "Grilled Chicken",
    "description": "Juicy grilled chicken breast with herbs",
    "price": 12.99,
    "categoryId": 1,
    "imageUrl": "https://cdn.example.com/images/grilled-chicken.jpg",
    "isAvailable": true,
    "isFeatured": false,
    "preparationTime": 15,
    "spicyLevel": 1,
    "ingredients": "Chicken, herbs, olive oil",
    "allergens": "None",
    "calories": 320,
    "servingSize": "250g"
  }
  ```
- **Field Notes**:
  - `name` (string, required)
  - `price` (number, required, > 0)
  - `categoryId` (number, required, must match an existing category)
  - `imageUrl` (string, optional). Accepts any string; typically an `http/https` URL. A data URL works if within server size limits.
  - `isAvailable`, `isFeatured` (booleans, optional; default `true`/`false`)
  - `preparationTime`, `spicyLevel`, `calories` (numbers, optional)
  - `ingredients`, `allergens`, `servingSize`, `description` (strings, optional)
- **Success Response (201 Created)**:
  ```json
  {
    "id": 10,
    "name": "Grilled Chicken",
    "slug": "grilled-chicken",
    "description": "Juicy grilled chicken breast with herbs",
    "price": 12.99,
    "category": {
      "id": 1,
      "name": "Mains",
      "slug": "mains",
      "description": "Main courses",
      "imageUrl": null,
      "displayOrder": 1,
      "isActive": true,
      "createdAt": "2024-01-01T12:00:00",
      "updatedAt": "2024-01-01T12:00:00"
    },
    "imageUrl": "https://cdn.example.com/images/grilled-chicken.jpg",
    "isAvailable": true,
    "isFeatured": false,
    "preparationTime": 15,
    "spicyLevel": 1,
    "ingredients": "Chicken, herbs, olive oil",
    "allergens": "None",
    "calories": 320,
    "servingSize": "250g",
    "rating": 0.0,
    "reviewCount": 0,
    "orderCount": 0,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
  ```
- **Error Responses**:
  - `400 Bad Request` for validation failures (missing name/price/categoryId, price ≤ 0, duplicate name/slug)
  - `404 Not Found` if `categoryId` does not exist
  - `500 Internal Server Error` for uncaught server errors (e.g., oversized payload)

### 8. Update/Delete/Toggle Menu Item (Admin)

- **Endpoints**:
  - `PUT /api/menu/{id}`
  - `PATCH /api/menu/{id}/toggle-availability`
  - `DELETE /api/menu/{id}`
- **Description**: Update, toggle availability, or delete an item. (Admin only)
- **Authentication**: Requires Admin role.

---

## Orders

Base Path: `/api/orders`

### 1. Create Order

- **Method**: `POST`
- **Endpoint**: `/api/orders`
- **Description**: Places a new order.
- **Authentication**: Requires JWT Bearer Token.
- **Request Body**:
  ```json
  {
    "items": [
      {
        "menuItemId": 1,
        "quantity": 2
      }
    ],
    "deliveryAddress": "123 Main St, Anytown, USA",
    "paymentMethodCode": "COD"
  }
  ```

### 2. Get User Orders

- **Method**: `GET`
- **Endpoint**: `/api/orders`
- **Description**: Retrieves a paginated list of the authenticated user's orders.
- **Authentication**: Requires JWT Bearer Token.
- **Query Parameters**:
  - `status` (string, optional): Filter by order status (e.g., `PENDING`, `CONFIRMED`).

### 3. Get Order by ID

- **Method**: `GET`
- **Endpoint**: `/api/orders/{id}`
- **Description**: Retrieves a specific order belonging to the authenticated user.
- **Authentication**: Requires JWT Bearer Token.

### 4. Update/Cancel/Reorder

- **Methods**: `PATCH`, `POST`
- **Endpoints**:
  - `PATCH /api/orders/{id}`
  - `POST /api/orders/{id}/cancel`
  - `POST /api/orders/{id}/reorder`
- **Description**: Endpoints for managing an existing order.
- **Authentication**: Requires JWT Bearer Token.

---

## Payments

Base Path: `/api/payments`

### 1. Get Payment Methods

- **Method**: `GET`
- **Endpoint**: `/api/payments/methods`
- **Description**: Retrieves a list of available payment methods.

### 2. Get Payment Details

- **Method**: `GET`
- **Endpoint**: `/api/payments/{paymentCode}`
- **Description**: Retrieves details for a specific payment.

### 3. Confirm Payment

- **Method**: `POST`
- **Endpoint**: `/api/payments/{paymentCode}/confirm`
- **Description**: Confirms a payment. The request body depends on the payment provider.

---

## Admin

Base Path: `/admin`

### 1. Get Logs

- **Method**: `GET`
- **Endpoint**: `/admin/logs`
- **Description**: Retrieves application logs. (Admin only)
- **Authentication**: Requires Admin role.
- **Query Parameters**:
  - `category` (string, optional)
  - `level` (string, optional)
  - `limit` (int, optional)
  - `page` (int, optional)

### 2. Get Log Categories

- **Method**: `GET`
- **Endpoint**: `/admin/logs/categories`
- **Description**: Retrieves available log categories. (Admin only)
- **Authentication**: Requires Admin role.

---

## Admin - Statistics

Base Path: `/api/admin/statistics`

### 1. Get Dashboard Statistics

- **Method**: `GET`
- **Endpoint**: `/api/admin/statistics/dashboard`
- **Description**: Retrieves aggregated statistics for the admin dashboard.
- **Authentication**: Requires Admin role.
- **Query Parameters**:
  - `days` (int, optional, default: 30): The number of past days to include in the statistics.
- **Success Response (200 OK)**:
  ```json
  {
    "totalOrders": 150,
    "totalRevenue": 7500.00,
    "totalMenuItems": 50,
    "totalUsers": 100,
    "revenueOverTime": [
      {
        "date": "2025-10-06",
        "orderCount": 5,
        "revenue": 250.00
      }
    ],
    "topSellingItems": [
      {
        "menuItemId": 1,
        "name": "Classic Burger",
        "totalQuantity": 50
      }
    ],
    "orderStatusDistribution": {
      "COMPLETED": 120,
      "PENDING": 10,
      "CANCELLED": 20
    }
  }
  ```

---

## General

### 1. Welcome Message

- **Method**: `GET`
- **Endpoint**: `/`
- **Description**: Returns a welcome message.

### 2. Health Check

- **Method**: `GET`
- **Endpoint**: `/health`
- **Description**: Returns the health status of the API.
- **Success Response (200 OK)**:
  ```
  API is running
  ```
