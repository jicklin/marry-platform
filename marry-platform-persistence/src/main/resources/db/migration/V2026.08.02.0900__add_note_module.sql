-- ============================================================
-- V2026.08.02.0900 — add note module (notes app)
-- table + menu + permissions
-- ============================================================

CREATE TABLE IF NOT EXISTS sys_note (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT,
    tags        VARCHAR(255),
    is_pinned   SMALLINT     DEFAULT 0,
    status      SMALLINT     DEFAULT 1,
    remark      VARCHAR(500),
    del_flag    SMALLINT     DEFAULT 0,
    create_by   BIGINT,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   BIGINT,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_note_title ON sys_note(title);
CREATE INDEX IF NOT EXISTS idx_note_pinned ON sys_note(is_pinned);

-- ============================================================
-- 菜单：顶层"笔记"目录 + 笔记管理页面 + 按钮权限
-- ============================================================

-- 顶层 笔记 目录 (M)
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (9, 0, '笔记', 'M', 'note', null, '', 'ReaderOutline', 4, 1, 1, 0, 1, 0, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 笔记管理 页面 (C)
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (90, 9, '我的笔记', 'C', 'list', 'note/index', 'note:list', 'DocumentTextOutline', 1, 1, 1, 1, 1, 0, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 按钮权限 (F)
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time) VALUES
    (900, 90, '笔记查询', 'F', null, null, 'note:query', '#', 1, 1, 1, 0, 1, CURRENT_TIMESTAMP),
    (901, 90, '笔记新增', 'F', null, null, 'note:add',   '#', 2, 1, 1, 0, 1, CURRENT_TIMESTAMP),
    (902, 90, '笔记修改', 'F', null, null, 'note:edit',  '#', 3, 1, 1, 0, 1, CURRENT_TIMESTAMP),
    (903, 90, '笔记删除', 'F', null, null, 'note:remove','#', 4, 1, 1, 0, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Grant to admin role (super-admin gets everything)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id IN (9, 90, 900, 901, 902, 903)
ON CONFLICT DO NOTHING;

-- Grant to common role (read-only)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 9), (2, 90), (2, 900)
ON CONFLICT DO NOTHING;
