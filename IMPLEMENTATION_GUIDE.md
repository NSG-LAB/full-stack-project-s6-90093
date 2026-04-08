# Implementation Guide - Code Examples for Critical Fixes

## 1. Environment Variable Validation (server.js)

**File**: `backend/server.js`  
**Insert After**: Line 9 (after dotenv.config)

```javascript
// ==========================================
// CRITICAL FIX: Validate required environment variables
// ==========================================
const requiredEnvVars = [
  'JWT_SECRET',
  'MYSQL_DB',
  'MYSQL_USER'
];

const missingEnvVars = requiredEnvVars.filter(
  (envVar) => !process.env[envVar]
);

if (missingEnvVars.length > 0) {
  console.error(
    '❌ FATAL: Missing required environment variables:',
    missingEnvVars.join(', ')
  );
  console.error('Please configure these in your .env file');
  process.exit(1);
}

console.log('✅ All required environment variables are set');
```

---

## 2. Add Helmet Security Headers

**File**: `backend/server.js`  
**Installation**:
```bash
cd backend && npm install helmet
```

**Insert After**: Line 14 (after requiring helmet at top)
```javascript
const helmet = require('helmet');
```

**Insert After**: Line 17 (after express setup)
```javascript
// ==========================================
// Security: Add all HTTP headers
// ==========================================
app.use(helmet());
```

---

## 3. Fix CORS Configuration

**File**: `backend/server.js`  
**Replace**: Lines 15-16 (the current cors() call)

**Before**:
```javascript
app.use(cors());
```

**After**:
```javascript
// ==========================================
// CRITICAL FIX: Whitelist CORS origins
// ==========================================
const corsOptions = {
  origin: (origin, callback) => {
    const allowedOrigins = [
      'http://localhost:3000',
      'http://localhost:5173',  // Vite dev server
      process.env.FRONTEND_URL   // Production frontend
    ].filter(Boolean);

    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('CORS policy violation'));
    }
  },
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
};

app.use(cors(corsOptions));
```

---

## 4. Add Compression Middleware

**File**: `backend/server.js`  
**Installation**:
```bash
cd backend && npm install compression
```

**Insert After**: Line 5 (top imports)
```javascript
const compression = require('compression');
```

**Insert After**: Helmet middleware (around line 20)
```javascript
// ==========================================
// Performance: Enable response compression
// ==========================================
app.use(compression());
```

---

## 5. Create Centralized Input Validation Middleware

**File**: `backend/middleware/validation.js` (CREATE NEW FILE)

```javascript
const { body, param, query, validationResult } = require('express-validator');

// ==========================================
// Validation Middleware
// ==========================================

const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      message: 'Validation error',
      errors: errors.array().map(err => ({
        field: err.param,
        message: err.msg
      }))
    });
  }
  next();
};

// Auth validation rules
const authRules = {
  register: [
    body('firstName')
      .trim()
      .notEmpty().withMessage('First name required')
      .isLength({ max: 50 }).withMessage('First name max 50 chars'),
    body('lastName')
      .trim()
      .notEmpty().withMessage('Last name required')
      .isLength({ max: 50 }).withMessage('Last name max 50 chars'),
    body('email')
      .trim()
      .isEmail().withMessage('Invalid email format')
      .normalizeEmail(),
    body('password')
      .isLength({ min: 8 }).withMessage('Password min 8 characters')
      .matches(/[A-Z]/).withMessage('Must contain uppercase letter')
      .matches(/[0-9]/).withMessage('Must contain number'),
    body('city')
      .optional()
      .trim()
      .isLength({ max: 100 }).withMessage('City max 100 chars'),
    body('state')
      .optional()
      .trim()
      .isLength({ max: 100 }).withMessage('State max 100 chars')
  ],

  login: [
    body('email')
      .trim()
      .isEmail().withMessage('Invalid email format')
      .normalizeEmail(),
    body('password')
      .notEmpty().withMessage('Password required')
  ]
};

// Property validation rules
const propertyRules = {
  create: [
    body('title')
      .trim()
      .notEmpty().withMessage('Title required')
      .isLength({ max: 200 }).withMessage('Title max 200 chars'),
    body('propertyType')
      .isIn(['apartment', 'house', 'villa', 'townhouse']).withMessage('Invalid property type'),
    body('age')
      .isInt({ min: 0, max: 150 }).withMessage('Age must be 0-150'),
    body('builUpArea')
      .isInt({ min: 100, max: 100000 }).withMessage('Area must be 100-100000 sqft'),
    body('bedrooms')
      .isInt({ min: 0, max: 20 }).withMessage('Bedrooms must be 0-20'),
    body('bathrooms')
      .isInt({ min: 0, max: 20 }).withMessage('Bathrooms must be 0-20'),
    body('condition')
      .isIn(['excellent', 'good', 'average', 'needs-work']).withMessage('Invalid condition'),
    body('currentValue')
      .isInt({ min: 0 }).withMessage('Value must be positive')
  ]
};

// Recommendation validation rules
const recommendationRules = {
  create: [
    body('title')
      .trim()
      .notEmpty().withMessage('Title required')
      .isLength({ max: 200 }).withMessage('Title max 200 chars'),
    body('category')
      .isIn(['kitchen-bathroom', 'flooring', 'wall-paint', 'lighting-fixtures', 'garden-outdoor', 'safety-security', 'energy-efficiency', 'interior-design', 'electrical-plumbing'])
      .withMessage('Invalid category'),
    body('description')
      .trim()
      .notEmpty().withMessage('Description required')
      .isLength({ max: 5000 }).withMessage('Description max 5000 chars'),
    body('estimatedCost.min')
      .isInt({ min: 0 }).withMessage('Min cost must be positive'),
    body('estimatedCost.max')
      .isInt({ min: 0 }).withMessage('Max cost must be positive'),
    body('expectedROI')
      .isInt({ min: 0, max: 500 }).withMessage('ROI must be 0-500%'),
    body('difficulty')
      .isIn(['easy', 'moderate', 'difficult']).withMessage('Invalid difficulty')
  ]
};

module.exports = {
  handleValidationErrors,
  authRules,
  propertyRules,
  recommendationRules
};
```

---

## 6. Update Auth Routes with Validation

**File**: `backend/routes/auth.js`  
**Replace**: Top imports and register/login routes

```javascript
const express = require('express');
const jwt = require('jsonwebtoken');
const { User } = require('../models');
const { authRules, handleValidationErrors } = require('../middleware/validation');

const router = express.Router();

const buildToken = (user) =>
  jwt.sign(
    { userId: user.id, email: user.email, role: user.role },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRE || '7d' }
  );

const toPublicUser = (userInstance) => {
  if (!userInstance) return null;
  const user = userInstance.get({ plain: true });
  delete user.password;
  return user;
};

// Register with validation
router.post('/register', 
  authRules.register, 
  handleValidationErrors, 
  async (req, res) => {
    try {
      const { firstName, lastName, email, password, city, state } = req.body;

      // Check if user exists
      const existingUser = await User.findOne({ where: { email } });
      if (existingUser) {
        return res.status(400).json({ 
          success: false, 
          message: 'Email already registered' 
        });
      }

      const user = await User.create({
        firstName,
        lastName,
        email,
        password,
        city,
        state
      });

      const token = buildToken(user);

      res.status(201).json({
        success: true,
        message: 'User registered successfully',
        token,
        user: toPublicUser(user)
      });
    } catch (error) {
      console.error('Registration error:', error);
      res.status(500).json({ 
        success: false, 
        message: 'Registration failed' 
      });
    }
  }
);

// Login with validation
router.post('/login',
  authRules.login,
  handleValidationErrors,
  async (req, res) => {
    try {
      const { email, password } = req.body;

      const user = await User.scope('withPassword').findOne({ where: { email } });
      if (!user) {
        return res.status(401).json({ 
          success: false, 
          message: 'Invalid credentials' 
        });
      }

      const isPasswordValid = await user.comparePassword(password);
      if (!isPasswordValid) {
        return res.status(401).json({ 
          success: false, 
          message: 'Invalid credentials' 
        });
      }

      const token = buildToken(user);

      res.json({
        success: true,
        message: 'Login successful',
        token,
        user: toPublicUser(user)
      });
    } catch (error) {
      console.error('Login error:', error);
      res.status(500).json({ 
        success: false, 
        message: 'Login failed' 
      });
    }
  }
);

module.exports = router;
```

---

## 7. Add Rate Limiting to Auth Routes

**File**: Create `backend/middleware/rateLimiter.js` (NEW FILE)

```bash
npm install express-rate-limit
```

```javascript
const rateLimit = require('express-rate-limit');

// ==========================================
// Rate Limiters
// ==========================================

// Strict limit for auth endpoints (brute force protection)
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // 5 requests per window
  message: 'Too many login attempts, please try again later',
  standardHeaders: true,
  legacyHeaders: false,
  skipSuccessfulRequests: false // Count all requests
});

// General API limiter
const apiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 100, // 100 requests per minute
  message: 'Too many requests, please try again later'
});

module.exports = { authLimiter, apiLimiter };
```

**File**: `backend/routes/auth.js`  
**Insert**: At top
```javascript
const { authLimiter } = require('../middleware/rateLimiter');
```

**Update**: Both route definitions
```javascript
router.post('/register', authLimiter, authRules.register, handleValidationErrors, async (req, res) => {
  // ...
});

router.post('/login', authLimiter, authRules.login, handleValidationErrors, async (req, res) => {
  // ...
});
```

---

## 8. Fix Frontend API Base URL

**File**: `frontend/src/services/api.js`  
**Replace**: Line 3

**Before**:
```javascript
const api = axios.create({
  baseURL: 'http://localhost:5000/api',
});
```

**After**:
```javascript
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:5000/api',
});
```

**Create**: `frontend/.env.local` (local dev)
```env
VITE_API_URL=http://localhost:5000/api
```

**Create**: `frontend/.env.production` (production)
```env
VITE_API_URL=https://full-stack-project-s6-90093-production.up.railway.app/api
```

---

## 9. Create Error Boundary Component

**File**: `frontend/src/components/ErrorBoundary.js` (CREATE NEW)

```javascript
import React from 'react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
    // Could send to error tracking service here
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-red-50">
          <div className="max-w-md bg-white rounded-lg shadow p-8 border border-red-200">
            <h1 className="text-2xl font-bold text-red-600 mb-4">
              Something Went Wrong
            </h1>
            <p className="text-gray-600 mb-6">
              We encountered an unexpected error. Please try refreshing the page.
            </p>
            {process.env.NODE_ENV === 'development' && (
              <details className="mb-6 text-sm">
                <summary className="cursor-pointer font-mono text-red-800">
                  Error Details
                </summary>
                <pre className="mt-2 p-2 bg-gray-100 text-xs overflow-auto">
                  {this.state.error?.toString()}
                </pre>
              </details>
            )}
            <button
              onClick={() => window.location.reload()}
              className="w-full btn-primary py-2"
            >
              Refresh Page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
```

**File**: `frontend/src/App.js`  
**Update**: Wrap routes with ErrorBoundary

```javascript
import ErrorBoundary from './components/ErrorBoundary';

function App() {
  return (
    <ErrorBoundary>
      <Router>
        <div className="min-h-screen ui-page">
          {/* rest of app */}
        </div>
      </Router>
    </ErrorBoundary>
  );
}
```

---

## 10. Add Logging Infrastructure

**File**: Create `backend/utils/logger.js` (NEW FILE)

```bash
npm install winston
```

```javascript
const winston = require('winston');
const path = require('path');

// ==========================================
// Winston Logger Setup
// ==========================================

const logDir = path.join(__dirname, '../logs');

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: 'property-api' },
  transports: [
    // Write all logs to console
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.printf(
          ({ timestamp, level, message, ...meta }) => {
            return `${timestamp} [${level}]: ${message} ${
              Object.keys(meta).length > 0 ? JSON.stringify(meta, null, 2) : ''
            }`;
          }
        )
      )
    }),
    // Write error logs to error.log
    new winston.transports.File({
      filename: path.join(logDir, 'error.log'),
      level: 'error'
    }),
    // Write all logs to combined.log
    new winston.transports.File({
      filename: path.join(logDir, 'combined.log')
    })
  ]
});

module.exports = logger;
```

**File**: `backend/server.js`  
**Add**: After imports (line 5)
```javascript
const logger = require('./utils/logger');
```

**Replace**: All `console.error` with `logger.error`
```javascript
// Before:
console.error('❌ MySQL connection error:', error.message);

// After:
logger.error('MySQL connection failed', { error: error.message });
```

---

## Summary: Installation Command

Run this to install all critical security packages:

```bash
cd c:\Users\sivag\Desktop\FSD\full-stack project\backend

npm install --save-dev jest@latest supertest
npm install helmet compression express-rate-limit express-validator winston
npm audit fix

cd ../frontend
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom jsdom

echo "✅ All dependencies installed!"
```

---

## Next: Testing

After these critical fixes, move to testing setup (see IMPROVEMENTS_CHECKLIST.md)

