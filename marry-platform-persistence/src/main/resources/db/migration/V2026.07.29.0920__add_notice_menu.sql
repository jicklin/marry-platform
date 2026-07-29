-- ============================================================
-- V2026.07.29.0920 — add notice menu + permissions
-- ============================================================
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (8, 1, '通知公告', 'C', 'notice', 'system/notice/index', 'system:notice:list', 'NotificationsOutline', 5, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time) VALUES
    (80, 8, '公告查询', 'F', null, null, 'system:notice:query', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (81, 8, '公告新增', 'F', null, null, 'system:notice:add',   '#', 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (82, 8, '公告修改', 'F', null, null, 'system:notice:edit',  '#', 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
    (83, 8, '公告删除', 'F', null, null, 'system:notice:remove','#', 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Grant to admin role (super-admin gets everything)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id IN (8, 80, 81, 82, 83)
ON CONFLICT DO NOTHING;

-- Grant to common role (read-only)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 8), (2, 80)
ON CONFLICT DO NOTHING;