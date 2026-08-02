-- ============================================================
-- V2026.08.02.1000 — add child_event module (growth records)
-- table + attachments + menu + permissions
-- ============================================================

CREATE TABLE IF NOT EXISTS child_event (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT,                    -- markdown body, inline image urls included
    event_date  DATE         NOT NULL,   -- event date (timeline grouping key)
    category    VARCHAR(50),             -- 学习/运动/日常/纪念/成长
    tags        VARCHAR(255),            -- comma-separated
    importance  SMALLINT     DEFAULT 0,  -- 0 normal, 1 important, 2 milestone
    mood        VARCHAR(50),
    dir_name    VARCHAR(200),            -- disk directory, e.g. 2026-09-01_开学第一天
    del_flag    SMALLINT     DEFAULT 0,
    create_by   BIGINT,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   BIGINT,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_event_date  ON child_event(event_date);
CREATE INDEX IF NOT EXISTS idx_event_owner ON child_event(create_by);
CREATE INDEX IF NOT EXISTS idx_event_title ON child_event(title);

CREATE TABLE IF NOT EXISTS child_event_file (
    id          BIGSERIAL    PRIMARY KEY,
    event_id    BIGINT       NOT NULL,
    file_id     BIGINT       NOT NULL,   -- refs sys_file.id
    media_type  VARCHAR(20)  DEFAULT 'image',  -- image / file
    sort_no     SMALLINT     DEFAULT 0,
    create_by   BIGINT,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_event_file_event ON child_event_file(event_id);

-- ============================================================
-- 菜单：顶层"成长记录"目录 + 事件时间线页面 + 按钮权限
-- ============================================================

-- 顶层 成长记录 目录 (M)
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (10, 0, '成长记录', 'M', 'event', null, '', 'Camera', 5, 1, 1, 0, 1, 0, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 事件时间线 页面 (C)
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (100, 10, '事件时间线', 'C', 'list', 'event/index', 'event:list', 'DocumentTextOutline', 1, 1, 1, 1, 1, 0, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 按钮权限 (F)
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time) VALUES
    (1000, 100, '事件查询', 'F', null, null, 'event:query',  '#', 1, 1, 1, 0, 1, CURRENT_TIMESTAMP),
    (1001, 100, '事件新增', 'F', null, null, 'event:add',    '#', 2, 1, 1, 0, 1, CURRENT_TIMESTAMP),
    (1002, 100, '事件修改', 'F', null, null, 'event:edit',   '#', 3, 1, 1, 0, 1, CURRENT_TIMESTAMP),
    (1003, 100, '事件删除', 'F', null, null, 'event:remove', '#', 4, 1, 1, 0, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Grant to admin role (super-admin gets everything)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id IN (10, 100, 1000, 1001, 1002, 1003)
ON CONFLICT DO NOTHING;

-- Grant to common role (read-only)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 10), (2, 100), (2, 1000)
ON CONFLICT DO NOTHING;
