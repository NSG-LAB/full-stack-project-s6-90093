# Performance Monitoring Setup

This document describes the performance monitoring infrastructure implemented for the Property Value Enhancement platform.

## Overview

The monitoring system provides real-time insights into system performance, application health, and operational metrics. It includes:

- **PM2 Process Management**: Production-ready process management with clustering and monitoring
- **System Metrics Collection**: CPU, memory, disk, and network monitoring
- **Application Health Checks**: Database, Redis, and API endpoint monitoring
- **Log Management**: Centralized logging with rotation and analysis
- **Web Dashboard**: Real-time monitoring interface for administrators

## Components

### 1. PM2 Process Management

**Installation**: PM2 is installed as a development dependency in the backend.

**Configuration**: `backend/ecosystem.config.js` defines the production process configuration.

**Usage**:
```bash
# Start application in production mode
npm run pm2:start

# Start in development mode
npm run pm2:dev

# View process status
npm run pm2:status

# View logs
npm run pm2:logs

# Restart processes
npm run pm2:restart

# Stop processes
npm run pm2:stop
```

### 2. System Monitoring Script

**Location**: `scripts/monitor.sh`

**Features**:
- Collects system metrics (CPU, memory, disk, network)
- Checks application health (PM2 status, health endpoints, Redis)
- Monitors error logs
- Generates alerts for critical issues
- Outputs metrics to JSON file

**Usage**:
```bash
# Run monitoring manually
cd backend && ../scripts/monitor.sh

# Automated via cron (see crontab.example)
```

### 3. Web Monitoring Dashboard

**Access**: Available at `/monitoring` for admin users only

**Features**:
- Real-time system metrics display
- Application process status
- Health check indicators
- Recent log viewing
- Tabbed interface (Overview, System, Application, Logs)

### 4. Log Management

**Configuration**: `scripts/logrotate.conf`

**Features**:
- Automatic log rotation (daily)
- Compression and cleanup (7 days retention)
- PM2 log reloading

## Setup Instructions

### 1. Install Dependencies

```bash
cd backend
npm install
```

### 2. Configure Environment

Ensure your `.env` file contains all required variables (JWT_SECRET, database credentials, etc.).

### 3. Start with PM2

```bash
# Production mode
npm run pm2:start

# Development mode
npm run pm2:dev
```

### 4. Set up Automated Monitoring

```bash
# Copy crontab configuration
sudo cp scripts/crontab.example /etc/cron.d/property-app

# Set up log rotation
sudo cp scripts/logrotate.conf /etc/logrotate.d/property-app
```

### 5. Access Dashboard

1. Login as an admin user
2. Navigate to the "Monitoring" section in the navigation
3. View real-time metrics and system status

## Metrics Collected

### System Metrics
- CPU usage percentage
- Memory usage (total, used, percentage)
- Disk usage percentage
- Active network connections

### Application Metrics
- PM2 process status
- Health endpoint response codes
- Redis connection status
- Error count (last hour)
- Process memory and CPU usage

### Log Analysis
- Recent error logs
- Application logs with timestamps
- PM2 process logs

## Alerting

The monitoring system includes basic alerting for:
- High CPU usage (>90%)
- High memory usage (>90%)
- Application health check failures
- Database connection issues

Alerts are logged to the monitoring log file and can be extended to send email notifications or integrate with external monitoring services.

## Troubleshooting

### Common Issues

1. **PM2 not starting**: Check if all environment variables are set
2. **Monitoring script fails**: Ensure jq and curl are installed
3. **Dashboard not loading**: Verify admin authentication and API endpoints
4. **Logs not rotating**: Check logrotate configuration and permissions

### Manual Checks

```bash
# Check PM2 processes
pm2 list

# View PM2 logs
pm2 logs real-estate-backend

# Test health endpoint
curl http://localhost:5000/api/health

# Run monitoring manually
./scripts/monitor.sh
```

## Production Deployment

For production deployment:

1. Use PM2 ecosystem configuration
2. Set up cron jobs for automated monitoring
3. Configure log rotation
4. Set up proper file permissions
5. Consider integrating with external monitoring (DataDog, New Relic, etc.)

## Security Considerations

- Monitoring dashboard requires admin authentication
- API endpoints are protected with JWT tokens
- Log files contain sensitive information - ensure proper permissions
- Consider encrypting log storage in production