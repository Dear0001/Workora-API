-- Aligns `notification` table with NotificationRepository / Java entity.
-- Fixes: "column company_id does not exist", "column user_role_id does not exist", etc.
-- Run once against existing databases that were created from an older schema.
-- Mirror: docs/db/queue-migrate-notification-app-columns.sql

ALTER TABLE notification ADD COLUMN IF NOT EXISTS company_id INTEGER REFERENCES company(company_id);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS project_id INTEGER REFERENCES project(project_id);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS phase_id INTEGER REFERENCES phases(phase_id);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS task_id INTEGER REFERENCES tasks(task_id);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS report_id INTEGER REFERENCES reports(report_id);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS apply_id INTEGER REFERENCES apply(apply_id);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS user_role_id INTEGER REFERENCES user_roles(user_role_id);


-- =============================================================================
-- Bug Zapper — PostgreSQL baseline schema (Queue 8)
-- =============================================================================
-- How to use:
--   1) As superuser: CREATE DATABASE bugzapper_db;  CREATE USER bugzapper WITH PASSWORD '...'; GRANT ALL ON DATABASE bugzapper_db TO bugzapper;
--   2) Connect to bugzapper_db:  \c bugzapper_db
--   3) Run this entire file as the owner of the database (often postgres or bugzapper).
--
-- Notes:
--   - users.is_verified is REQUIRED by the API (OTP verify, login). NOT NULL DEFAULT false.
--   - roles: first five inserts get serial IDs 1..5; BUG HUNTER must be role_id = 5 (see AuthenticationController).
--   - Do NOT duplicate-seed role_id 5 after the five INSERTs below.
-- =============================================================================

-- Optional: create database (run while connected to postgres, not inside bugzapper_db)
-- CREATE DATABASE bugzapper_db;

CREATE TABLE company
(
    company_id    SERIAL PRIMARY KEY,
    company_name  VARCHAR(100) NOT NULL,
    description   TEXT,
    disabled_at   TIMESTAMP,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    deleted_at    TIMESTAMP,
    profile_image VARCHAR(255),
    cover_image   VARCHAR(255),
    invite_code   VARCHAR(15),
    phone         VARCHAR(20),
    address       VARCHAR(255),
    email         VARCHAR(255)
);

CREATE TABLE project
(
    project_id   SERIAL PRIMARY KEY,
    project_name VARCHAR(100) NOT NULL,
    company_id   INTEGER REFERENCES company ON DELETE CASCADE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    deleted_at   TIMESTAMP,
    description  TEXT
);

CREATE TABLE phases
(
    phase_id    SERIAL PRIMARY KEY,
    phase_name  VARCHAR(100) NOT NULL,
    description TEXT,
    image       VARCHAR(255) NOT NULL,
    project_id  INTEGER REFERENCES project ON DELETE CASCADE,
    is_private  BOOLEAN   DEFAULT TRUE,
    link        VARCHAR(100),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    price       DOUBLE PRECISION
);

CREATE TABLE phase_attachment
(
    phase_attachment_id SERIAL PRIMARY KEY,
    phase_id            INTEGER REFERENCES phases ON DELETE CASCADE,
    attachment          VARCHAR(255) NOT NULL
);

CREATE TABLE roles
(
    role_id   SERIAL PRIMARY KEY,
    role_name VARCHAR(20) NOT NULL
);

-- Order matters: role_id 5 must be BUG HUNTER (API assigns role_id 5 on registration)
INSERT INTO roles (role_name) VALUES ('ADMIN');
INSERT INTO roles (role_name) VALUES ('PROJECT MANAGER');
INSERT INTO roles (role_name) VALUES ('DEVELOPER');
INSERT INTO roles (role_name) VALUES ('TESTER');
INSERT INTO roles (role_name) VALUES ('BUG HUNTER');

CREATE TABLE users
(
    user_id     SERIAL PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    gender      VARCHAR(10),
    dob         DATE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255),
    disabled_at TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    avatar      VARCHAR(255),
    experience  JSONB,
    type        BOOLEAN,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    bio         VARCHAR(50)
);

CREATE TABLE apply
(
    apply_id   SERIAL PRIMARY KEY,
    user_id    INTEGER REFERENCES users ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    apply_data JSONB
);

CREATE TABLE apply_post_recruitment
(
    apply_post_recruitment_id SERIAL PRIMARY KEY,
    post_recruitment_id       INTEGER,
    apply_id                  INTEGER REFERENCES apply ON DELETE CASCADE,
    created_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE otps
(
    otps_id    SERIAL PRIMARY KEY,
    code       VARCHAR(255) NOT NULL,
    issued_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiration TIMESTAMP,
    verify     BOOLEAN,
    user_id    INTEGER NOT NULL REFERENCES users ON DELETE CASCADE
);

CREATE TABLE rate_and_feedback
(
    rate_and_feedback_id SERIAL PRIMARY KEY,
    feedback             TEXT NOT NULL,
    rate_value           INTEGER,
    user_id              INTEGER REFERENCES users ON DELETE CASCADE,
    company_id           INTEGER REFERENCES company ON DELETE CASCADE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP,
    deleted_at           TIMESTAMP,
    type                 BOOLEAN DEFAULT TRUE,
    title                VARCHAR(100)
);

CREATE TABLE task
(
    task_id     SERIAL PRIMARY KEY,
    task_name   VARCHAR(100) NOT NULL,
    description TEXT,
    status      VARCHAR(20) NOT NULL,
    phase_id    INTEGER REFERENCES phases ON DELETE CASCADE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    due_date    DATE,
    attachment  VARCHAR,
    title       VARCHAR(100)
);

CREATE TABLE report
(
    report_id            SERIAL PRIMARY KEY,
    description          TEXT,
    location             VARCHAR(255) NOT NULL,
    problem              VARCHAR(255) NOT NULL,
    user_id              INTEGER REFERENCES users ON DELETE CASCADE,
    phase_id             INTEGER REFERENCES phases ON DELETE CASCADE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rate_and_feedback_id INTEGER REFERENCES rate_and_feedback
);

CREATE TABLE user_roles
(
    user_role_id SERIAL PRIMARY KEY,
    role_id      INTEGER REFERENCES roles ON DELETE CASCADE,
    user_id      INTEGER REFERENCES users ON DELETE CASCADE,
    company_id   INTEGER REFERENCES company ON DELETE CASCADE,
    task_id      INTEGER REFERENCES task ON DELETE CASCADE,
    phase_id     INTEGER REFERENCES phases ON DELETE CASCADE,
    project_id   INTEGER REFERENCES project ON DELETE CASCADE
);

CREATE TABLE notification
(
    notification_id SERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    user_id         INTEGER REFERENCES users ON DELETE CASCADE,
    user_role_id    INTEGER REFERENCES user_roles ON DELETE CASCADE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,
    status          VARCHAR(100) NOT NULL,
    is_read         BOOLEAN DEFAULT FALSE,
    company_id      INTEGER,
    project_id      INTEGER,
    phase_id        INTEGER,
    task_id         INTEGER,
    report_id       INTEGER,
    apply_id        INTEGER
);

CREATE TABLE task_submit
(
    task_submit_id SERIAL PRIMARY KEY,
    attachment     VARCHAR(255),
    create_at      TIMESTAMP,
    task_id        INTEGER REFERENCES task ON UPDATE CASCADE ON DELETE CASCADE,
    user_role_id   INTEGER REFERENCES user_roles ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE post_recruitment
(
    post_recruitment_id SERIAL PRIMARY KEY,
    post_data           JSONB,
    company_id          INTEGER REFERENCES company ON DELETE CASCADE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    deleted_at          TIMESTAMP,
    description         TEXT,
    title               VARCHAR(255) NOT NULL,
    fee                 VARCHAR NOT NULL,
    image               VARCHAR(255),
    application_title   VARCHAR
);

DO $$
    DECLARE
        r RECORD;
        col_name TEXT;
    BEGIN
        IF to_regclass('public.project') IS NULL THEN
            RAISE NOTICE 'Skip FK fix: public.project does not exist';
            RETURN;
        END IF;
        FOR r IN
            SELECT
                tc.table_schema,
                tc.table_name,
                tc.constraint_name,
                kcu.column_name
            FROM information_schema.table_constraints tc
                     JOIN information_schema.constraint_column_usage ccu
                          ON tc.constraint_schema = ccu.constraint_schema
                              AND tc.constraint_name = ccu.constraint_name
                     JOIN information_schema.key_column_usage kcu
                          ON tc.constraint_schema = kcu.constraint_schema
                              AND tc.constraint_name = kcu.constraint_name
                              AND tc.table_name = kcu.table_name
            WHERE tc.constraint_type = 'FOREIGN KEY'
              AND tc.table_schema = 'public'
              AND ccu.table_schema = 'public'
              AND ccu.table_name = 'projects'
            LOOP
                col_name := r.column_name;
                EXECUTE format(
                        'ALTER TABLE %I.%I DROP CONSTRAINT IF EXISTS %I',
                        r.table_schema, r.table_name, r.constraint_name
                        );
                EXECUTE format(
                        'ALTER TABLE %I.%I ADD CONSTRAINT %I FOREIGN KEY (%I) REFERENCES public.project (project_id) ON DELETE CASCADE',
                        r.table_schema,
                        r.table_name,
                        r.constraint_name,
                        col_name
                        );
                RAISE NOTICE 'Repointed FK % on %.%', r.constraint_name, r.table_name, col_name;
            END LOOP;
    END $$;


ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_project_id_fkey;
ALTER TABLE user_roles
    ADD CONSTRAINT user_roles_project_id_fkey
        FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE CASCADE;
ALTER TABLE phases DROP CONSTRAINT IF EXISTS phases_project_id_fkey;
ALTER TABLE phases
    ADD CONSTRAINT phases_project_id_fkey
        FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE CASCADE;

-- =============================================================================
-- Queue Migration — Add missing task.project_id
-- Fixes:
-- ERROR: column "project_id" of relation "task" does not exist
-- =============================================================================

-- Add missing column
ALTER TABLE task
    ADD COLUMN IF NOT EXISTS project_id INTEGER;

-- Drop old FK if exists
ALTER TABLE task
    DROP CONSTRAINT IF EXISTS task_project_id_fkey;

-- Recreate FK correctly
ALTER TABLE task
    ADD CONSTRAINT task_project_id_fkey
        FOREIGN KEY (project_id)
            REFERENCES project(project_id)
            ON DELETE CASCADE;

-- Optional index for performance
CREATE INDEX IF NOT EXISTS idx_task_project_id
    ON task(project_id);

-- Check if the task exists
SELECT * FROM task WHERE task_id = 1;

-- If not, insert it
INSERT INTO task (task_id, task_name, status, phase_id, project_id)
VALUES (1, 'Placeholder Task', 'OPEN', 1, 1); --

ALTER TABLE notification
    ALTER COLUMN task_id DROP NOT NULL;-- djust phase_id/project_id to match your schema

---------------------------
-- Drop old foreign key
ALTER TABLE notification
    DROP CONSTRAINT IF EXISTS notification_task_id_fkey;

-- Recreate foreign key using correct table
ALTER TABLE notification
    ADD CONSTRAINT notification_task_id_fkey
        FOREIGN KEY (task_id)
            REFERENCES task(task_id)
            ON DELETE CASCADE;


-- Remove wrong FK
ALTER TABLE user_roles
    DROP CONSTRAINT IF EXISTS user_roles_task_id_fkey;

-- Create correct FK
ALTER TABLE user_roles
    ADD CONSTRAINT user_roles_task_id_fkey
        FOREIGN KEY (task_id)
            REFERENCES task(task_id)
            ON DELETE SET NULL;
--=======================================================================


--=============================================================================

-- =========================
-- task_assignments
-- =========================
ALTER TABLE task_assignments
    DROP CONSTRAINT IF EXISTS task_assignments_task_id_fkey;

ALTER TABLE task_assignments
    ADD CONSTRAINT task_assignments_task_id_fkey
        FOREIGN KEY (task_id)
            REFERENCES task(task_id)
            ON DELETE CASCADE;

-- =========================
-- reports
-- =========================
ALTER TABLE reports
    DROP CONSTRAINT IF EXISTS reports_task_id_fkey;

ALTER TABLE reports
    ADD CONSTRAINT reports_task_id_fkey
        FOREIGN KEY (task_id)
            REFERENCES task(task_id)
            ON DELETE SET NULL;

-- =========================
-- rate_feedback
-- =========================
ALTER TABLE rate_feedback
    DROP CONSTRAINT IF EXISTS rate_feedback_task_id_fkey;

ALTER TABLE rate_feedback
    ADD CONSTRAINT rate_feedback_task_id_fkey
        FOREIGN KEY (task_id)
            REFERENCES task(task_id)
            ON DELETE CASCADE;
-- =========================================
-- FIX task_assignments FK
-- =========================================
ALTER TABLE task_assignments
    DROP CONSTRAINT IF EXISTS task_assignments_task_id_fkey;

ALTER TABLE task_assignments
    ADD CONSTRAINT task_assignments_task_id_fkey
        FOREIGN KEY (task_id)
            REFERENCES task(task_id);

-- =========================================
-- FIX reports FK
-- =========================================
ALTER TABLE reports
    DROP CONSTRAINT IF EXISTS reports_task_id_fkey;

ALTER TABLE reports
    ADD CONSTRAINT reports_task_id_fkey
        FOREIGN KEY (task_id)
            REFERENCES task(task_id);

-- =========================================
-- FIX rate_feedback FK
-- =========================================
ALTER TABLE rate_feedback
    DROP CONSTRAINT IF EXISTS rate_feedback_task_id_fkey;

ALTER TABLE rate_feedback
    ADD CONSTRAINT rate_feedback_task_id_fkey
        FOREIGN KEY (task_id)
            REFERENCES task(task_id);

