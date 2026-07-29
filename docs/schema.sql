-- ============================================================
--  marry-platform  -- 完整 PostgreSQL 建表脚本 (DDL)
--
--  本文件由两段 Flyway 迁移整理合并而成:
--    V2026.07.29.0900__init_core_tables.sql  -- 建表
--    V2026.07.29.0910__seed_admin.sql        -- 初始数据
--
--  使用方法:
--    psql -U postgres -d marry_platform -f schema.sql
--
--  表清单 (18 张):
--    RBAC 核心:   sys_user, sys_role, sys_menu, sys_dept
--    RBAC 关联:   sys_user_role, sys_role_menu, sys_role_dept
--    日志:        sys_oper_log, sys_login_log
--    业务配置:    sys_notice, sys_dict_type, sys_dict_data, sys_config
--    扩展:        sys_file, sys_job, sys_job_log
--    代码生成:    gen_table, gen_table_column
--
--  公共约定:
--    - 主键统一使用 BIGSERIAL (自增 64 位)
--    - 业务表使用 del_flag SMALLINT DEFAULT 0 做软删除 (1=已删)
--    - 所有业务表均含 create_by / create_time / update_by / update_time
--    - sys_menu.menu_type: 'M' 目录 / 'C' 菜单 / 'F' 按钮
--    - sys_role.data_scope: 1 全部 / 2 本部门 / 3 本部门及子 / 4 仅本人 / 5 自定义
--    - sys_*.status: 0 禁用 / 1 启用
-- ============================================================


-- ============================================================
-- 一、 RBAC 核心表
-- ============================================================

-- 1. sys_user  -- 用户
CREATE TABLE IF NOT EXISTS sys_user (
    id           BIGSERIAL    PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,                 -- BCrypt hash
    nick_name    VARCHAR(64),
    email        VARCHAR(128),
    phone        VARCHAR(32),
    avatar       VARCHAR(255),
    sex          SMALLINT     DEFAULT 0,                 -- 0 未知 / 1 男 / 2 女
    dept_id      BIGINT,
    status       SMALLINT     DEFAULT 1,                 -- 0 禁用 / 1 启用
    login_ip     VARCHAR(64),
    login_date   TIMESTAMP,
    remark       VARCHAR(500),
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_dept ON sys_user(dept_id);
COMMENT ON TABLE  sys_user               IS '用户账号';
COMMENT ON COLUMN sys_user.password      IS 'BCrypt 加密后的密码';


-- 2. sys_role  -- 角色
CREATE TABLE IF NOT EXISTS sys_role (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,                 -- 角色名
    code         VARCHAR(64)  NOT NULL UNIQUE,           -- 角色编码, 用于代码引用
    data_scope   SMALLINT     DEFAULT 1,                 -- 1 全部 / 2 本部门 / 3 本部门及子 / 4 仅本人 / 5 自定义
    status       SMALLINT     DEFAULT 1,
    remark       VARCHAR(500),
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE  sys_role               IS '角色';
COMMENT ON COLUMN sys_role.code          IS '角色编码 (如 admin / common)';


-- 3. sys_menu  -- 菜单 (含按钮级别权限)
CREATE TABLE IF NOT EXISTS sys_menu (
    id           BIGSERIAL    PRIMARY KEY,
    parent_id    BIGINT       DEFAULT 0,
    name         VARCHAR(64)  NOT NULL,
    menu_type    VARCHAR(8)   NOT NULL,                  -- 'M' 目录 / 'C' 菜单 / 'F' 按钮
    path         VARCHAR(255),                           -- 前端路由 path
    component    VARCHAR(255),                           -- 前端组件路径 (相对 views/)
    perm         VARCHAR(128),                           -- 按钮权限标识, 如 system:user:add
    icon         VARCHAR(64),
    order_num    INT          DEFAULT 0,
    visible      SMALLINT     DEFAULT 1,                 -- 0 隐藏 / 1 显示
    status       SMALLINT     DEFAULT 1,
    is_cache     SMALLINT     DEFAULT 0,                 -- 是否 keep-alive
    is_frame     SMALLINT     DEFAULT 1,                 -- 是否外链 (此处预留)
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_menu_parent ON sys_menu(parent_id);
COMMENT ON TABLE  sys_menu              IS '菜单(含按钮)';
COMMENT ON COLUMN sys_menu.menu_type    IS 'M 目录 / C 菜单 / F 按钮';
COMMENT ON COLUMN sys_menu.perm         IS '按钮权限 key (与 @PreAuthorize / v-auth 一一对应)';


-- 4. sys_dept  -- 部门
CREATE TABLE IF NOT EXISTS sys_dept (
    id           BIGSERIAL    PRIMARY KEY,
    parent_id    BIGINT       DEFAULT 0,
    name         VARCHAR(64)  NOT NULL,
    code         VARCHAR(64),
    leader       VARCHAR(64),
    phone        VARCHAR(32),
    email        VARCHAR(128),
    order_num    INT          DEFAULT 0,
    status       SMALLINT     DEFAULT 1,
    ancestors    VARCHAR(500),                          -- '/0/1/3/' 形式的父级路径, 用于子部门快速查询
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE  sys_dept               IS '部门组织';
COMMENT ON COLUMN sys_dept.ancestors     IS '父级路径 (例如 /0/1/3/)';


-- ============================================================
-- 二、 RBAC 关联表 (多对多)
-- ============================================================

-- 用户 - 角色
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 角色 - 菜单
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 角色 - 部门 (data_scope=5 自定义时使用)
CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id)
);


-- ============================================================
-- 三、 日志表
-- ============================================================

-- 操作日志 (AOP 自动写入)
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id              BIGSERIAL    PRIMARY KEY,
    title           VARCHAR(64),                          -- 模块名 (来自 @Log.title)
    business_type   VARCHAR(16),                          -- CREATE/UPDATE/DELETE/EXPORT/IMPORT/GRANT/...
    method          VARCHAR(255),                         -- 完整方法签名
    request_method  VARCHAR(8),                           -- HTTP 方法
    oper_url        VARCHAR(255),                         -- 请求 URI
    oper_param      TEXT,                                 -- 请求参数 (敏感字段已脱敏)
    json_result     TEXT,                                 -- 返回结果
    oper_id         BIGINT,
    oper_name       VARCHAR(64),
    dept_id         BIGINT,
    dept_name       VARCHAR(64),
    oper_ip         VARCHAR(64),
    user_agent      VARCHAR(500),
    status          SMALLINT     DEFAULT 1,                -- 0 失败 / 1 成功
    error_msg       TEXT,
    cost_time       BIGINT,                               -- 耗时 (ms)
    oper_time       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_oper_user ON sys_oper_log(oper_id);
CREATE INDEX IF NOT EXISTS idx_oper_time ON sys_oper_log(oper_time);
COMMENT ON TABLE sys_oper_log IS '操作日志 (AOP 自动记录)';


-- 登录日志
CREATE TABLE IF NOT EXISTS sys_login_log (
    id          BIGSERIAL    PRIMARY KEY,
    user_name   VARCHAR(64),
    ip          VARCHAR(64),
    user_agent  VARCHAR(500),
    browser     VARCHAR(64),
    os          VARCHAR(64),
    status      VARCHAR(16),                              -- SUCCESS / FAIL
    message     VARCHAR(500),
    login_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_login_user_time ON sys_login_log(user_name, login_time);
COMMENT ON TABLE sys_login_log IS '登录日志';


-- ============================================================
-- 四、 业务配置表
-- ============================================================

-- 通知公告
CREATE TABLE IF NOT EXISTS sys_notice (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(128) NOT NULL,
    type        VARCHAR(16),                             -- notice 通知 / announcement 公告
    content     TEXT,
    status      SMALLINT     DEFAULT 1,
    remark      VARCHAR(500),
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 字典类型
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,                    -- 类型名 (中文)
    type        VARCHAR(64)  NOT NULL UNIQUE,             -- 类型编码 (英文, 代码引用)
    status      SMALLINT     DEFAULT 1,
    remark      VARCHAR(500),
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 字典数据
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id          BIGSERIAL    PRIMARY KEY,
    dict_type   VARCHAR(64)  NOT NULL,                    -- 关联 sys_dict_type.type
    label       VARCHAR(64)  NOT NULL,                    -- 显示标签
    value       VARCHAR(64)  NOT NULL,                    -- 实际值
    css_class   VARCHAR(64),                             -- tag class (如 primary)
    list_class  VARCHAR(64),
    is_default  SMALLINT     DEFAULT 0,                   -- 是否默认
    order_num   INT          DEFAULT 0,
    status      SMALLINT     DEFAULT 1,
    remark      VARCHAR(500),
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_dict_type ON sys_dict_data(dict_type);

-- 参数配置 (key-value)
CREATE TABLE IF NOT EXISTS sys_config (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,
    config_key   VARCHAR(64)  NOT NULL UNIQUE,
    config_value TEXT,
    config_type  SMALLINT     DEFAULT 1,                  -- 1 系统 / 2 业务
    is_builtin   SMALLINT     DEFAULT 0,                  -- 是否内置 (不可删)
    remark       VARCHAR(500),
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 五、 扩展表
-- ============================================================

-- 文件上传记录
CREATE TABLE IF NOT EXISTS sys_file (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,                   -- 存储文件名
    original_name VARCHAR(255),                           -- 原始文件名
    bucket        VARCHAR(64),                            -- 存储桶 (本地/OSS/MinIO)
    path          VARCHAR(500) NOT NULL,                   -- 存储路径
    url           VARCHAR(500),                           -- 访问 URL
    content_type  VARCHAR(128),
    size          BIGINT,
    md5           VARCHAR(64),
    storage_type  VARCHAR(16),                            -- local / minio / oss
    upload_by     VARCHAR(64),
    upload_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 定时任务
CREATE TABLE IF NOT EXISTS sys_job (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,
    bean_name    VARCHAR(128) NOT NULL,                   -- Spring Bean 名称
    method_name  VARCHAR(128) NOT NULL,
    params       VARCHAR(500),
    cron         VARCHAR(64)  NOT NULL,                   -- Quartz cron 表达式
    status       SMALLINT     DEFAULT 1,                  -- 0 暂停 / 1 运行
    remark       VARCHAR(500),
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 定时任务执行日志
CREATE TABLE IF NOT EXISTS sys_job_log (
    id          BIGSERIAL    PRIMARY KEY,
    job_id      BIGINT       NOT NULL,
    job_name    VARCHAR(64),
    bean_name   VARCHAR(128),
    method_name VARCHAR(128),
    params      VARCHAR(500),
    status      SMALLINT,                                -- 0 失败 / 1 成功
    error_msg   TEXT,
    cost_time   BIGINT,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 六、 代码生成元数据表
-- ============================================================

-- 代码生成 - 表
CREATE TABLE IF NOT EXISTS gen_table (
    id             BIGSERIAL    PRIMARY KEY,
    table_name     VARCHAR(64)  NOT NULL UNIQUE,
    table_comment  VARCHAR(255),
    class_name     VARCHAR(128),
    tpl_category   VARCHAR(16),                           -- crud / tree
    package_name   VARCHAR(128),
    module_name    VARCHAR(64),
    business_name  VARCHAR(64),
    function_name  VARCHAR(128),
    gen_type       VARCHAR(16),                           -- zip / project-path
    options        VARCHAR(500),
    create_by      VARCHAR(64),
    create_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by      VARCHAR(64),
    update_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 代码生成 - 字段
CREATE TABLE IF NOT EXISTS gen_table_column (
    id              BIGSERIAL    PRIMARY KEY,
    table_id        BIGINT       NOT NULL,                -- 关联 gen_table.id
    column_name     VARCHAR(64)  NOT NULL,
    column_comment  VARCHAR(255),
    column_type     VARCHAR(64),                          -- DB 类型 (varchar(64) 等)
    java_type       VARCHAR(64),                          -- Java 类型 (String / Long / ...)
    java_field      VARCHAR(128),                         -- 驼峰字段名
    is_pk           SMALLINT,                             -- 是否主键
    is_increment    SMALLINT,                             -- 是否自增
    is_required     SMALLINT,                             -- 是否必填
    is_insert       SMALLINT     DEFAULT 1,               -- 是否插入字段
    is_edit         SMALLINT     DEFAULT 1,               -- 是否编辑字段
    is_list         SMALLINT     DEFAULT 1,               -- 是否列表字段
    is_query        SMALLINT     DEFAULT 1,               -- 是否查询字段
    query_type      VARCHAR(16),                          -- EQ / LIKE / BETWEEN
    html_type       VARCHAR(32),                          -- input / textarea / select / datetime
    dict_type       VARCHAR(64),                          -- 关联字典 type
    sort            INT          DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_gen_table_id ON gen_table_column(table_id);


-- ============================================================
-- 七、 初始数据 (Seed)
-- ============================================================

-- 7.1 部门
INSERT INTO sys_dept (id, parent_id, ancestors, name, code, order_num, status, del_flag, create_by, create_time)
VALUES (1, 0, '0/', '总公司', 'root', 0, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 7.2 角色
INSERT INTO sys_role (id, name, code, data_scope, status, del_flag, create_by, create_time) VALUES
    (1, '超级管理员', 'admin', 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (2, '普通用户',   'common', 5, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 7.3 用户
-- admin / admin123 (BCrypt of 'admin123')
INSERT INTO sys_user (id, username, password, nick_name, email, status, dept_id, del_flag, create_by, create_time) VALUES
    (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
        '超级管理员', 'admin@marry.com', 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (2, 'demo',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
        '演示账号',    '',                  1, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 7.4 用户-角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2) ON CONFLICT DO NOTHING;


-- 7.5 菜单 (含按钮级别权限)
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time) VALUES
    -- 顶层目录
    (1,   0, '系统管理', 'M', 'system',  null,          '',                'SettingsOutline',   1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (40,  0, '系统监控', 'M', 'monitor',  null,          '',                'PulseOutline',      2, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (70,  0, '系统工具', 'M', 'tool',     null,          '',                'BuildOutline',      3, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 用户管理
    (2,   1, '用户管理', 'C', 'user',     'system/user/index',     'system:user:list',   'PersonOutline',      1, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (20,  2, '用户查询', 'F', null, null,  'system:user:query',    '',                    1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (21,  2, '用户新增', 'F', null, null,  'system:user:add',      '#',                   2, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (22,  2, '用户修改', 'F', null, null,  'system:user:edit',     '#',                   3, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (23,  2, '用户删除', 'F', null, null,  'system:user:remove',   '#',                   4, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (24,  2, '重置密码', 'F', null, null,  'system:user:resetPwd', '#',                   5, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 角色管理
    (3,   1, '角色管理', 'C', 'role',     'system/role/index',     'system:role:list',   'PeopleCircleOutline',2, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (30,  3, '角色查询', 'F', null, null,  'system:role:query',    '#',                   1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (31,  3, '角色新增', 'F', null, null,  'system:role:add',      '#',                   2, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (32,  3, '角色修改', 'F', null, null,  'system:role:edit',     '#',                   3, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (33,  3, '角色删除', 'F', null, null,  'system:role:remove',   '#',                   4, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 菜单管理
    (4,   1, '菜单管理', 'C', 'menu',     'system/menu/index',     'system:menu:list',   'MenuOutline',        3, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (41,  4, '菜单查询', 'F', null, null,  'system:menu:query',    '#',                   1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (42,  4, '菜单新增', 'F', null, null,  'system:menu:add',      '#',                   2, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (43,  4, '菜单修改', 'F', null, null,  'system:menu:edit',     '#',                   3, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (44,  4, '菜单删除', 'F', null, null,  'system:menu:remove',   '#',                   4, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 部门管理
    (5,   1, '部门管理', 'C', 'dept',     'system/dept/index',     'system:dept:list',   'BusinessOutline',    4, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (50,  5, '部门查询', 'F', null, null,  'system:dept:query',    '#',                   1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (51,  5, '部门新增', 'F', null, null,  'system:dept:add',      '#',                   2, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (52,  5, '部门修改', 'F', null, null,  'system:dept:edit',     '#',                   3, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (53,  5, '部门删除', 'F', null, null,  'system:dept:remove',   '#',                   4, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 字典管理
    (6,   1, '字典管理', 'C', 'dict',     'system/dict/index',     'system:dict:list',   'BookOutline',        6, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (60,  6, '字典查询', 'F', null, null,  'system:dict:query',    '#',                   1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (61,  6, '字典新增', 'F', null, null,  'system:dict:add',      '#',                   2, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (62,  6, '字典修改', 'F', null, null,  'system:dict:edit',     '#',                   3, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (63,  6, '字典删除', 'F', null, null,  'system:dict:remove',   '#',                   4, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 参数设置
    (7,   1, '参数设置', 'C', 'config',   'system/config/index',   'system:config:list', 'OptionsOutline',     7, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (70,  7, '参数新增', 'F', null, null,  'system:config:add',    '#',                   1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (71,  7, '参数修改', 'F', null, null,  'system:config:edit',   '#',                   2, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    (72,  7, '参数删除', 'F', null, null,  'system:config:remove', '#',                   3, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 操作日志
    (410, 40, '操作日志', 'C', 'operlog',  'monitor/operlog/index', 'monitor:operlog:list',  'ListOutline',  1, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (411, 410,'日志删除', 'F', null, null, 'monitor:operlog:remove','#',                    1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 登录日志
    (420, 40, '登录日志', 'C', 'loginlog', 'monitor/loginlog/index','monitor:loginlog:list','LogInOutline', 2, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (421, 420,'日志删除', 'F', null, null, 'monitor:loginlog:remove','#',                   1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 在线用户
    (430, 40, '在线用户', 'C', 'online',   'monitor/online/index',  'monitor:online:list', 'GlobeOutline',      3, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (431, 430,'强制下线', 'F', null, null, 'monitor:online:forceLogout','#',                1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP),
    -- 代码生成
    (700, 70, '代码生成', 'C', 'gen',      'tool/gen/index',        'tool:gen:list',       'CodeSlashOutline',  1, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 7.6 角色-菜单 (超级管理员 = 全部菜单)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu ON CONFLICT DO NOTHING;

-- 普通用户 = 只读菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7),
    (2, 20), (2, 30), (2, 41), (2, 50), (2, 60), (2, 70)
ON CONFLICT DO NOTHING;


-- 7.7 字典: sys_user_sex
INSERT INTO sys_dict_type (id, name, type, status, del_flag, create_by, create_time)
VALUES (1, '用户性别', 'sys_user_sex', 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_dict_data (dict_type, label, value, is_default, order_num, status, del_flag, create_by, create_time) VALUES
    ('sys_user_sex', '男',   '1', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    ('sys_user_sex', '女',   '2', 0, 2, 1, 0, 'system', CURRENT_TIMESTAMP),
    ('sys_user_sex', '未知', '0', 0, 3, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;


-- 7.8 欢迎公告
INSERT INTO sys_notice (id, title, type, content, status, del_flag, create_by, create_time)
VALUES (1, '欢迎使用 marry-platform', 'notice', '这是 marry-platform 多模块 RBAC 平台的初始通知。', 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- 完
-- 默认账号: admin / admin123  (BCrypt)
--          demo  / admin123
-- ============================================================