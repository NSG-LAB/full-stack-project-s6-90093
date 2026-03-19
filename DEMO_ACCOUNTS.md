# Demo Account Testing Guide

## ✅ FIXED: Demo Account Credentials

The demo buttons on the **Home** and **Login** pages now use the correct credentials that match the backend.

### Demo Credentials:

| Role | Email | Password |
|------|-------|----------|
| **User** | `user@demo.com` | `User@123456` |
| **Admin** | `admin@demo.com` | `Admin@123456` |

## 🚀 How to Test Demo Accounts

### Option 1: From Home Page
1. Navigate to the home page (`/`)
2. Scroll down to "Try Demo Accounts" section
3. Click either:
   - **"Try User Demo"** - Access user features
   - **"Try Admin Demo"** - Access admin features

### Option 2: From Login Page
1. Navigate to login page (`/login`)
2. Click either:
   - **"Demo User"** button
   - **"Demo Admin"** button

## 📋 What You Can Test

### User Demo Account (`user@demo.com`)
- ✅ User dashboard with properties
- ✅ Property management (add, edit, delete)
- ✅ Valuation estimator
- ✅ ROI planner
- ✅ Recommendations
- ✅ Notifications
- ✅ Profile management

### Admin Demo Account (`admin@demo.com`)
- ✅ Admin dashboard with metrics
- ✅ Analytics (user activity, properties, performance)
- ✅ System monitoring (PM2 status, logs, metrics)
- ✅ Recommendation management
- ✅ User management
- ✅ View all properties
- ✅ System health monitoring

## ⚙️ Prerequisites

Make sure the backend is running and has demo accounts initialized:

### Backend Setup
```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# Create .env file with required variables
cp .env.example .env

# Run database migrations (if needed)
npm run db:migrate

# Start the backend
npm run dev
# or
npm start
```

### Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start the frontend dev server
npm start
# or
npm run dev
```

## 🔍 Verification

After clicking a demo button, you should:
1. ✅ See a success toast notification
2. ✅ Be redirected to the appropriate dashboard
3. ✅ See user/admin specific data and features
4. ✅ Be able to navigate throughout the app
5. ✅ See the user/admin name in the navigation header

## ❌ Troubleshooting

### If Demo Login Fails:

1. **Check Backend is Running:**
   ```bash
   curl http://localhost:5001/api/health
   ```
   Should return `200 OK`

2. **Verify Demo Accounts Exist in Database:**
   - Check MySQL database
   - Look for users with emails: `user@demo.com` and `admin@demo.com`
   - If not found, backend should create them automatically on startup

3. **Check Console Errors:**
   - Open browser DevTools (F12)
   - Check Console tab for API errors
   - Check Network tab to see login request/response

4. **Common Issues:**
   - **"Invalid credentials" error:** Verify correct password is being used
   - **"Cannot reach server" error:** Backend is not running
   - **Database connection error:** Check MySQL is running and accessible

## 📝 Reset Demo Accounts

If demo accounts get corrupted or deleted, they will be automatically recreated when the backend server restarts (see `backend/server.js` lines 99-135).

To manually reset:
1. Stop the backend server
2. Delete the demo user records from MySQL
3. Restart the backend - demo accounts will be recreated

```sql
-- Delete demo accounts from database (if needed)
DELETE FROM Users WHERE email IN ('user@demo.com', 'admin@demo.com');
```

## 📞 Support

If demo accounts still aren't working after checking the above:
1. Check the backend logs for errors
2. Verify MySQL connection is working
3. Ensure JWT_SECRET is set in .env
4. Check that all environment variables are properly configured

---

**Last Updated:** March 19, 2026
**Status:** ✅ Demo Accounts Fixed & Tested
