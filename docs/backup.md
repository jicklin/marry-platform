# 数据备份：PostgreSQL + 上传文件 + InfiniCloud WebDAV

项目的业务数据由两部分组成：

1. PostgreSQL 数据库，包括 `sys_file` 中的文件元数据；
2. `MARRY_FILE_PATH` 下的图片、视频和附件原文件。

两者必须一起备份。Redis 当前用于 Token、在线状态等临时数据，通常不需要恢复。

## 方案

使用 `pg_dump + restic + rclone`：

- `pg_dump` 创建事务一致的 PostgreSQL 备份；
- `restic` 在本地分块、压缩/去重并加密；
- `rclone` 将加密后的 restic 仓库写入 InfiniCloud WebDAV；
- WebDAV 端不会看到原始文件名和文件内容；
- 图片和视频按内容增量上传，未修改的大文件不会在每次备份时重新上传。

> restic 仓库密码无法找回。密码文件之外还应离线保存一份，例如密码管理器和纸质应急副本。

## 1. 安装工具

macOS：

```bash
brew install postgresql@16 restic rclone
```

Linux 使用系统包管理器安装 `postgresql-client`、`restic` 和 `rclone`。

## 2. 配置 InfiniCloud WebDAV

运行：

```bash
rclone config
```

创建名为 `infinicloud` 的 remote：

- Storage 选择 `webdav`；
- URL 填写 InfiniCloud 账户页面提供的 WebDAV 地址，不要猜测地址；
- Vendor 选择 `other`；
- User 和 Password 填写 InfiniCloud 的 WebDAV 凭据。

验证连接：

```bash
rclone lsd infinicloud:
```

rclone 配置中保存的是 WebDAV 登录凭据，应将配置文件权限设为 `600`。也可以在 `rclone config` 中为整个配置设置密码；用于无人值守定时任务时，还需要安全提供 `RCLONE_CONFIG_PASS`。

## 3. 创建本地秘密文件

```bash
mkdir -p "$HOME/.config/marry-platform"
cp scripts/backup.env.example "$HOME/.config/marry-platform/backup.env"
chmod 600 "$HOME/.config/marry-platform/backup.env"
```

编辑 `backup.env`，特别确认：

- `PGUSER`；
- `PGPASSFILE`；
- `MARRY_FILE_PATH`；
- `RESTIC_PASSWORD_FILE`；
- `RESTIC_REPOSITORY`。

创建 PostgreSQL 密码文件，格式是 `host:port:database:user:password`：

```bash
printf '%s\n' 'localhost:5432:marry_platform:root:替换为数据库密码' \
  > "$HOME/.config/marry-platform/pgpass"
chmod 600 "$HOME/.config/marry-platform/pgpass"
```

生成并保存 restic 仓库密码：

```bash
openssl rand -base64 48 > "$HOME/.config/marry-platform/restic-password"
chmod 600 "$HOME/.config/marry-platform/restic-password"
```

密码中不要包含换行以外的额外内容，不要把上述秘密文件放进 Git 仓库。

## 4. 初始化加密仓库

只执行一次：

```bash
set -a
source "$HOME/.config/marry-platform/backup.env"
set +a
restic init
```

初始化后建议立即运行一次完整备份：

```bash
./scripts/backup-webdav.sh all
```

脚本支持：

```bash
./scripts/backup-webdav.sh db          # 只备份数据库
./scripts/backup-webdav.sh files       # 只备份图片、视频和附件
./scripts/backup-webdav.sh all         # 数据库和文件一起备份，推荐
./scripts/backup-webdav.sh all --prune # 备份后执行保留策略并清理旧分块
```

数据库 dump 只在本地临时存在，上传成功或失败后都会清理。脚本使用目录锁避免两个备份任务并发运行。

## 5. 一致性与执行频率

`pg_dump` 自身具有数据库事务一致性，但数据库中的文件记录和文件目录无法跨存储介质形成原子快照。要求严格一致时：

1. 暂停后端应用或至少暂停上传、删除操作；
2. 执行 `./scripts/backup-webdav.sh all`；
3. 再启动应用。

建议：

- 每天执行一次 `all`；
- 数据变化频繁时，每 6 小时执行一次 `db`；
- 每周执行一次 `all --prune`；
- 每月执行一次 `restic check` 和恢复演练；
- 首次大量视频上传可能耗时很长，后续通常只上传新增或变更的数据块。

不要在每次备份后运行 `--prune`，WebDAV 上清理大量分块较慢，也会增加网络请求。

## 6. 查看与校验

先加载配置：

```bash
set -a
source "$HOME/.config/marry-platform/backup.env"
set +a
```

```bash
restic snapshots --tag marry-platform
restic check
```

深度读取校验会下载数据，耗时和流量较大，可按月运行：

```bash
restic check --read-data-subset=5%
```

## 7. 恢复演练

恢复到临时目录，不要直接覆盖生产数据：

```bash
mkdir -p "$HOME/restore-test"
restic restore latest --tag mode-all --target "$HOME/restore-test"
```

restic 会在目标目录内保留原绝对路径层级。找到恢复出的数据库文件：

```bash
find "$HOME/restore-test" -name 'marry_platform.dump'
```

恢复到测试数据库：

```bash
createdb -h localhost -p 5432 -U root marry_platform_restore
pg_restore \
  -h localhost \
  -p 5432 \
  -U root \
  -d marry_platform_restore \
  --no-owner \
  /恢复目录中的/marry_platform.dump
```

文件验证无误后，再将恢复出的 `MARRY_FILE_PATH` 内容同步回正式目录。正式恢复前应停止应用，并保留当前数据库和文件目录作为回退副本。

## 8. 定时任务

cron 示例（每天 03:15 完整备份）：

```cron
15 3 * * * /项目绝对路径/scripts/backup-webdav.sh all >> /用户目录/marry-backup.log 2>&1
```

每周日 04:15 清理旧快照：

```cron
15 4 * * 0 /项目绝对路径/scripts/backup-webdav.sh all --prune >> /用户目录/marry-backup.log 2>&1
```

cron 环境的 `PATH` 很精简。如果找不到 `pg_dump`、`restic` 或 `rclone`，应在 cron 中设置包含 Homebrew/系统工具目录的 `PATH`。macOS 更推荐使用 `launchd`，并确保进程具有读取 `MARRY_FILE_PATH` 的权限。
