#!/usr/bin/env bash
# PostgreSQL + uploaded files -> encrypted restic repository over rclone/WebDAV.
# Usage: ./scripts/backup-webdav.sh [db|files|all] [--prune]

set -Eeuo pipefail
umask 077

MODE="${1:-all}"
PRUNE="${2:-}"
CONFIG_FILE="${BACKUP_CONFIG:-${HOME}/.config/marry-platform/backup.env}"

case "$MODE" in
  db|files|all) ;;
  *) echo "Usage: $0 [db|files|all] [--prune]" >&2; exit 2 ;;
esac
if [[ -n "$PRUNE" && "$PRUNE" != "--prune" ]]; then
  echo "Unknown option: $PRUNE" >&2
  exit 2
fi
if [[ ! -r "$CONFIG_FILE" ]]; then
  echo "Backup config is missing or unreadable: $CONFIG_FILE" >&2
  echo "Copy scripts/backup.env.example there and fill in the values." >&2
  exit 1
fi

# shellcheck disable=SC1090
source "$CONFIG_FILE"

PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGDATABASE="${PGDATABASE:-marry_platform}"
PGUSER="${PGUSER:-root}"
MARRY_FILE_PATH="${MARRY_FILE_PATH:-/Users/yoyo/Documents/马呦呦上学记}"
BACKUP_WORK_DIR="${BACKUP_WORK_DIR:-${HOME}/.cache/marry-platform-backup}"
RESTIC_REPOSITORY="${RESTIC_REPOSITORY:-}"
RESTIC_PASSWORD_FILE="${RESTIC_PASSWORD_FILE:-}"
KEEP_DAILY="${KEEP_DAILY:-7}"
KEEP_WEEKLY="${KEEP_WEEKLY:-4}"
KEEP_MONTHLY="${KEEP_MONTHLY:-12}"

if [[ -z "$RESTIC_REPOSITORY" ]]; then
  echo "RESTIC_REPOSITORY is required (for example rclone:infinicloud:marry-platform/restic)." >&2
  exit 1
fi
if [[ -z "$RESTIC_PASSWORD_FILE" || ! -r "$RESTIC_PASSWORD_FILE" ]]; then
  echo "RESTIC_PASSWORD_FILE must point to a readable file containing the restic repository password." >&2
  exit 1
fi

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Required command not found: $1" >&2
    exit 1
  }
}

require_command restic
require_command rclone
if [[ "$MODE" == "db" || "$MODE" == "all" ]]; then
  require_command pg_dump
  require_command pg_restore
fi
if [[ "$MODE" == "files" || "$MODE" == "all" ]]; then
  if [[ ! -d "$MARRY_FILE_PATH" ]]; then
    echo "Upload directory does not exist: $MARRY_FILE_PATH" >&2
    exit 1
  fi
fi

export PGHOST PGPORT PGDATABASE PGUSER
export RESTIC_REPOSITORY RESTIC_PASSWORD_FILE
if [[ -n "${PGPASSFILE:-}" ]]; then
  export PGPASSFILE
fi
if [[ -n "${RCLONE_CONFIG:-}" ]]; then
  export RCLONE_CONFIG
fi

mkdir -p "$BACKUP_WORK_DIR/database"
LOCK_DIR="${BACKUP_WORK_DIR}/.lock"
if ! mkdir "$LOCK_DIR" 2>/dev/null; then
  echo "Another backup appears to be running (lock: $LOCK_DIR)." >&2
  exit 1
fi

DB_DUMP="${BACKUP_WORK_DIR}/database/${PGDATABASE}.dump"
cleanup() {
  rm -f "$DB_DUMP"
  rmdir "$LOCK_DIR" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

BACKUP_PATHS=()
if [[ "$MODE" == "db" || "$MODE" == "all" ]]; then
  echo "==> Dumping PostgreSQL database ${PGDATABASE}@${PGHOST}:${PGPORT}"
  rm -f "$DB_DUMP"
  pg_dump \
    --format=custom \
    --compress=6 \
    --no-owner \
    --file="$DB_DUMP"
  pg_restore --list "$DB_DUMP" >/dev/null
  BACKUP_PATHS+=("$DB_DUMP")
fi

if [[ "$MODE" == "files" || "$MODE" == "all" ]]; then
  echo "==> Adding uploaded files: $MARRY_FILE_PATH"
  BACKUP_PATHS+=("$MARRY_FILE_PATH")
fi

echo "==> Uploading encrypted incremental backup to $RESTIC_REPOSITORY"
restic backup \
  --tag marry-platform \
  --tag "mode-${MODE}" \
  --exclude-caches \
  "${BACKUP_PATHS[@]}"

if [[ "$PRUNE" == "--prune" ]]; then
  echo "==> Applying retention policy and pruning unreferenced data"
  restic forget \
    --tag marry-platform \
    --tag "mode-${MODE}" \
    --keep-daily "$KEEP_DAILY" \
    --keep-weekly "$KEEP_WEEKLY" \
    --keep-monthly "$KEEP_MONTHLY" \
    --prune
fi

echo "==> Latest snapshots"
restic snapshots --tag marry-platform --latest 5
echo "Backup completed successfully."
