#!/usr/bin/env bash
# ============================================================
# Bring up the marry-platform local Docker stack.
#
# Designed to run from **WSL** (Windows Subsystem for Linux) when Docker
# Desktop / dockerd is running in WSL2. Native Linux/macOS hosts also work —
# the script auto-detects the environment and only converts paths on WSL.
#
# Usage (from a WSL terminal):
#   cd /mnt/d/myprojects/marry-platform/deploy/docker
#   ./up.sh
#
#   ./up.sh --fg           # attach logs
#   ./up.sh --rebuild      # --no-cache rebuild
#   ./up.sh --reset        # DELETE persistent data (asks confirmation)
#   ./up.sh --reset --yes  # skip confirmation
#   ./up.sh --import <path>  # import a legacy pg_dump file after stack starts
#
# Path handling:
#   * Windows-style paths in .env (D:\foo, E:/bar) are auto-converted to
#     /mnt/d/foo, /mnt/e/bar so the WSL docker daemon can bind-mount them.
#   * Relative paths stay relative to deploy/docker.
#   * Absolute Unix paths pass through unchanged.
#
# The original .env is preserved untouched. Resolved paths are written to
# .env.resolved and passed to `docker compose --env-file .env.resolved`,
# which overrides the values in .env.
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
ENV_FILE="$SCRIPT_DIR/.env"
ENV_EXAMPLE="$SCRIPT_DIR/.env.example"
ENV_RESOLVED="$SCRIPT_DIR/.env.resolved"

FOREGROUND=0
REBUILD=0
RESET=0
FORCE_NO=0
IMPORT=""
for arg in "$@"; do
    case "$arg" in
        --fg|-f)        FOREGROUND=1 ;;
        --rebuild)      REBUILD=1 ;;
        --reset)        RESET=1 ;;
        --yes|-y)       FORCE_NO=1 ;;
        --import)       IMPORT="$2"; shift 2 ;;
        --import=*)     IMPORT="${arg#*=}" ;;
        -h|--help)
            sed -n '4,22p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
            exit 0 ;;
        *) echo "Unknown arg: $arg" >&2; exit 1 ;;
    esac
done

# ------------------------------------------------------------
# Environment detection
# ------------------------------------------------------------
IS_WSL=0
if [ -r /proc/version ] && grep -qi 'microsoft\|wsl' /proc/version 2>/dev/null; then
    IS_WSL=1
fi
if [ "$IS_WSL" = "1" ]; then
    echo "==> Detected WSL environment" >&2
else
    echo "==> Detected native Linux/macOS environment" >&2
fi

# Convert Windows path -> Unix path. Already-Unix absolute paths pass through;
# relative paths pass through (they will be anchored to $SCRIPT_DIR later).
to_unix_path() {
    local p="$1"
    if [ -z "$p" ]; then echo ""; return; fi
    if [[ "$p" =~ ^([A-Za-z]):[\\/](.*)$ ]]; then
        local drive
        drive="$(echo "${BASH_REMATCH[1]}" | tr '[:upper:]' '[:lower:]')"
        local rest="${BASH_REMATCH[2]}"
        rest="${rest//\\//}"
        echo "/mnt/$drive/$rest"
        return
    fi
    if [[ "$p" =~ ^\\\\\?\\([A-Za-z]):[\\/](.*)$ ]]; then
        local drive
        drive="$(echo "${BASH_REMATCH[1]}" | tr '[:upper:]' '[:lower:]')"
        local rest="${BASH_REMATCH[2]}"
        rest="${rest//\\//}"
        echo "/mnt/$drive/$rest"
        return
    fi
    echo "$p"
}

# Resolve a path: convert Windows -> /mnt, anchor relative -> $SCRIPT_DIR.
resolve_path() {
    local raw="$1"
    local p
    p="$(to_unix_path "$raw")"
    if [[ "$p" != /* ]]; then
        p="$SCRIPT_DIR/$p"
    fi
    p="$(cd "$(dirname "$p" 2>/dev/null)" 2>/dev/null && pwd)/$(basename "$p")" 2>/dev/null || echo "$p"
    echo "$p"
}

ensure_dir() {
    local p="$1"
    if [ -n "$p" ] && [ ! -d "$p" ]; then
        echo "    creating $p" >&2
        mkdir -p "$p"
    fi
}

# ------------------------------------------------------------
# 1. .env bootstrap
# ------------------------------------------------------------
if [ ! -f "$ENV_FILE" ]; then
    echo "==> Creating .env from .env.example"
    cp "$ENV_EXAMPLE" "$ENV_FILE"
    echo "    Edit $ENV_FILE (PG_DATA_PATH / UPLOADS_PATH / JWT_SECRET) if needed."
else
    echo "==> Using existing .env"
fi

# ------------------------------------------------------------
# 2. Build .env.resolved with path conversions + export into this shell
# ------------------------------------------------------------
echo "==> Building .env.resolved (with WSL path conversion if needed)"
: > "$ENV_RESOLVED"
while IFS= read -r line || [ -n "$line" ]; do
    if [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]]; then
        echo "$line" >> "$ENV_RESOLVED"
        continue
    fi
    if [[ "$line" =~ ^[[:space:]]*([^=]+)=(.*)$ ]]; then
        local_key="${BASH_REMATCH[1]}"
        local_val="${BASH_REMATCH[2]}"
        local_val="${local_val%\"}"; local_val="${local_val#\"}"
        local_val="${local_val%\'}"; local_val="${local_val#\'}"
        case "$local_key" in
            PG_DATA_PATH|UPLOADS_PATH)
                resolved="$(resolve_path "$local_val")"
                echo "$local_key=$resolved" >> "$ENV_RESOLVED"
                ;;
            *)
                echo "$line" >> "$ENV_RESOLVED"
                ;;
        esac
    else
        echo "$line" >> "$ENV_RESOLVED"
    fi
done < "$ENV_FILE"

set -a
# shellcheck disable=SC1090
. "$ENV_RESOLVED"
set +a

echo "==> Persistent paths"
echo "    PG_DATA_PATH = ${PG_DATA_PATH:-<unset>}"
echo "    UPLOADS_PATH = ${UPLOADS_PATH:-<unset>}"
ensure_dir "${PG_DATA_PATH:-}"
ensure_dir "${UPLOADS_PATH:-}"

# ------------------------------------------------------------
# 3. Re-aggregate Flyway SQL
# ------------------------------------------------------------
echo "==> Aggregating Flyway migrations"
if command -v pwsh >/dev/null 2>&1; then
    pwsh -NoProfile -File "$SCRIPT_DIR/scripts/build-db-init.ps1"
elif command -v powershell >/dev/null 2>&1; then
    powershell -NoProfile -File "$SCRIPT_DIR/scripts/build-db-init.ps1"
else
    bash "$SCRIPT_DIR/scripts/build-db-init.sh"
fi

# ------------------------------------------------------------
# 4. Optional destructive reset
# ------------------------------------------------------------
if [ "$RESET" = "1" ]; then
    echo "==> Reset requested. This DELETES persistent data."
    if [ "$FORCE_NO" != "1" ]; then
        read -r -p "    Type 'yes' to confirm deletion of pg data + uploads: " ans
        [ "$ans" = "yes" ] || { echo "Aborted."; exit 1; }
    fi
    for p in "$PG_DATA_PATH" "$UPLOADS_PATH"; do
        if [ -d "$p" ]; then
            echo "    removing $p"
            rm -rf "$p"
            ensure_dir "$p"
        else
            echo "    $p (missing, skipped)"
        fi
    done
    docker volume rm marry-redis-data >/dev/null 2>&1 || true
fi

# ------------------------------------------------------------
# 5. docker compose up
# ------------------------------------------------------------
ARGS=(--env-file "$ENV_RESOLVED" -f "$COMPOSE_FILE" up --build)
[ "$FOREGROUND" = "0" ] && ARGS+=(-d)
[ "$REBUILD"     = "1" ] && ARGS+=(--no-cache)
echo "==> docker compose ${ARGS[*]}"
docker compose "${ARGS[@]}"

# ------------------------------------------------------------
# 6. Wait for backend health
# ------------------------------------------------------------
echo "==> Waiting for backend health check (this may take ~60s on first start)..."
deadline=$(( $(date +%s) + 300 ))
status="starting"
while [ "$(date +%s)" -lt "$deadline" ]; do
    status="$(docker inspect --format '{{.State.Health.Status}}' marry-backend 2>/dev/null || true)"
    if [ "$status" = "healthy" ]; then
        echo "==> Backend is healthy OK"
        break
    fi
    sleep 3
done
if [ "$status" != "healthy" ]; then
    echo "Backend failed to become healthy. Try: docker compose -f $COMPOSE_FILE logs backend" >&2
    exit 1
fi

echo ""
if [ -n "$IMPORT" ]; then
    IMPORT_SCRIPT="$SCRIPT_DIR/db-import/import-dump.sh"
    if [ ! -x "$IMPORT_SCRIPT" ]; then
        IMPORT_SCRIPT="$SCRIPT_DIR/db-import/import-dump.sh"
    fi
    if [ ! -f "$IMPORT_SCRIPT" ]; then
        echo "import-dump.sh not found at $IMPORT_SCRIPT" >&2
        exit 1
    fi
    echo "==> Importing legacy dump: $IMPORT"
    bash "$IMPORT_SCRIPT" "$IMPORT"
fi

echo "OK marry-platform is up."
echo "   Frontend : http://localhost:${FRONTEND_PORT:-5173}"
echo "   Backend  : http://localhost:10045/api (inside the docker network)"
echo "   Login    : admin / admin123"