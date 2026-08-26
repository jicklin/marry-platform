#!/usr/bin/env bash
# ============================================================
# Import a legacy PostgreSQL dump into the running marry-platform stack.
#
# Supports three pg_dump output formats:
#   - Plain-text SQL  (pg_dump / pg_dump -Fp)        -> run via psql
#   - Custom binary   (pg_dump -Fc)                  -> run via pg_restore
#   - Tar archive     (pg_dump -Ft)                  -> run via pg_restore
#
# The dump can be on the HOST filesystem at any path. We `docker cp` it
# into the postgres container, then exec the right pg_restore/psql
# command inside.
#
# Usage:
#   ./import-dump.sh /path/to/legacy.dump
#   ./import-dump.sh /path/to/legacy.sql
#   ./import-dump.sh /path/to/legacy.tar
#
#   ./import-dump.sh --help
#
# Options (after positional arg):
#   --clean           Drop existing objects before restoring (DANGEROUS —
#                      wipes the admin/demo seed!). Off by default.
#   --drop-schema     DROP SCHEMA public CASCADE before restoring. Same
#                      warning as --clean. Off by default.
#   --no-owner        Don't set ownership of restored objects. Recommended.
#   --schema=NAME     Only restore this schema (default: public).
#   --no-public       Skip the public schema entirely.
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/../docker-compose.yml"
ENV_RESOLVED="$SCRIPT_DIR/../.env.resolved"

# ---- argument parsing ----
DUMP_FILE=""
CLEAN=0
DROP_SCHEMA=0
NO_OWNER=1       # default ON for safety
SCHEMA="public"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --clean)        CLEAN=1; shift ;;
        --drop-schema)  DROP_SCHEMA=1; shift ;;
        --no-owner)     NO_OWNER=1; shift ;;
        --owner)        NO_OWNER=0; shift ;;
        --schema=*)     SCHEMA="${1#*=}"; shift ;;
        --schema)       SCHEMA="$2"; shift 2 ;;
        --no-public)    SCHEMA=""; shift ;;
        -h|--help)
            sed -n '2,23p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
            exit 0 ;;
        -*)
            echo "Unknown option: $1" >&2
            exit 1 ;;
        *)
            if [[ -n "$DUMP_FILE" ]]; then
                echo "Only one dump file may be given." >&2
                exit 1
            fi
            DUMP_FILE="$1"; shift ;;
    esac
done

if [[ -z "$DUMP_FILE" ]]; then
    echo "Usage: $0 <path-to-dump> [--clean] [--drop-schema] [--no-public]" >&2
    exit 1
fi
if [[ ! -r "$DUMP_FILE" ]]; then
    echo "Dump file not found or unreadable: $DUMP_FILE" >&2
    exit 1
fi

# ---- resolve the docker compose file ----
if [[ ! -f "$COMPOSE_FILE" ]]; then
    COMPOSE_FILE="$SCRIPT_DIR/../docker-compose.yml"
fi
if [[ ! -f "$COMPOSE_FILE" ]]; then
    echo "docker-compose.yml not found. Run \`./up.sh\` first to bring up the stack." >&2
    exit 1
fi

# ---- sanity: stack is up ----
if ! docker compose -f "$COMPOSE_FILE" --env-file "${ENV_RESOLVED:-$SCRIPT_DIR/../.env.resolved}" ps --status running postgres 2>/dev/null | grep -q postgres; then
    echo "Postgres container is not running. Start the stack with ./up.sh first." >&2
    exit 1
fi

POSTGRES_USER="${POSTGRES_USER:-marry}"
POSTGRES_DB="${POSTGRES_DB:-marry_platform}"

# ---- detect format ----
DUMP_ABS="$(cd "$(dirname "$DUMP_FILE")" && pwd)/$(basename "$DUMP_FILE")"
DUMP_BASENAME="$(basename "$DUMP_FILE")"
DUMP_EXT="${DUMP_BASENAME##*.}"

echo "==> Importing: $DUMP_ABS"
echo "    User / DB : $POSTGRES_USER @ $POSTGRES_DB"
echo "    Schema    : ${SCHEMA:-<none>}"
echo "    Flags     : clean=$CLEAN drop-schema=$DROP_SCHEMA no-owner=$NO_OWNER"

# ---- safety prompts ----
if [[ $CLEAN -eq 1 ]]; then
    echo "" >&2
    echo "WARNING: --clean will DROP any existing objects in the target database" >&2
    echo "         before restoring. The admin/demo seed WILL be wiped." >&2
    read -r -p "Type 'yes' to continue: " ans
    [[ "$ans" == "yes" ]] || { echo "Aborted."; exit 1; }
fi
if [[ $DROP_SCHEMA -eq 1 ]]; then
    echo "" >&2
    echo "WARNING: --drop-schema will DROP SCHEMA $SCHEMA CASCADE." >&2
    read -r -p "Type 'yes' to continue: " ans
    [[ "$ans" == "yes" ]] || { echo "Aborted."; exit 1; }
fi

# ---- copy into container ----
CONTAINER_DUMP="/tmp/import-$$-$DUMP_BASENAME"
echo "==> Copying dump into postgres container"
docker cp "$DUMP_ABS" "marry-postgres:$CONTAINER_DUMP"

cleanup() {
    docker exec marry-postgres rm -f "$CONTAINER_DUMP" >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

# ---- run restore ----
is_text=0
if file "$DUMP_ABS" 2>/dev/null | grep -qi 'text\|SQL\|ASCII'; then
    is_text=1
elif head -c 100 "$DUMP_ABS" | grep -q '^--\|CREATE TABLE\|INSERT INTO'; then
    is_text=1
fi

if [[ $is_text -eq 1 ]]; then
    # Plain-text SQL: use psql
    echo "==> Detected plain-text SQL — using psql"
    PSQL_ARGS=(-v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$CONTAINER_DUMP")
    docker exec -u postgres marry-postgres psql "${PSQL_ARGS[@]}"
else
    # Custom (-Fc) or tar (-Ft): use pg_restore
    echo "==> Detected binary/tar dump — using pg_restore"
    PG_ARGS=(--no-owner --no-privileges -U "$POSTGRES_USER" -d "$POSTGRES_DB")
    [[ $NO_OWNER -eq 1 ]] && PG_ARGS+=(--no-owner)
    [[ -n "$SCHEMA" ]]    && PG_ARGS+=(--schema="$SCHEMA")
    [[ $CLEAN -eq 1 ]]    && PG_ARGS+=(--clean --if-exists)
    PG_ARGS+=("$CONTAINER_DUMP")
    docker exec -u postgres marry-postgres pg_restore "${PG_ARGS[@]}" || {
        rc=$?
        echo "" >&2
        echo "pg_restore exited with code $rc." >&2
        echo "Common causes:" >&2
        echo "  * Schema already exists (use --clean or --drop-schema)" >&2
        echo "  * Row conflicts (try without ON_ERROR_STOP or wrap in BEGIN/COMMIT)" >&2
        echo "  * Field type mismatch (verify field mappings)" >&2
        exit $rc
    }
fi

echo ""
echo "OK Import finished."
echo "   Verify with: docker exec -it marry-postgres psql -U $POSTGRES_USER -d $POSTGRES_DB -c '\\dt'"