# Importing legacy `pg_dump` output into the marry-platform stack

This directory contains the import tooling for bringing data from an
older marry-platform installation (or any PostgreSQL-based admin tool)
into your new Docker stack.

## Quick start

```bash
# 1. Bring the stack up (data dir is fresh, schema + seed are applied)
cd deploy/docker
./up.sh

# 2. Drop your dump file somewhere docker can reach it, then:
./db-import/import-dump.sh /Users/yoyo/Downloads/20260825.dump
```

Or from Windows PowerShell:

```powershell
cd D:\myprojects\marry-platform\deploy\docker
.\up.ps1
.\db-import\import-dump.ps1 C:\Users\me\Downloads\20260825.dump
```

Or, if the dump lives inside WSL:

```powershell
.\db-import\import-dump.ps1 '/mnt/c/Users/me/Downloads/20260825.dump'
```

## Supported formats

`import-dump.sh` / `import-dump.ps1` auto-detect the dump format:

| `pg_dump` flag                 | File looks like              | Script uses    |
|--------------------------------|------------------------------|----------------|
| (default) or `-Fp`             | plain SQL text               | `psql`         |
| `-Fc` or `--format=custom`     | binary `.dump` (compressed)  | `pg_restore`   |
| `-Ft` or `--format=tar`        | directory or `.tar`          | `pg_restore`   |

Your command:

```bash
pg_dump --format=custom --compress=6 --no-owner \
  --file="/Users/yoyo/Downloads/20260825.dump" -h localhost
```

produces a **custom-format binary dump** (`-Fc`). The script will pick
`pg_restore` automatically.

## Safety flags

By default the script is **non-destructive** — it adds rows, never
deletes existing ones. To overwrite the admin/demo seed or anything
else, opt in explicitly:

| Flag (bash / PowerShell) | What it does                              |
|--------------------------|-------------------------------------------|
| `--clean` / `-Clean`     | Adds `--clean --if-exists` to pg_restore (drops existing objects) |
| `--drop-schema` / `-DropSchema` | `DROP SCHEMA public CASCADE` before restore |
| `--no-public` / `-NoPublic` | Skip the `public` schema entirely    |

Both `--clean` and `--drop-schema` print a warning and require you to
type `yes` to confirm. Use them only when you really want a clean slate.

## Why no `--clean` by default

The fresh stack ships with:

- 21 tables created by `db-init/00-flyway.sql`
- An `admin / admin123` super-admin (BCrypt-hashed)
- A `demo / demo` read-only user
- ~50 menu entries
- A handful of dictionary entries

`--clean` would wipe all of those before importing. If you want to keep
the admin/demo login working after import, **do not** pass `--clean`.

## Common pitfalls

### 1. `id` collisions

The seed inserts `sys_user.id = 1` (admin) and `sys_user.id = 2` (demo).
If your legacy dump also has `sys_user.id = 1` (someone), `pg_restore`
will refuse the duplicate key (because the seed inserted first) — or,
worse, silently overwrite the seed row depending on restore order.

**Fixes:**

- Don't import `sys_user` rows with `id IN (1, 2)`.
- Or use `--drop-schema` and re-run init (loses the fresh schema).
- Or do a one-shot `UPDATE sys_user SET id = id + 1000` after the
  import to shift your legacy IDs out of the seed range.

### 2. Password hash format

marry-platform uses **BCrypt** (`$2a$10$...`). If your legacy dump has
plain-text or MD5 passwords, those users can't log in. Two options:

- Re-hash passwords before import.
- Force a password reset on first login (out of scope here).

### 3. `del_flag` defaults

The new schema defaults `del_flag` to `0` (active). If your legacy DB
defaults to `1` (deleted), every imported user is "deleted" until you
fix the flag.

### 4. Encoding

The container uses PostgreSQL's default `UTF-8` locale. If your dump
was made with a different client encoding (e.g. `WIN1252`), pg_restore
may fail or produce garbled text. Re-dump with `pg_dump --encoding=UTF-8`
if you see encoding errors.

### 5. Permission errors

By default the script passes `--no-owner --no-privileges` to
`pg_restore`, so the imported objects become owned by the container's
`postgres` superuser. This is correct for a single-tenant docker stack
but you can opt out with `--owner`.

## Verifying the import

```bash
docker exec -it marry-postgres psql -U marry -d marry_platform
```

Inside psql:

```sql
\dt                    -- list tables
SELECT COUNT(*) FROM sys_user;       -- should match your dump
SELECT id, username, status FROM sys_user WHERE del_flag = 0 LIMIT 10;
\q
```

## Resetting everything and starting over

If you want to wipe the imported data and start fresh:

```bash
# Stops the stack, removes the PG data dir, re-creates init scripts,
# and brings everything back up.
./up.sh --reset
```

This re-runs `db-init/00-flyway.sql` so the seed is back. Then re-run
`import-dump.sh` if you want a clean re-import.

## Logging

The script doesn't keep a log by default. To capture the restore output:

```bash
./db-import/import-dump.sh /path/to/legacy.dump 2>&1 | tee import.log
```

For very large dumps (millions of rows), set PostgreSQL's statement
timeout high enough via `docker exec -e PGSTATEMENT_TIMEOUT=0` or by
relaxing it in the backend container if needed.