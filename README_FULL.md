# 🏠 Property Value Enhancement Platform

![Status](https://img.shields.io/badge/status-development-blue)
![License](https://img.shields.io/badge/license-proprietary-red)
![Node Version](https://img.shields.io/badge/node-14+-green)

A comprehensive full-stack platform designed to help Indian middle-class homeowners enhance the value of their residential properties with data-driven recommendations and expert insights.

## ✨ Features

### 👥 For Users
- 🏠 Submit property details for analysis
- 💡 View personalized property enhancement recommendations
- 📊 Understand ROI potential for improvements
- 💰 Compare cost-benefit analysis
- 🎯 Track property value improvements
- 📱 Responsive mobile-friendly interface

### 🔧 For Admins
- 📝 Create and manage property improvement recommendations
- 📈 Curate region-specific enhancement suggestions
- 👥 Manage user submissions
- 📊 View analytics and trends
- 🎨 Update recommendation categories
- 🏪 Manage property market data

## 🛠️ Tech Stack

### Backend
- **Node.js** + **Express.js** - REST API server
- **MongoDB** - NoSQL database
- **JWT** - Secure authentication
- **bcryptjs** - Password hashing
- **Mongoose** - Database ODM

### Frontend
- **React.js** - UI framework
- **Redux Toolkit** - State management
- **React Router** - Navigation
- **Tailwind CSS** - Styling
- **Axios** - HTTP client
- **React Toastify** - Notifications

## 📦 Project Structure

```
full-stack project/
├── backend/
│   ├── models/          # MongoDB schemas (User, Property, Recommendation)
│   ├── routes/          # API endpoints
│   ├── middleware/      # Auth & error handling
│   ├── .env.example     # Environment variables
│   ├── server.js        # Entry point
│   └── package.json
├── frontend/
│   ├── src/
│   │   ├── components/  # Reusable components
│   │   ├── pages/       # Page components
│   │   ├── redux/       # State management
│   │   ├── services/    # API integration
│   │   ├── App.js
│   │   └── index.js
│   ├── public/
│   ├── package.json
│   └── tailwind.config.js
├── SETUP_GUIDE.md       # Installation instructions
├── API_DOCUMENTATION.md # API reference
└── DEVELOPMENT_GUIDE.md # Development guidelines
```

## 🚀 Quick Start

### Prerequisites
- Node.js v14+
- MongoDB (local or Atlas)
- npm or yarn

### Installation

1. **Clone/Navigate to Project**
```bash
cd c:\Users\sivag\Desktop\FSD\full-stack project
```

2. **Install Dependencies**
```bash
npm run setup
```

3. **Configure Environment**
```bash
cd backend
cp .env.example .env
# Edit .env with your configurations
```

4. **Start Application**
```bash
# From root directory
npm run dev
```

This will start:
- Backend API on `http://localhost:5000`
- Frontend UI on `http://localhost:3000`

### Manual Start (If needed)

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

## 📚 Documentation

- **[Setup Guide](SETUP_GUIDE.md)** - Detailed installation and configuration
- **[API Documentation](API_DOCUMENTATION.md)** - Complete API reference
- **[Development Guide](DEVELOPMENT_GUIDE.md)** - Development workflow and guidelines

## 🔐 Authentication

The platform uses JWT-based authentication:

1. Users register or login
2. Backend generates JWT token
3. Token stored in localStorage
4. Included in all API requests
5. Protected routes verified on backend

## 📊 Enhancement Categories

The platform provides recommendations across 9 categories:

1. **🍳 Kitchen & Bathroom** - Modern fittings, fixtures, storage
2. **🪵 Flooring** - Upgraded materials, designs
3. **🎨 Wall & Paint** - Color schemes, treatments
4. **💡 Lighting & Fixtures** - Modern lighting solutions
5. **🌿 Garden & Outdoor** - Landscaping, outdoor spaces
6. **🔒 Safety & Security** - Locks, CCTV, security systems
7. **⚡ Energy Efficiency** - Solar, insulation, water systems
8. **🛋️ Interior Design** - Space optimization, décor
9. **🔧 Electrical & Plumbing** - Modern infrastructure upgrades

## 🔗 API Endpoints

### Authentication
```
POST   /api/auth/register
POST   /api/auth/login
```

### Properties
```
GET    /api/properties
POST   /api/properties
GET    /api/properties/:id
PUT    /api/properties/:id
DELETE /api/properties/:id
```

### Recommendations
```
GET    /api/recommendations
GET    /api/recommendations/property/:id
POST   /api/recommendations              (Admin)
PUT    /api/recommendations/:id          (Admin)
DELETE /api/recommendations/:id          (Admin)
```

### Users
```
GET    /api/users/profile
PUT    /api/users/profile
GET    /api/users                        (Admin)
```

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for detailed endpoints.

## 🧪 Testing

### Test Credentials
- **Admin**: admin@propertyvalue.com / admin_password_123
- **User**: Register via UI

### Manual Testing
1. Start the application
2. Register/Login user
3. Submit property details
4. View recommendations
5. (Admin) Create new recommendations

## 🌐 Deployment

### Backend Deployment (Heroku/Railway/Render)
1. Push code to Git
2. Connect to deployment platform
3. Set environment variables
4. Deploy

### Frontend Deployment (Vercel/Netlify)
1. Build: `npm run build`
2. Deploy build folder
3. Set API proxy in package.json

## 🔒 Security Features

- ✅ JWT-based authentication
- ✅ Password hashing with bcrypt
- ✅ Input validation & sanitization
- ✅ CORS protection
- ✅ Environment variables for secrets
- ✅ Role-based access control
- ✅ Protected API endpoints

## 🚧 Future Enhancements

- [ ] Video tutorials for improvements
- [ ] Contractor/service provider network
- [ ] AR visualization of changes
- [ ] Mobile app (React Native)
- [ ] Payment integration
- [ ] Email notifications
- [ ] Advanced analytics dashboard
- [ ] Integration with property listing platforms

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Test thoroughly
4. Submit pull request

## 📞 Support & Contact

For issues, questions, or support:
- Create an issue in the repository
- Contact the development team
- Check [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) for troubleshooting

## 📄 License

This project is proprietary and intended for the Indian residential property market.

## 👨‍💼 Team

- Full-stack development team
- Focus on Indian real estate market

## 🙏 Acknowledgments

- Built for Indian middle-class homeowners
- Designed with market expertise
- Crafted for property value enhancement

---

## 📈 Project Status

- ✅ Database schema designed
- ✅ API endpoints developed
- ✅ Frontend UI components created
- ✅ Authentication implemented
- ✅ Redux state management setup
- 🔄 Testing phase
- 📅 Production deployment planned

**Last Updated**: January 21, 2026

For detailed information, refer to:
- [Setup Guide](SETUP_GUIDE.md)
- [API Documentation](API_DOCUMENTATION.md)
- [Development Guide](DEVELOPMENT_GUIDE.md)
