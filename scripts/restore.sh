#!/bin/bash

# Database Restore Script for Property Value Enhancement App
# This script restores the MySQL database from a backup

set -e  # Exit on any error

# Configuration
BACKUP_DIR="/var/backups/property-app"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Database credentials (should be passed as environment variables)
DB_HOST=${MYSQL_HOST:-"localhost"}
DB_PORT=${MYSQL_PORT:-"3306"}
DB_NAME=${MYSQL_DB:-"property_app"}
DB_USER=${MYSQL_USER}
DB_PASSWORD=${MYSQL_PASSWORD}

# Logging
LOG_FILE="/var/log/property-app/restore.log"
mkdir -p "$(dirname "$LOG_FILE")"

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

error_exit() {
    log "ERROR: $1"
    exit 1
}

show_usage() {
    echo "Usage: $0 <backup_file>"
    echo "Example: $0 property_app_backup_20231201_120000.sql.gz"
    echo ""
    echo "Available backups:"
    ls -la "$BACKUP_DIR"/property_app_backup_*.sql.gz 2>/dev/null || echo "No backups found in $BACKUP_DIR"
    exit 1
}

# Validate arguments
if [ $# -ne 1 ]; then
    show_usage
fi

BACKUP_FILE="$1"

# Validate environment
if [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ]; then
    error_exit "Database credentials not provided. Set MYSQL_USER and MYSQL_PASSWORD environment variables."
fi

# Check if backup file exists
if [ ! -f "$BACKUP_FILE" ]; then
    # Try with full path
    BACKUP_FILE="${BACKUP_DIR}/${BACKUP_FILE}"
    if [ ! -f "$BACKUP_FILE" ]; then
        error_exit "Backup file not found: $1"
    fi
fi

log "Starting database restore from: $BACKUP_FILE"

# Verify backup file
if ! gzip -t "$BACKUP_FILE" 2>/dev/null; then
    error_exit "Backup file is corrupted or not a valid gzip file"
fi

# Create a backup of current database before restore (safety measure)
CURRENT_BACKUP="${BACKUP_DIR}/pre_restore_backup_${TIMESTAMP}.sql.gz"
log "Creating pre-restore backup: $CURRENT_BACKUP"

mysqldump \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USER" \
    --password="$DB_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    "$DB_NAME" | gzip > "$CURRENT_BACKUP" || error_exit "Failed to create pre-restore backup"

log "Pre-restore backup created: $CURRENT_BACKUP"

# Confirm restore action
echo "WARNING: This will overwrite the current database!"
echo "A backup of the current database has been created: $CURRENT_BACKUP"
read -p "Are you sure you want to continue? (yes/no): " -r
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    log "Restore cancelled by user"
    exit 0
fi

# Stop application (if running with PM2)
if command -v pm2 &> /dev/null; then
    log "Stopping application with PM2"
    pm2 stop all || true
fi

# Perform restore
log "Restoring database from backup"
gunzip -c "$BACKUP_FILE" | mysql \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USER" \
    --password="$DB_PASSWORD" \
    "$DB_NAME" || error_exit "Database restore failed"

# Verify restore
log "Verifying database restore"
TABLE_COUNT=$(mysql \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USER" \
    --password="$DB_PASSWORD" \
    --execute="USE $DB_NAME; SHOW TABLES;" | wc -l)

if [ "$TABLE_COUNT" -gt 0 ]; then
    log "Database restore completed successfully. Tables found: $((TABLE_COUNT - 1))"
else
    error_exit "Database restore verification failed - no tables found"
fi

# Restart application
if command -v pm2 &> /dev/null; then
    log "Restarting application with PM2"
    pm2 restart all || true
fi

log "Database restore process completed successfully"

exit 0