#!/bin/bash

# Performance Monitoring Script for Real Estate Platform
# This script collects system and application metrics

LOG_FILE="./logs/monitoring.log"
METRICS_FILE="./logs/metrics.json"

# Create logs directory if it doesn't exist
mkdir -p ./logs

# Function to log messages
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" >> "$LOG_FILE"
}

# Function to collect system metrics
collect_system_metrics() {
    echo "Collecting system metrics..."

    # CPU usage
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')

    # Memory usage
    MEM_TOTAL=$(free -m | awk 'NR==2{printf "%.2f", $2/1024}')
    MEM_USED=$(free -m | awk 'NR==2{printf "%.2f", $3/1024}')
    MEM_USAGE=$(free -m | awk 'NR==2{printf "%.2f", $3*100/$2}')

    # Disk usage
    DISK_USAGE=$(df / | awk 'NR==2{print $5}' | sed 's/%//')

    # Network connections
    ACTIVE_CONNECTIONS=$(netstat -tun | grep ESTABLISHED | wc -l)

    echo "{
        \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",
        \"system\": {
            \"cpu_usage_percent\": $CPU_USAGE,
            \"memory_total_gb\": $MEM_TOTAL,
            \"memory_used_gb\": $MEM_USED,
            \"memory_usage_percent\": $MEM_USAGE,
            \"disk_usage_percent\": $DISK_USAGE,
            \"active_connections\": $ACTIVE_CONNECTIONS
        }
    }" > "$METRICS_FILE"
}

# Function to check application health
check_application_health() {
    echo "Checking application health..."

    # Check if PM2 process is running
    PM2_STATUS=$(pm2 jlist | jq -r '.[] | select(.name=="real-estate-backend") | .pm2_env.status' 2>/dev/null || echo "stopped")

    # Check database connection via health endpoint
    if command -v curl &> /dev/null; then
        HEALTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5000/api/health 2>/dev/null || echo "000")
    else
        HEALTH_RESPONSE="000"
    fi

    # Check Redis connection
    if command -v redis-cli &> /dev/null; then
        REDIS_PING=$(redis-cli ping 2>/dev/null || echo "PONG_FAILED")
    else
        REDIS_PING="CLI_NOT_FOUND"
    fi

    # Update metrics file with application health
    jq --arg pm2_status "$PM2_STATUS" \
       --arg health_code "$HEALTH_RESPONSE" \
       --arg redis_ping "$REDIS_PING" \
       '.application = {
           "pm2_status": $pm2_status,
           "health_endpoint_status": $health_code,
           "redis_ping": $redis_ping
       }' "$METRICS_FILE" > "${METRICS_FILE}.tmp" && mv "${METRICS_FILE}.tmp" "$METRICS_FILE"
}

# Function to check for errors in logs
check_error_logs() {
    echo "Checking for errors in logs..."

    # Count errors in the last hour
    ERROR_COUNT=$(grep -c "ERROR" ./logs/*.log 2>/dev/null | awk '{sum += $1} END {print sum+0}')

    # Get recent errors
    RECENT_ERRORS=$(tail -n 50 ./logs/*.log 2>/dev/null | grep -i error | tail -n 5)

    jq --arg error_count "$ERROR_COUNT" \
       --arg recent_errors "$RECENT_ERRORS" \
       '.errors = {
           "total_errors_last_hour": ($error_count | tonumber),
           "recent_errors": $recent_errors
       }' "$METRICS_FILE" > "${METRICS_FILE}.tmp" && mv "${METRICS_FILE}.tmp" "$METRICS_FILE"
}

# Function to send alerts (placeholder for future implementation)
send_alerts() {
    # Check if CPU usage is above 90%
    CPU_USAGE=$(jq -r '.system.cpu_usage_percent' "$METRICS_FILE" 2>/dev/null)
    if (( $(echo "$CPU_USAGE > 90" | bc -l 2>/dev/null || echo 0) )); then
        log "ALERT: High CPU usage detected: ${CPU_USAGE}%"
    fi

    # Check if memory usage is above 90%
    MEM_USAGE=$(jq -r '.system.memory_usage_percent' "$METRICS_FILE" 2>/dev/null)
    if (( $(echo "$MEM_USAGE > 90" | bc -l 2>/dev/null || echo 0) )); then
        log "ALERT: High memory usage detected: ${MEM_USAGE}%"
    fi

    # Check if application is not healthy
    HEALTH_CODE=$(jq -r '.application.health_endpoint_status' "$METRICS_FILE" 2>/dev/null)
    if [ "$HEALTH_CODE" != "200" ]; then
        log "ALERT: Application health check failed with code: ${HEALTH_CODE}"
    fi
}

# Main execution
log "Starting performance monitoring..."

collect_system_metrics
check_application_health
check_error_logs
send_alerts

log "Performance monitoring completed"

# Display current metrics
echo "Current Metrics:"
cat "$METRICS_FILE" | jq . 2>/dev/null || cat "$METRICS_FILE"