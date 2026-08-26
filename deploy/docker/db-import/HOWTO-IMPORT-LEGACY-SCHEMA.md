# 如何从老库导出整个 schema + 数据

## 第一步：导出 schema（DDL only）

```bash
# PostgreSQL 老库
pg_dump -h <old-host> -p 5432 -U <user> -d <old-db> \
  --schema-only --no-owner --no-acl \
  > /tmp/legacy-schema.sql
```

## 第二步：导出数据（INSERT 形式）

```bash
pg_dump -h <old-host> -p 5432 -U <user> -d <old-db> \
  --data-only --inserts --disable-triggers --no-owner \
  > /tmp/legacy-data.sql
```

`--inserts` 让 pg_dump 用显式 `INSERT INTO t (col1, col2) VALUES (...)` 格式，
而不是 COPY，这是关键 — 它让字段顺序**显式**写在 INSERT 里，方便我们做映射。

## 第三步：分析 schema 差异

```bash
# 看老库有哪些表（用来对照新库的 21 张表）
grep -E "^CREATE TABLE" /tmp/legacy-schema.sql

# 看每张表的字段
grep -A 30 "CREATE TABLE public.sys_user" /tmp/legacy-schema.sql
```

## 第四步：填映射表

打开 `/tmp/legacy-schema.sql`，把每张老表的字段**列出来**，跟我们新库的字段**对照**，写出映射。

比如老库 `sys_user`：
```sql
CREATE TABLE public.sys_user (
    id           BIGSERIAL PRIMARY KEY,
    user_name    VARCHAR(64) NOT NULL,
    pass_hash    VARCHAR(100) NOT NULL,
    nick         VARCHAR(64),
    ...
);
```

新库 `sys_user`：
```sql
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nick_name VARCHAR(64),
    ...
);
```

字段映射：
```
user_name  -> username
pass_hash  -> password    ⚠️ 验证 BCrypt 格式
nick       -> nick_name
```

## 第五步：转换数据 INSERT

打开 `/tmp/legacy-data.sql`，对每行 INSERT：
1. **重排字段名**（按新库顺序）
2. **删掉新库里没有的字段**
3. **加 `ON CONFLICT (id) DO NOTHING`**
4. **如果有字段类型不兼容，做转换**（如 INT -> BIGINT，CHAR(1) -> SMALLINT）

自动化转换（如果你熟悉 sed/awk）：
```bash
# 把老字段 user_name 换成新字段 username（仅在 INSERT 字段列表里）
sed -i 's/user_name/username/g' /tmp/legacy-data.sql
sed -i 's/pass_hash/password/g' /tmp/legacy-data.sql
```

但 sed 不能保证只在 INSERT 里替换 — 真正可靠的做法是**手工审一遍**。

## 第六步：贴进 10-import-data.sql

把转换好的 INSERT 复制到 `deploy/docker/db-init/10-import-data.sql` 的
对应 `=== EDIT ME ===` 段。

注意 FK 顺序！10-import-data.sql 已经按顺序排好：
1. sys_dept
2. sys_role
3. sys_menu
4. sys_user
5. sys_user_role
6. sys_role_menu
7. sys_dict_type
8. sys_dict_data

如果老库有**新库里没有的表**（比如你自己加的业务模块表），
直接追加到 10-import-data.sql 末尾：
```sql
-- ============================================================
-- 9. Custom business tables (only present in legacy DB)
-- ============================================================
CREATE TABLE IF NOT EXISTS my_custom_module (
    id BIGSERIAL PRIMARY KEY,
    ...
);

INSERT INTO my_custom_module (...) VALUES (...)
ON CONFLICT (id) DO NOTHING;
```

## ⚠️ 三个高风险陷阱

### 1. 密码哈希格式

老库的 `pass_hash` 必须是 BCrypt (`$2a$10$...` 或 `$2b$...`)，
Spring Security 才能验证。如果老库用：
- **MD5** — 不能用，要重新生成 BCrypt
- **明文** — 不能用
- **自定义算法** — 不能用

判断方法：打开 `/tmp/legacy-data.sql`，grep 几条密码：
```bash
grep "INSERT INTO.*sys_user" /tmp/legacy-data.sql | head -5
```

如果格式不是 `$2[ayb]$\d{2}$`，就别导入密码列，让用户用默认密码登录后改。

### 2. 主键 ID 冲突

`00-flyway.sql` 已经用了 id=1 (admin), id=2 (demo) 等等。
如果老库的 `sys_user` 里也有 id=1 的人，那个 INSERT 会被 `ON CONFLICT DO NOTHING`
跳过，**老用户丢失**。

两种处理：
- **重映射 ID**：`id = id + 1000`（在 INSERT 里做算术）
- **导入后 UPDATE**：先插入，再 `UPDATE sys_user SET id = id + 1000 WHERE id BETWEEN 1 AND 100`

### 3. del_flag 默认值

新库 `del_flag` 默认 `0`（未删除）。
老库如果默认是 `1`，导入后会全部"已删除"，用户登录不上。

## 一键转换脚本（如果你嫌手工烦）

我可以写一个 Python 或 PowerShell 脚本，读取你的 `/tmp/legacy-schema.sql` +
`/tmp/legacy-data.sql`，自动生成转换后的 SQL，输出到
`deploy/docker/db-init/10-import-data.sql`。

但**前提是你给我一份完整的字段映射表**（老字段 -> 新字段）。

---

## 下一步

要让我自动转换，请提供：
1. 老库的 `pg_dump --schema-only` 输出（或部分）
2. 一两张表的 `pg_dump --data-only` INSERT 前几行
3. 你期望的字段映射（如果跟我的默认猜测不一致）

或者你自己跑一遍上面六个步骤，把转换好的 SQL 贴进 10-import-data.sql。