-- Align DB with API: backend expects table "project" (singular).
-- Safe to run multiple times.

DO $$
BEGIN
    IF to_regclass('public.project') IS NULL AND to_regclass('public.projects') IS NOT NULL THEN
        ALTER TABLE projects RENAME TO project;
        BEGIN
            ALTER TABLE project ALTER COLUMN description TYPE TEXT;
        EXCEPTION
            WHEN others THEN NULL;
        END;
        RAISE NOTICE 'Renamed table projects -> project';
    ELSIF to_regclass('public.project') IS NULL THEN
        CREATE TABLE project (
            project_id   SERIAL PRIMARY KEY,
            project_name VARCHAR(100) NOT NULL,
            company_id   INTEGER REFERENCES company (company_id) ON DELETE CASCADE,
            created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at   TIMESTAMP,
            deleted_at   TIMESTAMP,
            description  TEXT
        );
        RAISE NOTICE 'Created table project';
    END IF;
END $$;
