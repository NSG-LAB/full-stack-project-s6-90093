#!/bin/bash

# Database Backup Script for Property Value Enhancement App
# This script creates automated backups of the MySQL database

set -e  # Exit on any error

# Configuration
BACKUP_DIR="/var/backups/property-app"
RETENTION_DAYS=30
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_NAME="property_app_backup_${TIMESTAMP}.sql.gz"

# Database credentials (should be passed as environment variables)
DB_HOST=${MYSQL_HOST:-"localhost"}
DB_PORT=${MYSQL_PORT:-"3306"}
DB_NAME=${MYSQL_DB:-"property_app"}
DB_USER=${MYSQL_USER}
DB_PASSWORD=${MYSQL_PASSWORD}

# Logging
LOG_FILE="/var/log/property-app/backup.log"
mkdir -p "$(dirname "$LOG_FILE")"

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

error_exit() {
    log "ERROR: $1"
    exit 1
}

# Validate environment
if [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ]; then
    error_exit "Database credentials not provided. Set MYSQL_USER and MYSQL_PASSWORD environment variables."
fi

# Create backup directory
mkdir -p "$BACKUP_DIR" || error_exit "Failed to create backup directory: $BACKUP_DIR"

log "Starting database backup: $BACKUP_NAME"

# Perform backup
mysqldump \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USER" \
    --password="$DB_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    --all-databases \
    "$DB_NAME" | gzip > "${BACKUP_DIR}/${BACKUP_NAME}" || error_exit "Database backup failed"

# Verify backup
if [ ! -f "${BACKUP_DIR}/${BACKUP_NAME}" ]; then
    error_exit "Backup file was not created"
fi

BACKUP_SIZE=$(stat -c%s "${BACKUP_DIR}/${BACKUP_NAME}" 2>/dev/null || stat -f%z "${BACKUP_DIR}/${BACKUP_NAME}" 2>/dev/null || echo "unknown")
log "Backup completed successfully. Size: $BACKUP_SIZE bytes"

# Clean up old backups
log "Cleaning up backups older than $RETENTION_DAYS days"
find "$BACKUP_DIR" -name "property_app_backup_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete

# Verify backup integrity (optional)
log "Verifying backup integrity"
gunzip -c "${BACKUP_DIR}/${BACKUP_NAME}" | head -n 10 > /dev/null || error_exit "Backup verification failed"

log "Backup process completed successfully"

# Optional: Upload to cloud storage (uncomment and configure as needed)
# aws s3 cp "${BACKUP_DIR}/${BACKUP_NAME}" "s3://your-backup-bucket/${BACKUP_NAME}"
# az storage blob upload --account-name yourstorage --container-name backups --name "$BACKUP_NAME" --file "${BACKUP_DIR}/${BACKUP_NAME}"

exit 0