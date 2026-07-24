-- Run in pgAdmin on bugzapper_db if you cannot restart the API right now.
-- Fixes: user_roles_project_id_fkey (and any other FK) pointing at "projects" instead of "project".

ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_project_id_fkey;
ALTER TABLE user_roles
    ADD CONSTRAINT user_roles_project_id_fkey
    FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE CASCADE;

-- phases (legacy schema from users-add-is-verified-columns.sql)
ALTER TABLE phases DROP CONSTRAINT IF EXISTS phases_project_id_fkey;
ALTER TABLE phases
    ADD CONSTRAINT phases_project_id_fkey
    FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE CASCADE;

-- project_members (if present)
DO $$
BEGIN
    IF to_regclass('public.project_members') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE project_members DROP CONSTRAINT IF EXISTS project_members_project_id_fkey';
        EXECUTE 'ALTER TABLE project_members ADD CONSTRAINT project_members_project_id_fkey FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE CASCADE';
    END IF;
END $$;

-- notification (if column was added with wrong FK)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'notification' AND column_name = 'project_id'
    ) THEN
        EXECUTE 'ALTER TABLE notification DROP CONSTRAINT IF EXISTS notification_project_id_fkey';
        BEGIN
            EXECUTE 'ALTER TABLE notification ADD CONSTRAINT notification_project_id_fkey FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE SET NULL';
        EXCEPTION WHEN others THEN
            NULL;
        END;
    END IF;
END $$;

-- Optional: remove duplicate empty legacy table after FKs are fixed
-- DROP TABLE IF EXISTS projects CASCADE;
