# Importing legacy data into the marry-platform stack

`deploy/docker/db-init/00-flyway.sql` already creates the schema and
seeds the **system** data: `admin / admin123`, `demo / admin123`, the
default role / menu tree, and a couple of starter dictionary entries.

If you have an existing database (older marry-platform install, or a
totally different admin platform) and you want to bring your data over,
this is how.

## How init scripts run

Postgres' official image executes **every** `*.sql` and `*.sh` file
inside `/docker-entrypoint-initdb.d/` in **alphabetical order**, once,
on the very first start (when the data directory is empty). The
`docker-compose.yml` mounts the whole `db-init/` directory, so:

| File                  | Source                              | Auto-rebuilt? |
|-----------------------|-------------------------------------|---------------|
| `00-flyway.sql`       | generated from `marry-platform-persistence/src/main/resources/db/migration/V*.sql` | yes (`build-db-init.ps1`) |
| `10-import-data.sql`  | **hand-written by you**             | no — git-tracked |

`10-import-data.sql` runs *after* `00-flyway.sql`, so the schema and
system seed already exist when your data is inserted.

## What's in `10-import-data.sql`

A fill-in-the-blanks template, organized in **FK order** so it just
works:

1. `sys_dept` — departments
2. `sys_role` — roles
3. `sys_menu` — menu entries
4. `sys_user` — user accounts
5. `sys_user_role` — user-role links
6. `sys_role_menu` — role-menu links
7. `sys_dict_type` — dictionary types
8. `sys_dict_data` — dictionary values

Each section is currently a comment block. Replace the placeholder
`INSERT` with real rows from your source database.

## Three ways to fill it

### A. PostgreSQL → PostgreSQL (most common)

In your **source** database, dump only the rows you need:

```bash
pg_dump -h <src-host> -U <user> -d <src-db> \
  --data-only --inserts --disable-triggers \
  -t public.sys_dept \
  -t public.sys_role \
  -t public.sys_user \
  -t public.sys_user_role \
  -t public.sys_role_menu \
  -t public.sys_dict_type \
  -t public.sys_dict_data \
  > /tmp/legacy.sql
```

Then:

1. Open `/tmp/legacy.sql` and **strip any leading `TRUNCATE` /
   `DELETE` statements** — you do NOT want to wipe the system seed.
2. **Strip `SET` / `SELECT pg_catalog.set_config(...)` lines** — they
   come from pg_dump and aren't needed for our purposes.
3. **Strip `\restrict` / `\unrestrict`** if present (newer pg_dump
   versions add these around the whole file).
4. For every `INSERT INTO` line, append `ON CONFLICT (id) DO NOTHING`
   (or `ON CONFLICT DO NOTHING` for tables without a single-column PK).
6. Paste the cleaned-up content into the matching `=== EDIT ME ===`
   block in `10-import-data.sql`.

### B. MySQL / SQL Server / Oracle → PostgreSQL

You cannot use the source's native dump format — dialects differ.
Use one of these strategies:

- **`mysqldump --compatible=postgresql`** and then patch field types
  (`INT` → `INTEGER`, `TINYINT(1)` → `SMALLINT`, `DATETIME` →
  `TIMESTAMP`, backticks → double quotes).
- **Export to CSV** in your source tool, then in `10-import-data.sql`
  use Postgres `COPY`:
  ```sql
  COPY sys_dept (id, parent_id, name, code, order_num, status, del_flag)
  FROM '/docker-entrypoint-initdb.d/legacy/sys_dept.csv'
  WITH (FORMAT csv, HEADER true);
  ```
  Mount the legacy CSV directory into the postgres container alongside
  `db-init/` to make the path work.

### C. CSV / Excel (small one-offs)

Open the CSV, write out `INSERT` statements directly. Keep the
`ON CONFLICT (id) DO NOTHING` suffix on every row.

## Critical gotcha: password hashes

Spring Security uses **BCrypt** ($2a$10$…). If your source database
contains plain-text or MD5 passwords, those users will not be able to
log in. Two options:

- **Re-hash during migration.** In a one-off Java snippet, run each
  plain-text password through `BCryptPasswordEncoder` and substitute
  the resulting hash into the `INSERT`.
- **Force-reset on first login.** Add a SQL flag column to mark
  imported users, and have a startup hook call Spring Security's
  `UserDetailsService` to set a temporary password / require change.

The template's `sys_user` section documents both approaches.

## Editing `10-import-data.sql`

```powershell
# Open it in your editor
code deploy/docker/db-init/10-import-data.sql

# Or, if you have pg_dump output ready:
code deploy/docker/db-init/10-import-data.sql
# paste your rows into the matching "=== EDIT ME ===" blocks
```

The file is committed to git. The `up.sh` / `up.ps1` helpers **never**
overwrite it — only `00-flyway.sql` is regenerated.

## Try it out

1. Drop your legacy data into the template (keep the FK order).
2. `cd deploy/docker && ./up.sh` (or `.\up.ps1` on Windows).
3. Watch the postgres container log:
   ```bash
   docker compose -f deploy/docker/docker-compose.yml logs postgres
   ```
   You should see both `00-flyway.sql` and `10-import-data.sql`
   execute, then the backend start.
4. Log in with one of your imported accounts.

If you need to re-run with a clean slate:

```bash
./up.sh --reset     # Wipes PG_DATA_PATH, UPLOADS_PATH, and the Redis
                    # volume; postgres re-runs every init script.
```

## When NOT to use this file

- **Big data** (millions of rows): `initdb` is one-shot and synchronous;
  the container won't start until *all* init scripts finish. For bulk
  imports, do them *after* the stack is up via `psql` or `COPY`.
- **Production data**: don't check raw user passwords or PII into a
  public repo. Either redact / hash before committing, or load the
  script from a private location (e.g. a docker secret) and have it
  populate `10-import-data.sql` at deploy time.