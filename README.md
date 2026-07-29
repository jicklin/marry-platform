# 🍵 marry-platform

A production-grade, multi-module Spring Boot 3.3 admin platform with **Vue 3 + Naive UI** frontend, PostgreSQL persistence, MyBatis-Plus ORM, Spring Security + JWT (stateless) authentication, full RBAC with **button-level permissions**, and operation/login logs with statistics dashboard.

## ✨ Features

- **Multi-module Maven** (10 modules): clean separation of concerns along DDD layers
- **Spring Boot 3.3 + Java 17** + **Spring Security 6** + **JWT** (dual-token: access / refresh, Redis blacklist)
- **RBAC** with **button-level permissions** (enforced server-side via `@PreAuthorize`, client-side via `v-auth` directive)
- **Operation logs** (AOP captured) + **login logs** + **online users** (Redis-backed)
- **Statistics dashboard** with ECharts (visit trend, operation type, login status)
- **Knife4j 4.5** OpenAPI 3 documentation at `/api/doc.html`
- **Druid** connection pool with monitoring UI at `/api/druid`
- **Actuator** + **Prometheus** metrics
- **Flyway** migrations (PostgreSQL dialect)
- **Vue 3.5 + Vite 6 + Naive UI 2.41** frontend, Pinia stores, persistent auth, dynamic routing
- **Code generation** scaffolding (MyBatis-Plus Generator)
- Custom Naive UI theme overrides with light/dark modes

## 📁 Module Structure

```
marry-platform/                          # Root (pom packaging)
├── marry-platform-common/               # R<T>, BizCode, BaseEntity, utils
├── marry-platform-api/                  # DTOs, VOs, PageQuery
├── marry-platform-domain/               # Pure POJO entities
├── marry-platform-persistence/          # MyBatis-Plus mappers + Flyway
├── marry-platform-security/             # Spring Security + JWT + LoginUser
├── marry-platform-system/               # User/Role/Menu/Dept/Dict/Config services
├── marry-platform-log/                  # @Log AOP + LoginLogAspect
├── marry-platform-monitor/              # Actuator + custom health indicators
├── marry-platform-generator/            # Code generation
├── marry-platform-admin/                # Spring Boot main app (executable jar)
└── marry-platform-frontend/             # Vue 3 + Vite SPA (separate artifact)
```

## 🚀 Quick Start

### Prerequisites

- **Java 17** (LTS)
- **Maven 3.9+**
- **PostgreSQL 14+** (Postgres 16 recommended)
- **Redis 7+** (optional but recommended for token blacklist + online users)
- **Node.js 20+** + pnpm/npm

### 1. Backend setup

```bash
# Start PostgreSQL and Redis (install via brew if not present)
brew services start postgresql@16
brew services start redis

# Create database
createdb marry_platform
createuser marry --pwprompt    # set password to marry123

# Run backend (dev profile)
cd /Users/yoyo/MyProjects/marry-platform
mvn -pl marry-platform-admin -am spring-boot:run

# OR build a runnable jar
mvn -DskipTests -Pprod package
java -jar marry-platform-admin/target/marry-platform-admin.jar
```

The backend will start on `http://localhost:8080/api`.

On first run, **Flyway** automatically creates all 18 tables and seeds:
- `admin` / `admin123` (BCrypt)
- `demo` / `admin123`
- Full menu tree with button-level permissions

### 2. Frontend setup

```bash
cd marry-platform-frontend
npm install -g pnpm           # one-time
pnpm install

# Dev server (proxies /api → localhost:8080)
pnpm dev

# Production build
pnpm build
# → outputs to dist/
```

Open `http://localhost:5173`. Login with `admin / admin123`.

### 3. Useful URLs

| URL | Description |
|---|---|
| `http://localhost:8080/api/auth/login` | Login endpoint (POST) |
| `http://localhost:8080/api/actuator/health` | Health check |
| `http://localhost:8080/api/doc.html` | Knife4j / Swagger UI |
| `http://localhost:8080/api/druid` | Druid monitor UI (admin/admin) |
| `http://localhost:5173` | Frontend dev server |

## 🎨 UI Tour

The frontend uses **Naive UI** with a custom theme:

- **Login page**: gradient background, glassy card
- **Layout**: collapsible sidebar + navbar (theme toggle + user dropdown) + breadcrumbs
- **Dashboard**: 4 stat tiles + 2 charts (line/bar) + 1 pie chart
- **System** pages: user/role/menu/dept/dict/config CRUD with search forms, data tables, modals
- **Monitor** pages: operation logs, login logs, online users

Button-level permissions are enforced via the `v-auth` directive:

```vue
<n-button v-auth="'system:user:add'">新增用户</n-button>
```

## 🔐 Default Accounts

| Username | Password | Roles |
|---|---|---|
| `admin` | `admin123` | super admin (all permissions) |
| `demo` | `admin123` | common user (read-only on system) |

> Override by setting `JWT_SECRET` env var and editing the seed migration.

## 📦 Build Artifacts

Per your preference, the project builds **two separate artifacts**:

1. **Backend jar**: `marry-platform-admin/target/marry-platform-admin.jar`
2. **Frontend static**: `marry-platform-frontend/dist/` — deploy behind Nginx/Apache pointing to `/api` proxy.

## 🐳 Docker (optional)

```bash
# Use existing PostgreSQL/Redis installations, or run in containers
docker run -d --name marry-pg -e POSTGRES_USER=marry -e POSTGRES_PASSWORD=marry123 -e POSTGRES_DB=marry_platform -p 5432:5432 postgres:16-alpine
docker run -d --name marry-redis -p 6379:6379 redis:7-alpine
```

A multi-stage `Dockerfile` lives in `marry-platform-admin/Dockerfile` for the backend.

## 📊 Permission Keys Reference

Button permissions follow the pattern `<module>:<resource>:<action>`:

- `system:user:add` / `system:user:edit` / `system:user:remove` / `system:user:resetPwd` / `system:user:list` / `system:user:query`
- `system:role:add` / `system:role:edit` / `system:role:remove` / `system:role:list` / `system:role:query`
- `system:menu:add` / `system:menu:edit` / `system:menu:remove` / `system:menu:list` / `system:menu:query`
- `system:dept:*` / `system:dict:*` / `system:config:*`
- `monitor:operlog:list` / `monitor:operlog:remove`
- `monitor:loginlog:list` / `monitor:loginlog:remove`
- `monitor:online:list` / `monitor:online:forceLogout`

## 🛠 Tech Stack

| Layer | Library | Version |
|---|---|---|
| Framework | Spring Boot | 3.3.5 |
| Java | JDK | 17 |
| ORM | MyBatis-Plus | 3.5.9 |
| DB | PostgreSQL | 14+ |
| Pool | Alibaba Druid | 1.2.24 |
| Cache | Redis (Lettuce) + Redisson | 7 / 3.36 |
| Security | Spring Security | 6.x |
| JWT | jjwt | 0.12.6 |
| API docs | Knife4j (OpenAPI 3) | 4.5.0 |
| Migration | Flyway | 10.17 |
| Frontend | Vue 3.5 + Vite 6 + Naive UI 2.41 + TS 5.7 |
| State | Pinia | 2.3 |
| Charts | ECharts + vue-echarts | 5.5 / 7 |
| Persistence | pinia-plugin-persistedstate | 4.2 |

## 📜 License

MIT