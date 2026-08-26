# marry-platform — Local Docker Stack

One command brings up the whole platform locally:

- **PostgreSQL 16** with the Flyway schema + seeds auto-applied on first boot
- **Redis 7** for JWT blacklist / online users
- **Spring Boot 3.3 backend** (`/api`)
- **Vue 3 SPA** served by Nginx (port `5173` on the host, matching Vite's default)

## Quick start

### WSL (recommended when Docker Desktop uses the WSL2 backend)

```bash
cd /mnt/d/myprojects/marry-platform/deploy/docker
./up.sh
```

Or from Windows PowerShell, forward into WSL:

```powershell
cd D:\myprojects\marry-platform\deploy\docker
.\up-wsl.ps1
```

### Native Linux / macOS

```bash
cd deploy/docker
./up.sh
```

### Windows (Docker Desktop with WSL2 backend, no WSL terminal handy)

```powershell
cd D:\myprojects\marry-platform\deploy\docker
.\up.ps1
```

That is it. Wait ~60 s for the backend health check to flip green, then open
<http://localhost:5173> and log in with `admin / admin123`.

> The frontend port defaults to **5173** (Vite's dev port). If that port is
> taken on your machine, change `FRONTEND_PORT` in `.env` to anything else.

## WSL path handling

When `up.sh` is run from **inside WSL**, or `up.ps1` is run from Windows
PowerShell with Docker Desktop's WSL2 backend, the docker daemon only sees
Unix paths. The scripts therefore:

1. Read your `deploy/docker/.env` (where you can keep writing normal Windows
   paths like `D:\my-data\marry-pg`).
2. Convert any `D:\…`, `E:/…`, `\\?\D:\…` paths into `/mnt/d/…`, `/mnt/e/…`.
3. Write the converted values to `deploy/docker/.env.resolved` and pass
   it to `docker compose --env-file .env.resolved`.

Your `.env` is never modified. The `docker-compose.yml` continues to use
`${PG_DATA_PATH:-./.pgdata}` etc., so the resolved values are what
actually get mounted.

| You write in `.env`         | Container sees                          |
|-----------------------------|-----------------------------------------|
| `D:\my-data\pg`             | `/mnt/d/my-data/pg`                      |
| `E:/uploads`                | `/mnt/e/uploads`                         |
| `/Volumes/external/pg`      | `/Volumes/external/pg` (macOS native)    |
| `/var/lib/marry/pg`         | `/var/lib/marry/pg` (Linux native)       |
| `./.pgdata`                 | resolved relative to `deploy/docker/`   |

## What `up` actually does

1. Creates `deploy/docker/.env` from `.env.example` on first run.
2. Builds `deploy/docker/.env.resolved` with WSL path conversion applied.
3. Re-aggregates `marry-platform-persistence/src/main/resources/db/migration/V*.sql`
   into `db-init/00-flyway.sql` so Postgres can auto-apply it
   (`/docker-entrypoint-initdb.d/` runs every `*.sql` once on first start).
4. `docker compose --env-file .env.resolved up -d --build` — builds the
   backend and frontend images, starts `postgres`, `redis`, `backend`,
   `frontend` on the `marry-net` bridge.
5. Waits for the backend's `actuator/health` to return `UP`.

## Choosing a disk for PostgreSQL

Edit `deploy/docker/.env` and set an absolute path on whichever disk you
want. Pick the form that matches how you intend to run the stack:

```env
# WSL — Windows paths are auto-converted to /mnt/d/...
PG_DATA_PATH=D:\my-data\marry-pg
UPLOADS_PATH=D:\my-data\marry-uploads

# macOS / Linux native
PG_DATA_PATH=/Volumes/external/pg
UPLOADS_PATH=/Volumes/external/uploads
```

If you change `PG_DATA_PATH` after the first run you must **move** (not
copy) the existing data directory, otherwise Postgres will treat the new
path as a fresh cluster and re-run the init scripts. Flyway-style migrations
are idempotent so re-applying them is safe, but you will lose runtime data.

> Tip: keep these paths outside the repo so `git status` stays clean. The
> helper scripts already `mkdir -p` the target so the first start "just works".

## Tearing down

```bash
./down.sh                # stop containers, keep data
./down.sh --volumes      # also remove named Redis volume
./down.sh --images       # also remove local backend/frontend images
./down.sh --all          # volumes + images + orphans
```

PowerShell equivalents: `.\down.ps1 [-Volumes] [-Images] [-Project] [-Full]`.

## Reset to a clean state

```bash
./up.sh --reset          # asks for confirmation
./up.sh --reset --yes    # skip confirmation
```

`Reset` removes the resolved `PG_DATA_PATH` directory, the resolved
`UPLOADS_PATH` directory, and the `marry-redis-data` named volume before
bringing the stack back up. The next start will look like a fresh install:
schema + seeds applied from `db-init/00-flyway.sql`.

PowerShell equivalent: `.\up.ps1 -Reset [-Force]`.

## Useful commands

```bash
docker compose -f deploy/docker/docker-compose.yml --env-file deploy/docker/.env.resolved logs -f backend
docker compose -f deploy/docker/docker-compose.yml exec postgres psql -U marry -d marry_platform
docker compose -f deploy/docker/docker-compose.yml restart backend
```

## Notes

- The backend uses the root-level `Dockerfile`, which multi-stage-builds
  the Spring Boot fat jar with `mvn -Pprod package`.
- The frontend uses `marry-platform-frontend/Dockerfile` (Vue 3 + pnpm/nginx),
  with a Docker-specific Nginx config in `deploy/docker/nginx/` that points
  the upstream at the `backend` container hostname.
- `application.yml` ships with `flyway.enabled: false`; the schema is
  applied by Postgres itself via `db-init/00-flyway.sql`. The Spring Boot
  app just connects to an already-initialized database.
- Health checks (`pg_isready`, `redis-cli ping`, `actuator/health`) gate
  service startup so the backend only starts after Postgres + Redis are
  truly ready.
- Default ports: `5173` (frontend / Nginx, matching Vite) — change with
  `FRONTEND_PORT`. The backend is reachable only via Nginx at `/api/`;
  it is **not** published on the host by default.
- `.env.resolved` is generated and git-ignored by convention (it's added
  to `.gitignore` automatically; you can also add it manually).