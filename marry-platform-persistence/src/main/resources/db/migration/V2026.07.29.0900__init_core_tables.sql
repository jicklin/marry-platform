-- ============================================================
-- marry-platform  -- core schema  (PostgreSQL)
-- ============================================================

-- sys_user
CREATE TABLE IF NOT EXISTS sys_user (
    id           BIGSERIAL    PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,
    nick_name    VARCHAR(64),
    email        VARCHAR(128),
    phone        VARCHAR(32),
    avatar       VARCHAR(255),
    sex          SMALLINT     DEFAULT 0,
    dept_id      BIGINT,
    status       SMALLINT     DEFAULT 1,
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
COMMENT ON TABLE sys_user IS 'User accounts';

-- sys_role
CREATE TABLE IF NOT EXISTS sys_role (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,
    code         VARCHAR(64)  NOT NULL UNIQUE,
    data_scope   SMALLINT     DEFAULT 1,
    status       SMALLINT     DEFAULT 1,
    remark       VARCHAR(500),
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- sys_menu
CREATE TABLE IF NOT EXISTS sys_menu (
    id           BIGSERIAL    PRIMARY KEY,
    parent_id    BIGINT       DEFAULT 0,
    name         VARCHAR(64)  NOT NULL,
    menu_type    VARCHAR(8)   NOT NULL,
    path         VARCHAR(255),
    component    VARCHAR(255),
    perm         VARCHAR(128),
    icon         VARCHAR(64),
    order_num    INT          DEFAULT 0,
    visible      SMALLINT     DEFAULT 1,
    status       SMALLINT     DEFAULT 1,
    is_cache     SMALLINT     DEFAULT 0,
    is_frame     SMALLINT     DEFAULT 1,
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_menu_parent ON sys_menu(parent_id);
COMMENT ON COLUMN sys_menu.menu_type IS 'M directory, C menu, F button';

-- sys_dept
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
    ancestors    VARCHAR(500),
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- joins
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);
CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id)
);

-- sys_oper_log
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id              BIGSERIAL    PRIMARY KEY,
    title           VARCHAR(64),
    business_type   VARCHAR(16),
    method          VARCHAR(255),
    request_method  VARCHAR(8),
    oper_url        VARCHAR(255),
    oper_param      TEXT,
    json_result     TEXT,
    oper_id         BIGINT,
    oper_name       VARCHAR(64),
    dept_id         BIGINT,
    dept_name       VARCHAR(64),
    oper_ip         VARCHAR(64),
    user_agent      VARCHAR(500),
    status          SMALLINT     DEFAULT 1,
    error_msg       TEXT,
    cost_time       BIGINT,
    oper_time       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_oper_user ON sys_oper_log(oper_id);
CREATE INDEX IF NOT EXISTS idx_oper_time ON sys_oper_log(oper_time);

-- sys_login_log
CREATE TABLE IF NOT EXISTS sys_login_log (
    id          BIGSERIAL    PRIMARY KEY,
    user_name   VARCHAR(64),
    ip          VARCHAR(64),
    user_agent  VARCHAR(500),
    browser     VARCHAR(64),
    os          VARCHAR(64),
    status      VARCHAR(16),
    message     VARCHAR(500),
    login_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_login_user_time ON sys_login_log(user_name, login_time);

-- sys_notice
CREATE TABLE IF NOT EXISTS sys_notice (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(128) NOT NULL,
    type        VARCHAR(16),
    content     TEXT,
    status      SMALLINT     DEFAULT 1,
    remark      VARCHAR(500),
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- sys_dict_type
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    type        VARCHAR(64)  NOT NULL UNIQUE,
    status      SMALLINT     DEFAULT 1,
    remark      VARCHAR(500),
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- sys_dict_data
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id          BIGSERIAL    PRIMARY KEY,
    dict_type   VARCHAR(64)  NOT NULL,
    label       VARCHAR(64)  NOT NULL,
    value       VARCHAR(64)  NOT NULL,
    css_class   VARCHAR(64),
    list_class  VARCHAR(64),
    is_default  SMALLINT     DEFAULT 0,
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

-- sys_config
CREATE TABLE IF NOT EXISTS sys_config (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,
    config_key   VARCHAR(64)  NOT NULL UNIQUE,
    config_value TEXT,
    config_type  SMALLINT     DEFAULT 1,
    is_builtin   SMALLINT     DEFAULT 0,
    remark       VARCHAR(500),
    del_flag     SMALLINT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- sys_file
CREATE TABLE IF NOT EXISTS sys_file (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    bucket        VARCHAR(64),
    path          VARCHAR(500) NOT NULL,
    url           VARCHAR(500),
    content_type  VARCHAR(128),
    size          BIGINT,
    md5           VARCHAR(64),
    storage_type  VARCHAR(16),
    upload_by     VARCHAR(64),
    upload_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- sys_job
CREATE TABLE IF NOT EXISTS sys_job (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,
    bean_name    VARCHAR(128) NOT NULL,
    method_name  VARCHAR(128) NOT NULL,
    params       VARCHAR(500),
    cron         VARCHAR(64)  NOT NULL,
    status       SMALLINT     DEFAULT 1,
    remark       VARCHAR(500),
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- sys_job_log
CREATE TABLE IF NOT EXISTS sys_job_log (
    id          BIGSERIAL    PRIMARY KEY,
    job_id      BIGINT       NOT NULL,
    job_name    VARCHAR(64),
    bean_name   VARCHAR(128),
    method_name VARCHAR(128),
    params      VARCHAR(500),
    status      SMALLINT,
    error_msg   TEXT,
    cost_time   BIGINT,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- gen_table
CREATE TABLE IF NOT EXISTS gen_table (
    id             BIGSERIAL    PRIMARY KEY,
    table_name     VARCHAR(64)  NOT NULL UNIQUE,
    table_comment  VARCHAR(255),
    class_name     VARCHAR(128),
    tpl_category   VARCHAR(16),
    package_name   VARCHAR(128),
    module_name    VARCHAR(64),
    business_name  VARCHAR(64),
    function_name  VARCHAR(128),
    gen_type       VARCHAR(16),
    options        VARCHAR(500),
    create_by      VARCHAR(64),
    create_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by      VARCHAR(64),
    update_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- gen_table_column
CREATE TABLE IF NOT EXISTS gen_table_column (
    id              BIGSERIAL    PRIMARY KEY,
    table_id        BIGINT       NOT NULL,
    column_name     VARCHAR(64)  NOT NULL,
    column_comment  VARCHAR(255),
    column_type     VARCHAR(64),
    java_type       VARCHAR(64),
    java_field      VARCHAR(128),
    is_pk           SMALLINT,
    is_increment    SMALLINT,
    is_required     SMALLINT,
    is_insert       SMALLINT     DEFAULT 1,
    is_edit         SMALLINT     DEFAULT 1,
    is_list         SMALLINT     DEFAULT 1,
    is_query        SMALLINT     DEFAULT 1,
    query_type      VARCHAR(16),
    html_type       VARCHAR(32),
    dict_type       VARCHAR(64),
    sort            INT          DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_gen_table_id ON gen_table_column(table_id);