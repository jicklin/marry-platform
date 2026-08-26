# ============================================================
# Multi-stage Dockerfile for marry-platform-frontend (Vue 3 SPA)
#
# Build from the repo root:
#   docker build -t marry-platform-frontend -f marry-platform-frontend/Dockerfile .
# ============================================================

# ---- build stage ----
FROM node:20-alpine AS build
WORKDIR /src

# Install pnpm (lockfile-driven). Falls back to npm if pnpm-lock.yaml is absent.
# Pin pnpm to 10.x — the last major that supports Node 20.
# pnpm 11.x requires Node >= 22.13 (uses node:sqlite builtin).
RUN corepack enable && corepack prepare pnpm@10.14.0 --activate

COPY marry-platform-frontend/package.json marry-platform-frontend/pnpm-lock.yaml* /src/
RUN if [ -f pnpm-lock.yaml ]; then pnpm install --frozen-lockfile; else npm install; fi

COPY marry-platform-frontend/ /src/
RUN if [ -f pnpm-lock.yaml ]; then pnpm build; else npm run build; fi

# ---- run stage ----
FROM nginx:1.27-alpine
RUN rm -f /etc/nginx/conf.d/default.conf
# Use the Docker-aware upstream (backend:8080) instead of the host-local 127.0.0.1.
COPY deploy/docker/nginx/marry-platform.conf /etc/nginx/conf.d/marry-platform.conf
COPY --from=build /src/dist /var/www/marry-platform/dist

EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost/ >/dev/null 2>&1 || exit 1
CMD ["nginx", "-g", "daemon off;"]