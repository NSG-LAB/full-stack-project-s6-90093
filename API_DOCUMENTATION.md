# API Documentation

## Base URL
```
http://localhost:5000/api
```

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your_token_here>
```

## Endpoints

### 1. Authentication

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "securepassword",
  "city": "Mumbai",
  "state": "Maharashtra"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "token": "eyJhbGc...",
  "user": {
    "id": "507f1f77bcf86cd799439011",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "role": "user"
  }
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securepassword"
}
```

### 2. Properties

#### Create Property
```http
POST /properties
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Beautiful 2 BHK Apartment",
  "propertyType": "apartment",
  "age": 5,
  "builUpArea": 1200,
  "bedrooms": 2,
  "bathrooms": 2,
  "location": {
    "address": "123 Main Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "coordinates": {
      "latitude": 19.0760,
      "longitude": 72.8777
    }
  },
  "condition": "good",
  "currentValue": 4500000,
  "features": ["parking", "balcony", "security"]
}
```

#### Get All Properties
```http
GET /properties?city=Mumbai&propertyType=apartment&status=pending
```

#### Get Property by ID
```http
GET /properties/:id
```

#### Update Property
```http
PUT /properties/:id
Authorization: Bearer <token>
Content-Type: application/json

{
  "condition": "excellent",
  "currentValue": 5000000
}
```

#### Delete Property
```http
DELETE /properties/:id
Authorization: Bearer <token>
```

### 3. Recommendations

#### Get All Recommendations
```http
GET /recommendations?category=kitchen-bathroom&difficulty=easy
```

#### Get Recommendations for Property
```http
GET /recommendations/property/:propertyId
```

#### Create Recommendation (Admin Only)
```http
POST /recommendations
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "title": "Modern Kitchen Upgrade",
  "category": "kitchen-bathroom",
  "description": "Modernize your kitchen with contemporary fittings and appliances",
  "benefits": [
    "Improved functionality",
    "Enhanced aesthetics",
    "Increased home value"
  ],
  "estimatedCost": {
    "min": 200000,
    "max": 500000
  },
  "expectedROI": 45,
  "difficulty": "moderate",
  "timeframe": "4-6 weeks",
  "tips": [
    "Plan the layout first",
    "Choose quality materials",
    "Hire experienced contractors"
  ],
  "applicablePropertyTypes": ["apartment", "house"],
  "applicableConditions": ["good", "average"]
}
```

#### Update Recommendation (Admin Only)
```http
PUT /recommendations/:id
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "expectedROI": 50
}
```

#### Delete Recommendation (Admin Only)
```http
DELETE /recommendations/:id
Authorization: Bearer <admin_token>
```

### 4. Users

#### Get User Profile
```http
GET /users/profile
Authorization: Bearer <token>
```

#### Update User Profile
```http
PUT /users/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "9876543210",
  "city": "Mumbai",
  "state": "Maharashtra",
  "bio": "Home owner interested in property enhancement"
}
```

#### Get All Users (Admin Only)
```http
GET /users
Authorization: Bearer <admin_token>
```

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "All fields are required"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Access token required"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Admin access required"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Property not found"
}
```

### 500 Server Error
```json
{
  "success": false,
  "message": "Something went wrong!"
}
```

## Status Codes

- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **400 Bad Request** - Invalid request data
- **401 Unauthorized** - Missing or invalid authentication
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **500 Server Error** - Internal server error

---

**Last Updated**: January 2026
