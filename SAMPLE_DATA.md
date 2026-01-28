# Sample Data & Seed Scripts

This file contains sample data for testing and populating the database.

## 📝 Sample Users

### Admin User
```json
{
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@propertyvalue.com",
  "password": "admin@123",
  "role": "admin",
  "city": "Mumbai",
  "state": "Maharashtra"
}
```

### Sample Users
```json
[
  {
    "firstName": "Rajesh",
    "lastName": "Kumar",
    "email": "rajesh@email.com",
    "password": "secure123",
    "city": "Mumbai",
    "state": "Maharashtra"
  },
  {
    "firstName": "Priya",
    "lastName": "Singh",
    "email": "priya@email.com",
    "password": "secure456",
    "city": "Bangalore",
    "state": "Karnataka"
  },
  {
    "firstName": "Amit",
    "lastName": "Patel",
    "email": "amit@email.com",
    "password": "secure789",
    "city": "Ahmedabad",
    "state": "Gujarat"
  }
]
```

---

## 🏠 Sample Properties

### Sample Property 1
```json
{
  "title": "Beautiful 2 BHK Apartment in Fort",
  "propertyType": "apartment",
  "age": 5,
  "builUpArea": 1200,
  "bedrooms": 2,
  "bathrooms": 2,
  "location": {
    "address": "123 Marine Drive, Fort",
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
  "features": ["parking", "balcony", "security", "lift"],
  "status": "pending"
}
```

### Sample Property 2
```json
{
  "title": "Spacious 3 BHK Villa in Whitefield",
  "propertyType": "villa",
  "age": 3,
  "builUpArea": 1800,
  "bedrooms": 3,
  "bathrooms": 3,
  "location": {
    "address": "456 Tech Park, Whitefield",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560066",
    "coordinates": {
      "latitude": 12.9698,
      "longitude": 77.7499
    }
  },
  "condition": "excellent",
  "currentValue": 6500000,
  "features": ["garden", "parking", "balcony", "security", "gym"],
  "status": "reviewed"
}
```

### Sample Property 3
```json
{
  "title": "Cozy 1 BHK Studio in Andheri",
  "propertyType": "studio",
  "age": 8,
  "builUpArea": 600,
  "bedrooms": 1,
  "bathrooms": 1,
  "location": {
    "address": "789 Central Plaza, Andheri",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400053",
    "coordinates": {
      "latitude": 19.1136,
      "longitude": 72.8697
    }
  },
  "condition": "average",
  "currentValue": 2000000,
  "features": ["parking", "security"],
  "status": "pending"
}
```

---

## 💡 Sample Recommendations

### Recommendation 1: Kitchen Upgrade
```json
{
  "title": "Modern Modular Kitchen Setup",
  "category": "kitchen-bathroom",
  "description": "Transform your kitchen with a modern modular setup. Includes modular cabinets, stainless steel appliances, granite countertop, and efficient layout to maximize space and functionality.",
  "benefits": [
    "Increased functionality",
    "Enhanced aesthetic appeal",
    "Better storage utilization",
    "Improved home value",
    "Modern look appeals to buyers"
  ],
  "estimatedCost": {
    "min": 200000,
    "max": 500000
  },
  "expectedROI": 50,
  "roiPercentage": 50,
  "difficulty": "moderate",
  "timeframe": "4-6 weeks",
  "tips": [
    "Plan the layout before purchasing",
    "Invest in quality materials",
    "Hire experienced contractors",
    "Consider your lifestyle needs"
  ],
  "applicablePropertyTypes": ["apartment", "house", "villa"],
  "applicableConditions": ["good", "average"],
  "priority": 1
}
```

### Recommendation 2: Flooring Upgrade
```json
{
  "title": "Premium Vitrified Tile Flooring",
  "category": "flooring",
  "description": "Replace existing flooring with premium vitrified tiles. Offers durability, easy maintenance, aesthetic appeal, and works well with Indian climate. Suitable for living areas, bedrooms, and kitchens.",
  "benefits": [
    "Durable and long-lasting",
    "Easy to maintain",
    "Aesthetic appeal",
    "Climate appropriate",
    "Increases property value"
  ],
  "estimatedCost": {
    "min": 150000,
    "max": 400000
  },
  "expectedROI": 40,
  "difficulty": "moderate",
  "timeframe": "3-4 weeks",
  "tips": [
    "Choose light colors for smaller spaces",
    "Ensure proper installation",
    "Use non-slip tiles for bathrooms",
    "Regular sealing maintains longevity"
  ],
  "applicablePropertyTypes": ["apartment", "house", "villa", "townhouse"],
  "applicableConditions": ["good", "average", "needs-work"],
  "priority": 1
}
```

### Recommendation 3: Lighting & Fixtures
```json
{
  "title": "LED Lighting System Upgrade",
  "category": "lighting-fixtures",
  "description": "Complete LED lighting transformation. Replace all bulbs and fixtures with modern LED solutions. Includes recessed lighting, pendant lights, smart switches, and proper light design for different rooms.",
  "benefits": [
    "Energy efficient (80% power savings)",
    "Modern aesthetic",
    "Better ambiance",
    "Long lifespan",
    "Smart control options"
  ],
  "estimatedCost": {
    "min": 50000,
    "max": 150000
  },
  "expectedROI": 20,
  "difficulty": "easy",
  "timeframe": "1-2 weeks",
  "tips": [
    "Choose warm white for living areas",
    "Cool white for kitchens and bathrooms",
    "Add dimmers for flexibility",
    "Invest in quality fixtures"
  ],
  "applicablePropertyTypes": ["all"],
  "applicableConditions": ["excellent", "good", "average"],
  "priority": 2
}
```

### Recommendation 4: Paint & Wall Treatment
```json
{
  "title": "Fresh Paint & Wall Treatment",
  "category": "wall-paint",
  "description": "Refresh your home with modern paint colors and wall treatments. Includes premium paint, texture finishes, or wallpaper. Professional application ensures quality results.",
  "benefits": [
    "Instant visual transformation",
    "Cost-effective improvement",
    "Increases perceived value",
    "Modern color schemes",
    "Personalization options"
  ],
  "estimatedCost": {
    "min": 30000,
    "max": 80000
  },
  "expectedROI": 15,
  "difficulty": "easy",
  "timeframe": "1-2 weeks",
  "tips": [
    "Use neutral colors for broad appeal",
    "Add accent walls for style",
    "Quality paint ensures durability",
    "Professional finish looks better"
  ],
  "applicablePropertyTypes": ["all"],
  "applicableConditions": ["all"],
  "priority": 3
}
```

### Recommendation 5: Security System
```json
{
  "title": "Advanced Security System Installation",
  "category": "safety-security",
  "description": "Install comprehensive security system including CCTV cameras, video doorbell, motion sensors, alarm system, and digital locks. Modern security appeals to today's buyers.",
  "benefits": [
    "Peace of mind",
    "24/7 monitoring capability",
    "Deters theft and burglary",
    "Insurance benefits",
    "Appeals to modern buyers"
  ],
  "estimatedCost": {
    "min": 80000,
    "max": 250000
  },
  "expectedROI": 25,
  "difficulty": "moderate",
  "timeframe": "2-3 weeks",
  "tips": [
    "Choose wireless systems for apartments",
    "Ensure professional installation",
    "Include cloud backup",
    "Regular maintenance needed"
  ],
  "applicablePropertyTypes": ["apartment", "house", "villa"],
  "applicableConditions": ["all"],
  "priority": 2
}
```

### Recommendation 6: Energy Efficiency Upgrades
```json
{
  "title": "Solar Panel Installation",
  "category": "energy-efficiency",
  "description": "Install solar panels for clean energy generation. Reduces electricity bills significantly and appeals to environmentally conscious buyers. Government incentives available.",
  "benefits": [
    "Reduced electricity bills",
    "Environmental sustainability",
    "Government subsidies available",
    "Long-term savings",
    "Modern green home appeal"
  ],
  "estimatedCost": {
    "min": 300000,
    "max": 700000
  },
  "expectedROI": 35,
  "difficulty": "difficult",
  "timeframe": "3-4 weeks",
  "tips": [
    "Get roof assessment first",
    "Apply for government subsidies",
    "Choose reputable installers",
    "Proper maintenance increases lifespan"
  ],
  "applicablePropertyTypes": ["house", "villa"],
  "applicableConditions": ["good", "average", "excellent"],
  "priority": 2
}
```

---

## 🌿 Sample Garden/Outdoor Enhancement

### Recommendation 7: Landscaping & Garden
```json
{
  "title": "Professional Landscaping & Garden Design",
  "category": "garden-outdoor",
  "description": "Transform your outdoor space with professional landscaping. Includes garden design, plantation, pathways, seating areas, and water features.",
  "benefits": [
    "Curb appeal",
    "Additional relaxation space",
    "Visual beauty",
    "Environmental benefits",
    "Great for outdoor entertainment"
  ],
  "estimatedCost": {
    "min": 50000,
    "max": 200000
  },
  "expectedROI": 30,
  "difficulty": "moderate",
  "timeframe": "2-3 weeks",
  "tips": [
    "Mix ornamental and practical plants",
    "Use local plants for maintenance",
    "Include seating area",
    "Regular maintenance schedule"
  ],
  "applicablePropertyTypes": ["house", "villa"],
  "applicableConditions": ["all"],
  "priority": 3
}
```

---

## 🔧 Sample Plumbing Upgrade

### Recommendation 8: Plumbing Modernization
```json
{
  "title": "Modern Plumbing System Upgrade",
  "category": "electrical-plumbing",
  "description": "Replace old plumbing with modern copper or PEX pipes. Includes water pressure optimization, tankless water heater, and modern fixtures.",
  "benefits": [
    "Better water pressure",
    "Reduced water leaks",
    "Hot water availability",
    "Modern fixtures",
    "Long-term durability"
  ],
  "estimatedCost": {
    "min": 100000,
    "max": 300000
  },
  "expectedROI": 20,
  "difficulty": "difficult",
  "timeframe": "2-3 weeks",
  "tips": [
    "Hire certified plumbers",
    "Get proper permits",
    "Include pressure reduction valve",
    "Install water softener if needed"
  ],
  "applicablePropertyTypes": ["apartment", "house", "villa"],
  "applicableConditions": ["average", "needs-work"],
  "priority": 2
}
```

---

## MongoDB Insert Commands

### Create Admin User
```javascript
db.users.insertOne({
  firstName: "Admin",
  lastName: "User",
  email: "admin@propertyvalue.com",
  password: "hashed_password_here",
  role: "admin",
  city: "Mumbai",
  state: "Maharashtra",
  isActive: true,
  createdAt: new Date(),
  updatedAt: new Date()
})
```

### Create Multiple Recommendations
```javascript
db.recommendations.insertMany([
  // Paste recommendation objects here
])
```

---

## Testing with Postman

### Import Collection
Create a Postman collection with these endpoints:

1. **Register** - POST /auth/register
2. **Login** - POST /auth/login
3. **Get Recommendations** - GET /recommendations
4. **Create Property** - POST /properties
5. **Get Properties** - GET /properties

---

## Notes

- Replace email addresses with your own for testing
- Change passwords for security
- Update coordinates for actual locations
- Adjust costs based on your region
- Test all functionality before production

---

**Last Updated**: January 2026
