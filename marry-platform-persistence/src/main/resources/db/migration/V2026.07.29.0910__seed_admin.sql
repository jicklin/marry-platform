-- ============================================================
-- Seed admin user, role, menus, role-menu mapping, dept
-- ============================================================

-- Dept
INSERT INTO sys_dept (id, parent_id, ancestors, name, code, order_num, status, del_flag, create_by, create_time)
VALUES (1, 0, '0/', '总公司', 'root', 0, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Role (super admin)
INSERT INTO sys_role (id, name, code, data_scope, status, del_flag, create_by, create_time)
VALUES (1, '超级管理员', 'admin', 1, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Role (common)
INSERT INTO sys_role (id, name, code, data_scope, status, del_flag, create_by, create_time)
VALUES (2, '普通用户', 'common', 5, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- User (admin / admin123 BCrypt)
INSERT INTO sys_user (id, username, password, nick_name, email, status, dept_id, del_flag, create_by, create_time)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
        '超级管理员', 'admin@marry.com', 1, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (id, username, password, nick_name, status, dept_id, del_flag, create_by, create_time)
VALUES (2, 'demo', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
        '演示账号', 1, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- User-role mapping
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1) ON CONFLICT DO NOTHING;
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2) ON CONFLICT DO NOTHING;

-- ============================================================
-- Menus (with button-level perms)
-- ============================================================

-- 顶层 system 目录
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (1, 0, '系统管理', 'M', 'system', null, '', 'SettingsOutline', 1, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 顶层 monitor 目录
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (40, 0, '系统监控', 'M', 'monitor', null, '', 'PulseOutline', 2, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 顶层 tool 目录
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (70, 0, '系统工具', 'M', 'tool', null, '', 'BuildOutline', 3, 1, 1, 0, 1, 0, 'system', CURRENT_TIMESTAMP);

-- ===== system 子菜单 =====
-- 用户管理  2
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (2, 1, '用户管理', 'C', 'user', 'system/user/index', 'system:user:list', 'PersonOutline', 1, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (20, 2, '用户查询', 'F', null, null, 'system:user:query', '', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (21, 2, '用户新增', 'F', null, null, 'system:user:add', '#', 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (22, 2, '用户修改', 'F', null, null, 'system:user:edit', '#', 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (23, 2, '用户删除', 'F', null, null, 'system:user:remove', '#', 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (24, 2, '重置密码', 'F', null, null, 'system:user:resetPwd', '#', 5, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 角色管理  3
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (3, 1, '角色管理', 'C', 'role', 'system/role/index', 'system:role:list', 'PeopleCircleOutline', 2, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (30, 3, '角色查询', 'F', null, null, 'system:role:query', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (31, 3, '角色新增', 'F', null, null, 'system:role:add', '#', 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (32, 3, '角色修改', 'F', null, null, 'system:role:edit', '#', 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (33, 3, '角色删除', 'F', null, null, 'system:role:remove', '#', 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 菜单管理  4
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (4, 1, '菜单管理', 'C', 'menu', 'system/menu/index', 'system:menu:list', 'MenuOutline', 3, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (41, 4, '菜单查询', 'F', null, null, 'system:menu:query', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (42, 4, '菜单新增', 'F', null, null, 'system:menu:add', '#', 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (43, 4, '菜单修改', 'F', null, null, 'system:menu:edit', '#', 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (44, 4, '菜单删除', 'F', null, null, 'system:menu:remove', '#', 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 部门管理  5
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (5, 1, '部门管理', 'C', 'dept', 'system/dept/index', 'system:dept:list', 'BusinessOutline', 4, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (50, 5, '部门查询', 'F', null, null, 'system:dept:query', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (51, 5, '部门新增', 'F', null, null, 'system:dept:add', '#', 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (52, 5, '部门修改', 'F', null, null, 'system:dept:edit', '#', 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (53, 5, '部门删除', 'F', null, null, 'system:dept:remove', '#', 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 字典管理  6
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (6, 1, '字典管理', 'C', 'dict', 'system/dict/index', 'system:dict:list', 'BookOutline', 6, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (60, 6, '字典查询', 'F', null, null, 'system:dict:query', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (61, 6, '字典新增', 'F', null, null, 'system:dict:add', '#', 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (62, 6, '字典修改', 'F', null, null, 'system:dict:edit', '#', 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (63, 6, '字典删除', 'F', null, null, 'system:dict:remove', '#', 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 参数设置  7
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (7, 1, '参数设置', 'C', 'config', 'system/config/index', 'system:config:list', 'OptionsOutline', 7, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (70, 7, '参数新增', 'F', null, null, 'system:config:add', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (71, 7, '参数修改', 'F', null, null, 'system:config:edit', '#', 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (72, 7, '参数删除', 'F', null, null, 'system:config:remove', '#', 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- ===== monitor 子菜单 =====
-- 操作日志 41
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (410, 40, '操作日志', 'C', 'operlog', 'monitor/operlog/index', 'monitor:operlog:list', 'ListOutline', 1, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (411, 410, '日志删除', 'F', null, null, 'monitor:operlog:remove', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 登录日志 42
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (420, 40, '登录日志', 'C', 'loginlog', 'monitor/loginlog/index', 'monitor:loginlog:list', 'LogInOutline', 2, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (421, 420, '日志删除', 'F', null, null, 'monitor:loginlog:remove', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- 在线用户 43
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (430, 40, '在线用户', 'C', 'online', 'monitor/online/index', 'monitor:online:list', 'GlobeOutline', 3, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, del_flag, create_by, create_time)
VALUES (431, 430, '强制下线', 'F', null, null, 'monitor:online:forceLogout', '#', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- ===== tool 子菜单 =====
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, order_num, visible, status, is_cache, is_frame, del_flag, create_by, create_time)
VALUES (700, 70, '代码生成', 'C', 'gen', 'tool/gen/index', 'tool:gen:list', 'CodeSlashOutline', 1, 1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP);

-- ============================================================
-- 角色-菜单 关联  （超级管理员获得全部）
-- ============================================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu
ON CONFLICT DO NOTHING;

-- 普通用户只读
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7),
    (2, 20), (2, 30), (2, 41), (2, 50), (2, 60), (2, 70)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 字典：sys_user_sex
-- ============================================================
INSERT INTO sys_dict_type (id, name, type, status, del_flag, create_by, create_time)
VALUES (1, '用户性别', 'sys_user_sex', 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_dict_data (dict_type, label, value, is_default, order_num, status, del_flag, create_by, create_time)
VALUES ('sys_user_sex', '男', '1', 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP),
       ('sys_user_sex', '女', '2', 0, 2, 1, 0, 'system', CURRENT_TIMESTAMP),
       ('sys_user_sex', '未知', '0', 0, 3, 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 通知公告（默认一条欢迎）
-- ============================================================
INSERT INTO sys_notice (id, title, type, content, status, del_flag, create_by, create_time)
VALUES (1, '欢迎使用 marry-platform', 'notice', '这是 marry-platform 多模块 RBAC 平台的初始通知。', 1, 0, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;