-- ============================================================
-- Convert create_by / update_by columns from VARCHAR(64) to BIGINT
-- so they store SysUser.id directly. Non-numeric legacy values
-- (e.g. seed insert of literal 'system') are coerced to NULL.
-- ============================================================

-- Apply to every table that ships audit columns.
DO $$
DECLARE
    t text;
    tables text[] := ARRAY[
        'sys_user',
        'sys_role',
        'sys_menu',
        'sys_dept',
        'sys_notice',
        'sys_dict_type',
        'sys_dict_data',
        'sys_config',
        'sys_file',
        'sys_job',
        'sys_oper_log',
        'sys_login_log',
        'gen_table'
    ];
BEGIN
    FOREACH t IN LOOP tables
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = t AND column_name = 'create_by'
              AND data_type <> 'bigint'
        ) THEN
            EXECUTE format(
                'ALTER TABLE %I ALTER COLUMN create_by TYPE BIGINT USING '
                || 'CASE WHEN create_by ~ ''^[0-9]+$'' THEN create_by::BIGINT ELSE NULL END',
                t
            );
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = t AND column_name = 'update_by'
              AND data_type <> 'bigint'
        ) THEN
            EXECUTE format(
                'ALTER TABLE %I ALTER COLUMN update_by TYPE BIGINT USING '
                || 'CASE WHEN update_by ~ ''^[0-9]+$'' THEN update_by::BIGINT ELSE NULL END',
                t
            );
        END IF;
    END LOOP;
END $$;
