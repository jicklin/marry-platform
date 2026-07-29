# marry-platform — Architecture & Module Guide

## Module dependency graph

```
                 ┌──────────────────────────────┐
                 │   marry-platform-admin       │   @SpringBootApplication
                 │   (REST controllers, app yml)│
                 └──────────┬───────────────────┘
                            │ depends on
       ┌────────────┬───────┼──────────┬──────────────┬──────────────┐
       ▼            ▼       ▼          ▼              ▼              ▼
  ┌────────┐  ┌────────┐ ┌────────┐ ┌──────┐    ┌─────────┐    ┌──────────┐
  │ system │  │  log   │ │ security│ │monitr│   │generator│    │  common  │
  └───┬────┘  └───┬────┘ └────┬───┘ └──────┘    └────┬────┘    └────┬─────┘
      │           │           │                       │             ▲
      ▼           ▼           ▼                       ▼             │
  ┌────────┐ ┌────────┐ ┌────────┐  ┌────────┐  ┌────────────┐    │
  │persist.│ │persist.│ │persist.│  │ (none) │  │ persistence│────┘
  └───┬────┘ └────────┘ └────────┘  └────────┘  └─────┬──────┘
      │                                               │
      ▼                                               ▼
  ┌────────┐                                       ┌────────┐
  │ domain │                                       │ domain │
  └────────┘                                       └────────┘

  api ──▶ common
```

## Permission flow

```
┌─────────┐  POST /auth/login            ┌──────────────┐
│  Login  │ ───────────────────────────▶ │ AuthController│
└─────────┘                              └──────┬────────┘
                                                  │ verify pwd (BCrypt)
                                                  │ load menus perms
                                                  ▼
                              issueAccessToken (uid, username, perms[])
                                                  │
       ┌──────────────────────────────────────────┘
       ▼
   JWT in Authorization header
       │
       ▼  subsequent requests
┌──────────────────────────────────────────┐
│ JwtAuthenticationFilter                  │ 1. parse token
│                                         │ 2. check blacklist (Redis)
│                                         │ 3. build SimpleGrantedAuthority from perms claim
│                                         │ 4. populate SecurityContext
└──────────────────────────────────────────┘
       │
       ▼
   @PreAuthorize("hasAuthority('system:user:add')")
       │ passes if perm claim contains that key
       ▼
   Controller method runs
       │
       ▼
   @Around("Log") aspect → sys_oper_log row (async)
```

## Frontend permission UI flow

```
App boots
   │
   ▼
router.beforeEach (guard.ts)
   │ check userStore.token
   │ if missing → /login
   │ if first visit → fetchInfo() + permissionStore.generateRoutes()
   │                    │
   │                    ├─ GET /api/system/menu/routers
   │                    ├─ build Vue Router records dynamically
   │                    └─ router.addRoute()
   ▼
Layout renders sidebar from permissionStore.sidebarRoutes
   │ each button uses v-auth="'system:user:add'" directive
   │ directive reads userStore.perms and removes element if absent
```

## Database tables (18)

| Group | Tables |
|---|---|
| RBAC core | sys_user, sys_role, sys_menu, sys_dept |
| RBAC joins | sys_user_role, sys_role_menu, sys_role_dept |
| Logs | sys_oper_log, sys_login_log |
| Config | sys_notice, sys_dict_type, sys_dict_data, sys_config |
| Optional | sys_file, sys_job, sys_job_log |
| Code gen | gen_table, gen_table_column |

## Environment variables

| Var | Default | Description |
|---|---|---|
| `JWT_SECRET` | `replace-me-...` | HMAC secret (≥32 chars) |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `marry_platform` | Database name |
| `DB_USER` | `marry` | Database user |
| `DB_PASSWORD` | `marry123` | Database password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | (empty) | Redis password |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active profile |

## Useful commands

```bash
# Build everything
mvn -DskipTests clean package

# Run only backend (with -am to build deps)
mvn -pl marry-platform-admin -am spring-boot:run

# Hot reload of frontend
cd marry-platform-frontend && pnpm dev

# Smoke test the backend
./scripts/smoke.sh http://localhost:8080/api

# View Flyway migrations
psql -d marry_platform -c "SELECT version, description, success FROM flyway_schema_history;"
```