#!/usr/bin/env bash
# Stop the marry-platform Docker stack.
#
# Usage:
#   ./down.sh            # stop + remove containers (keeps data + volumes)
#   ./down.sh --volumes  # also remove the named Redis volume
#   ./down.sh --images   # also remove the local backend/frontend images

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
ENV_RESOLVED="$SCRIPT_DIR/.env.resolved"

VOLUMES=0
IMAGES=0
PROJECT=0
for arg in "$@"; do
    case "$arg" in
        -v|--volumes) VOLUMES=1 ;;
        -i|--images)  IMAGES=1 ;;
        -p|--project) PROJECT=1 ;;
        --all)        VOLUMES=1; IMAGES=1; PROJECT=1 ;;
        -h|--help)
            sed -n '2,9p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
            exit 0 ;;
        *) echo "Unknown arg: $arg" >&2; exit 1 ;;
    esac
done

ARGS=(--env-file "$ENV_RESOLVED" -f "$COMPOSE_FILE" down)
[ "$PROJECT"  = "1" ] && ARGS+=(--remove-orphans)
[ "$VOLUMES"  = "1" ] && ARGS+=(-v)
echo "==> docker compose ${ARGS[*]}"
docker compose "${ARGS[@]}"

if [ "$IMAGES" = "1" ]; then
    for img in marry-platform-backend:local marry-platform-frontend:local; do
        echo "    removing $img"
        docker rmi "$img" >/dev/null 2>&1 || true
    done
fi

echo "OK Stack stopped."