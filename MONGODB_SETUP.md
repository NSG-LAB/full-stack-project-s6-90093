# MongoDB Setup Guide

Your registration is failing because MongoDB is not running locally. Here are two solutions:

## Option 1: Use MongoDB Atlas (Cloud) - Recommended for Development

### Steps:
1. Go to https://www.mongodb.com/cloud/atlas
2. Click "Register" and create a free account
3. Create a new project
4. Build a cluster (choose Free tier)
5. Once cluster is created, click "Connect"
6. Choose "Drivers" and copy the connection string
7. Update your `.env` file:

```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/property-value-enhancement?retryWrites=true&w=majority
```

8. Replace `username` and `password` with your MongoDB Atlas credentials
9. Restart the backend server

## Option 2: Install MongoDB Locally on Windows

### Steps:
1. Download MongoDB Community from: https://www.mongodb.com/try/download/community
2. Run the installer and follow the installation wizard
3. During installation, check "Install MongoDB as a Service"
4. MongoDB will run automatically as a Windows service
5. Your current .env configuration should work:

```
MONGODB_URI=mongodb://localhost:27017/property-value-enhancement
```

6. Restart the backend server

## Option 3: Quick Test with Workaround

If you want to test the frontend UI without MongoDB:
1. The registration endpoint is prepared and ready
2. Just ensure MongoDB is set up using Option 1 or 2 above

## Verify MongoDB Connection

After setting up MongoDB, the backend terminal should show:
```
✅ MongoDB connected successfully
```

Then try registration again - it should work!
