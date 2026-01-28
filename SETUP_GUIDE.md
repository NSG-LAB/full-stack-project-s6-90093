# Installation & Setup Guide

## 📋 Prerequisites
- Node.js v14 or higher
- MongoDB (local or cloud instance)
- npm or yarn package manager
- Git

## 🚀 Quick Start

### 1. Clone/Setup Repository
```bash
cd c:\Users\sivag\Desktop\FSD\full-stack project
```

### 2. Install Dependencies

#### Option A: Using Root Package.json (Recommended)
```bash
npm run setup
```

#### Option B: Manual Installation

**Backend Setup:**
```bash
cd backend
npm install
cp .env.example .env
```

**Frontend Setup:**
```bash
cd frontend
npm install
```

### 3. Configure Environment Variables

**Backend (.env file):**
```env
PORT=5000
MONGODB_URI=mongodb://localhost:27017/property-value-enhancement
JWT_SECRET=your_secure_jwt_secret_key_here
JWT_EXPIRE=7d
NODE_ENV=development
ADMIN_EMAIL=admin@propertyvalue.com
ADMIN_PASSWORD=admin_password_123
```

### 4. Start MongoDB
```bash
# If using local MongoDB
mongod
```

### 5. Run the Application

#### Start Both Backend and Frontend
```bash
npm run dev
```

#### Or Start Separately

**Backend:**
```bash
cd backend
npm run dev
```

**Frontend:**
```bash
cd frontend
npm start
```

Frontend will open at: **http://localhost:3000**
Backend API runs at: **http://localhost:5000/api**

## 📊 Database Setup

### Create Initial Admin User (Optional)
```bash
# Use the credentials from .env file or MongoDB Compass
db.users.insertOne({
  firstName: "Admin",
  lastName: "User",
  email: "admin@propertyvalue.com",
  password: "hashed_password",
  role: "admin",
  createdAt: new Date(),
  updatedAt: new Date()
})
```

## 🔑 Test Credentials

### Admin Account
- Email: `admin@propertyvalue.com`
- Password: `admin_password_123` (change in .env)

### User Account
- Register via the application UI

## 📁 Project Structure

```
full-stack project/
├── backend/
│   ├── models/
│   │   ├── User.js
│   │   ├── Property.js
│   │   └── Recommendation.js
│   ├── routes/
│   │   ├── auth.js
│   │   ├── users.js
│   │   ├── properties.js
│   │   └── recommendations.js
│   ├── middleware/
│   │   └── auth.js
│   ├── .env.example
│   ├── server.js
│   └── package.json
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── redux/
│   │   ├── services/
│   │   ├── App.js
│   │   └── index.js
│   ├── package.json
│   └── tailwind.config.js
├── package.json
└── README.md
```

## 🔗 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Properties
- `GET /api/properties` - Get all properties
- `POST /api/properties` - Submit new property
- `GET /api/properties/:id` - Get property details
- `PUT /api/properties/:id` - Update property
- `DELETE /api/properties/:id` - Delete property

### Recommendations
- `GET /api/recommendations` - Get all recommendations
- `GET /api/recommendations/property/:id` - Get recommendations for property
- `POST /api/recommendations` - Create recommendation (Admin only)
- `PUT /api/recommendations/:id` - Update recommendation (Admin only)
- `DELETE /api/recommendations/:id` - Delete recommendation (Admin only)

### Users
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users` - Get all users (Admin only)

## 🐛 Troubleshooting

### MongoDB Connection Error
- Ensure MongoDB is running
- Check connection string in .env
- Verify database credentials

### Port Already in Use
```bash
# Kill process on port 5000
lsof -ti:5000 | xargs kill -9

# Or change PORT in .env
PORT=5001
```

### Dependencies Installation Issues
```bash
# Clear npm cache
npm cache clean --force

# Remove node_modules and reinstall
rm -rf node_modules
npm install
```

## 📚 Additional Resources

- [MongoDB Documentation](https://docs.mongodb.com/)
- [Express.js Guide](https://expressjs.com/)
- [React Documentation](https://react.dev/)
- [Redux Toolkit](https://redux-toolkit.js.org/)
- [Tailwind CSS](https://tailwindcss.com/)

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## 📞 Support

For issues and support, contact the development team.

---

**Last Updated**: January 2026
