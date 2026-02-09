# Development Guide

## Project Overview
This is a full-stack MERN application designed to help Indian middle-class homeowners enhance the value of their residential properties.

## Technology Stack

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MySQL (via Sequelize)
- **Authentication**: JWT (JSON Web Tokens)
- **Password Hashing**: bcryptjs

### Frontend
- **UI Framework**: React.js
- **State Management**: Redux Toolkit
- **Routing**: React Router v6
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios
- **Notifications**: React Toastify

## Code Structure

### Backend Structure
```
backend/
├── models/           # Sequelize models
├── routes/           # API endpoints
├── middleware/       # Custom middleware
├── services/         # Business logic (future)
├── utils/           # Utility functions (future)
├── .env.example     # Environment variables template
├── server.js        # Entry point
└── package.json     # Dependencies
```

### Frontend Structure
```
frontend/src/
├── components/      # Reusable React components
├── pages/          # Page components
├── redux/          # Redux slices and store
├── services/       # API service layer
├── App.js          # Main App component
├── index.js        # Entry point
└── index.css       # Global styles
```

## Key Concepts

### Database Schema

#### User Model
- firstName, lastName, email, password
- role (user/admin)
- city, state, profileImage, bio
- savedRecommendations, propertySubmissions
- isActive, timestamps

#### Property Model
- userId (owner reference)
- propertyType, age, builUpArea
- bedrooms, bathrooms, location
- condition, currentValue, features
- improvements, recommendations
- status (pending/reviewed/recommended)

#### Recommendation Model
- title, category, description
- benefits, estimatedCost, expectedROI
- difficulty, timeframe, tips
- applicablePropertyTypes, applicableConditions
- images, relatedRecommendations
- createdBy, timestamps

### Authentication Flow
1. User registers/logs in
2. Backend validates credentials
3. JWT token generated and sent to client
4. Client stores token in localStorage
5. Token included in all API requests
6. Backend verifies token on protected routes

### State Management (Redux)
- **authSlice**: User authentication state
- **propertySlice**: Properties data and filters
- **recommendationSlice**: Recommendations data

## Development Workflow

### Adding a New Feature

#### 1. Backend (API)
```javascript
// 1. Update model if needed (models/example.js)
// 2. Create route handler (routes/example.js)
// 3. Add route to server.js

// Example route:
router.post('/', authenticateToken, async (req, res) => {
  try {
    // Your logic here
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});
```

#### 2. Frontend (UI)
```javascript
// 1. Create Redux slice if managing state (redux/exampleSlice.js)
// 2. Add API call method (services/api.js)
// 3. Create component/page (components/ or pages/)
// 4. Add route to App.js
```

### Example: Add New Feature

**Backend - Route (routes/example.js):**
```javascript
const express = require('express');
const { authenticateToken, authorizeAdmin } = require('../middleware/auth');
const router = express.Router();

router.get('/', async (req, res) => {
  try {
    // Logic
    res.json({ success: true, data: [] });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

module.exports = router;
```

**Frontend - Component:**
```javascript
import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { exampleAPI } from '../services/api';

const ExampleComponent = () => {
  const dispatch = useDispatch();
  const { data } = useSelector(state => state.example);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response = await exampleAPI.getData();
      // Update Redux state
    } catch (error) {
      console.error(error);
    }
  };

  return <div>{/* Your JSX */}</div>;
};

export default ExampleComponent;
```

## Common Tasks

### Adding a New API Endpoint

1. **Create route handler** in appropriate routes file
2. **Add authentication middleware** if needed
3. **Register route** in server.js
4. **Test** using Postman or curl
5. **Add API method** in frontend services/api.js
6. **Update Redux** if managing state
7. **Create UI component** to use the endpoint

### Styling Components

- Use **Tailwind CSS** utility classes
- For complex styles, use index.css
- Follow existing component styling patterns
- Ensure responsive design

### Adding Form Validation

**Backend:**
```javascript
const { body, validationResult } = require('express-validator');

router.post('/', [
  body('email').isEmail(),
  body('password').isLength({ min: 6 })
], (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  // Process
});
```

**Frontend:**
```javascript
const handleSubmit = (e) => {
  e.preventDefault();
  
  // Validate
  if (!formData.email.includes('@')) {
    setError('Invalid email');
    return;
  }
  
  // Submit
  apiCall(formData);
};
```

## Testing

### Manual Testing
1. Run backend and frontend
2. Use browser dev tools
3. Check network requests
4. Verify data in MySQL

### API Testing
- Use Postman or REST Client VS Code extension
- Create test collections for each endpoint
- Document expected responses

## Performance Tips

### Backend
- Use pagination for large datasets
- Add database indexes
- Cache frequently accessed data
- Minimize database queries

### Frontend
- Use React.memo for components
- Lazy load routes
- Optimize images
- Minimize bundle size

## Security Best Practices

1. **Validate all inputs** on backend
2. **Use HTTPS** in production
3. **Secure JWT secret** in .env
4. **Hash passwords** with bcrypt
5. **Sanitize** user inputs
6. **Use CORS** properly
7. **Implement rate limiting**
8. **Validate file uploads**

## Deployment Checklist

- [ ] Environment variables configured
- [ ] Database production instance set up
- [ ] Security headers configured
- [ ] API endpoints tested
- [ ] UI responsive on all devices
- [ ] Error handling implemented
- [ ] Logging configured
- [ ] Monitoring set up
- [ ] Backup strategy in place
- [ ] Documentation updated

## Useful Commands

```bash
# Backend
npm run dev           # Start development server
npm test             # Run tests
npm start            # Start production server

# Frontend
npm start            # Start development server
npm run build        # Build for production
npm test             # Run tests

# Root
npm run setup        # Install all dependencies
npm run dev          # Start both servers
```

## Debugging Tips

1. **Use console.log** for simple debugging
2. **Browser DevTools** for frontend issues
3. Your preferred MySQL client for database inspection
4. **Postman** for API debugging
5. **Redux DevTools** for state management
6. **Network tab** for request/response inspection

## Resources

- [Express.js Docs](https://expressjs.com/)
- [React Docs](https://react.dev/)
- [Redux Toolkit](https://redux-toolkit.js.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [JWT.io](https://jwt.io/)

---

**Last Updated**: January 2026
