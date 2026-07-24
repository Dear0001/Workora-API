TRUNCATE TABLE
    public.task_assignments,
    public.task_submissions,
    public.task_submit,

    public.notifications,
    public.notification,

    public.reports,
    public.report,

    public.apply_post_recruitment,
    public.apply,

    public.otps,

    public.user_roles,
    public.company_user_roles,

    public.task,
    public.tasks,

    public.phases,
    public.phase_members,
    public.phase_attachment,
    public.phase_attachments,

    public.project,
    public.project_members,

    public.post_recruitment,

    public.company,
    public.users,

    public.roles
    RESTART IDENTITY CASCADE;
INSERT INTO roles (role_name, description) VALUES
                                               ('COMPANY_OWNER', 'Owner of company - full access'),
                                               ('PROJECT_MANAGER', 'Manages assigned projects and team members'),
                                               ('PHASE_LEAD', 'Leads specific project phases'),
                                               ('DEVELOPER', 'Team member working on assigned tasks'),
                                               ('RECRUITER', 'Manages recruitment posts')
ON CONFLICT (role_name) DO NOTHING;
-- =========================
-- 1. USERS & ROLES
-- =========================

CREATE TABLE roles (
                       role_id SERIAL PRIMARY KEY,
                       role_name VARCHAR(50) NOT NULL UNIQUE,
                       description TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       gender VARCHAR(10) NOT NULL,
                       dob DATE NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255),
                       bio TEXT,
                       avatar VARCHAR(255),
                       avatar_key VARCHAR(255),
                       is_verify BOOLEAN DEFAULT FALSE,
                       is_verified BOOLEAN DEFAULT FALSE,
                       experience JSONB,
                       type BOOLEAN NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       deleted_at TIMESTAMP
);

-- =========================
-- 2. COMPANY
-- =========================

CREATE TABLE company (
                         company_id SERIAL PRIMARY KEY,
                         company_name VARCHAR(100) NOT NULL,
                         description TEXT,
                         profile_image VARCHAR(255),
                         profile_image_key VARCHAR(255),
                         cover_image VARCHAR(255),
                         cover_image_key VARCHAR(255),
                         invite_code CHAR(15),
                         invite_link CHAR(50),
                         email VARCHAR(255),
                         phone VARCHAR(20),
                         address VARCHAR(255),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP,
                         deleted_at TIMESTAMP
);

-- =========================
-- 3. PROJECTS
-- =========================

CREATE TABLE projects (
                          project_id SERIAL PRIMARY KEY,
                          project_name VARCHAR(100) NOT NULL,
                          description TEXT,
                          company_id INT NOT NULL REFERENCES company ON DELETE CASCADE,
                          created_by INT NOT NULL REFERENCES users,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          deleted_at TIMESTAMP
);

-- =========================
-- 4. PHASES
-- =========================

CREATE TABLE phases (
                        phase_id SERIAL PRIMARY KEY,
                        phase_name VARCHAR(100) NOT NULL,
                        description TEXT NOT NULL,
                        image VARCHAR(255),
                        image_key VARCHAR(255),
                        project_id INT NOT NULL REFERENCES projects ON DELETE CASCADE,
                        is_private BOOLEAN NOT NULL,
                        price NUMERIC DEFAULT 0,
                        link VARCHAR(255),
                        created_by INT REFERENCES users,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP,
                        deleted_at TIMESTAMP
);

-- =========================
-- 5. TASKS (ONLY ONE TABLE USED)
-- =========================

CREATE TABLE tasks (
                       task_id SERIAL PRIMARY KEY,
                       task_name VARCHAR(100) NOT NULL,
                       description TEXT,
                       phase_id INT NOT NULL REFERENCES phases ON DELETE CASCADE,
                       project_id INT REFERENCES projects ON DELETE CASCADE,
                       attachment VARCHAR(255),
                       attachment_key VARCHAR(255),
                       due_date DATE,
                       status VARCHAR(100),
                       assigned_by INT REFERENCES users,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       deleted_at TIMESTAMP
);

-- =========================
-- 6. TASK ASSIGNMENTS
-- =========================

CREATE TABLE task_assignments (
                                  id SERIAL PRIMARY KEY,
                                  task_id INT NOT NULL REFERENCES tasks,
                                  user_id INT NOT NULL REFERENCES users ON DELETE CASCADE,
                                  assigned_by INT REFERENCES users,
                                  role_id INT REFERENCES roles,
                                  status VARCHAR(50) DEFAULT 'ASSIGNED',
                                  assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  UNIQUE(task_id, user_id)
);

-- =========================
-- 7. TASK SUBMISSIONS
-- =========================

CREATE TABLE task_submissions (
                                  task_submission_id SERIAL PRIMARY KEY,
                                  task_id INT NOT NULL REFERENCES tasks ON DELETE CASCADE,
                                  user_id INT NOT NULL REFERENCES users ON DELETE CASCADE,
                                  attachment VARCHAR(255),
                                  attachment_key VARCHAR(255),
                                  submission_status VARCHAR(50) DEFAULT 'PENDING',
                                  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  reviewed_by INT REFERENCES users,
                                  reviewed_at TIMESTAMP,
                                  notes TEXT
);

-- =========================
-- 8. APPLICATIONS
-- =========================

CREATE TABLE apply (
                       apply_id SERIAL PRIMARY KEY,
                       file_attachment VARCHAR(255),
                       file_key VARCHAR(255),
                       user_id INT NOT NULL REFERENCES users,
                       apply_data JSONB,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE apply_post_recruitment (
                                        id SERIAL PRIMARY KEY,
                                        post_recruitment_id INT REFERENCES post_recruitment,
                                        apply_id INT REFERENCES apply,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE post_recruitment (
                                  post_recruitment_id SERIAL PRIMARY KEY,
                                  title VARCHAR(100) NOT NULL,
                                  description TEXT,
                                  fee VARCHAR(255),
                                  image VARCHAR(255),
                                  image_key VARCHAR(255),
                                  post_data JSONB,
                                  created_by INT REFERENCES users,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP,
                                  deleted_at TIMESTAMP
);

-- =========================
-- 9. OTP
-- =========================

CREATE TABLE otps (
                      otp_id SERIAL PRIMARY KEY,
                      code VARCHAR(255) NOT NULL,
                      issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      expiration TIMESTAMP,
                      verify BOOLEAN DEFAULT FALSE,
                      user_id INT NOT NULL REFERENCES users
);

-- =========================
-- 10. NOTIFICATIONS (KEEP ONLY ONE VERSION)
-- =========================

CREATE TABLE notifications (
                               notification_id SERIAL PRIMARY KEY,
                               user_id INT NOT NULL REFERENCES users ON DELETE CASCADE,
                               title VARCHAR(100) NOT NULL,
                               description TEXT NOT NULL,
                               status VARCHAR(50) DEFAULT 'ACTIVE',
                               is_read BOOLEAN DEFAULT FALSE,
                               redirect_id INT,
                               company_id INT REFERENCES company ON DELETE CASCADE,
                               project_id INT REFERENCES projects ON DELETE CASCADE,
                               phase_id INT REFERENCES phases ON DELETE CASCADE,
                               task_id INT REFERENCES tasks ON DELETE CASCADE,
                               report_id INT REFERENCES reports ON DELETE CASCADE,
                               apply_id INT REFERENCES apply ON DELETE CASCADE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               deleted_at TIMESTAMP
);

-- =========================
-- 11. REPORTS
-- =========================

CREATE TABLE reports (
                         report_id SERIAL PRIMARY KEY,
                         description TEXT NOT NULL,
                         location VARCHAR(255) NOT NULL,
                         problem VARCHAR(255) NOT NULL,
                         status VARCHAR(50),
                         user_id INT NOT NULL REFERENCES users,
                         phase_id INT NOT NULL REFERENCES phases,
                         task_id INT REFERENCES tasks,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 12. USER ROLES
-- =========================

CREATE TABLE user_roles (
                            user_role_id SERIAL PRIMARY KEY,
                            user_id INT NOT NULL REFERENCES users ON DELETE CASCADE,
                            role_id INT REFERENCES roles,
                            company_id INT REFERENCES company,
                            project_id INT REFERENCES projects ON DELETE CASCADE,
                            phase_id INT REFERENCES phases,
                            task_id INT REFERENCES tasks ON DELETE SET NULL
);

-- =========================
-- 13. INDEXES
-- =========================

CREATE INDEX idx_tasks_phase ON tasks(phase_id);
CREATE INDEX idx_task_assignments_task ON task_assignments(task_id);
CREATE INDEX idx_task_assignments_user ON task_assignments(user_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_projects_company ON projects(company_id);
CREATE INDEX idx_phases_project ON phases(project_id);



-- =============================================================================
-- Bug Zapper — PostgreSQL schema v2: role model migration
-- =============================================================================
-- Supersedes docs/db/schema-queue-08-baseline.sql for the `roles` table content
-- and adds two small columns needed by the new recruitment-accept flow.
--
-- Role model (role_id is fixed and load-bearing — see AuthenticationController,
-- UserRoleRepository, PermissionService):
--   1 = COMPANY_OWNER   company-scoped, full access, granted on company creation
--   2 = PROJECT_MANAGER company-scoped, granted via invite or promotion
--   3 = PHASE_LEAD       company-scoped, granted via invite or phase assignment
--   4 = DEVELOPER        company-scoped, default role for open join-by-code
--   5 = RECRUITER        company-scoped, manages recruitment posts/applications
--   6 = BUG_HUNTER        global default assigned on registration (company_id IS NULL)
--
-- This script is DEV-ONLY: it truncates and reseeds `roles`, and adds the new
-- columns below if missing. It assumes the rest of the schema already matches
-- docs/db/schema-queue-08-baseline.sql (table names/shapes unchanged — this is
-- a role-model migration, not a schema restructuring). If you are bootstrapping
-- a brand-new database, run schema-queue-08-baseline.sql FIRST, then this file.
--
-- Safe to re-run (idempotent): re-running just re-truncates+reseeds roles and
-- no-ops the column additions.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1) Roles: replace the old ADMIN/PROJECT MANAGER/DEVELOPER/TESTER/BUG HUNTER
--    set with the new COMPANY_OWNER/PROJECT_MANAGER/PHASE_LEAD/DEVELOPER/
--    RECRUITER/BUG_HUNTER set, in the exact order that yields role_id 1..6.
-- ---------------------------------------------------------------------------

TRUNCATE TABLE roles RESTART IDENTITY CASCADE;

INSERT INTO roles (role_name) VALUES ('COMPANY_OWNER');    -- role_id 1
INSERT INTO roles (role_name) VALUES ('PROJECT_MANAGER');  -- role_id 2
INSERT INTO roles (role_name) VALUES ('PHASE_LEAD');       -- role_id 3
INSERT INTO roles (role_name) VALUES ('DEVELOPER');        -- role_id 4
INSERT INTO roles (role_name) VALUES ('RECRUITER');        -- role_id 5
INSERT INTO roles (role_name) VALUES ('BUG_HUNTER');       -- role_id 6

-- ---------------------------------------------------------------------------
-- 2) post_recruitment.role_id — which role an accepted applicant receives.
--    Nullable FK, defaults to DEVELOPER (4) at the application layer when
--    a post doesn't specify one (kept nullable here rather than NOT NULL
--    DEFAULT so existing rows don't need a backfill decision made in SQL).
-- ---------------------------------------------------------------------------

ALTER TABLE post_recruitment
    ADD COLUMN IF NOT EXISTS role_id INTEGER REFERENCES roles(role_id);

-- ---------------------------------------------------------------------------
-- 3) apply.status — application lifecycle for the accept/reject flow.
-- ---------------------------------------------------------------------------

ALTER TABLE apply
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- ---------------------------------------------------------------------------
-- 4) Verification queries (run manually, not part of migration)
-- ---------------------------------------------------------------------------
-- SELECT * FROM roles ORDER BY role_id;
-- SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'post_recruitment' AND column_name = 'role_id';
-- SELECT column_name, data_type, column_default FROM information_schema.columns WHERE table_name = 'apply' AND column_name = 'status';
ALTER TABLE post_recruitment
    ADD COLUMN IF NOT EXISTS company_id INTEGER REFERENCES company(company_id) ON DELETE CASCADE;

ALTER TABLE post_recruitment
    ADD COLUMN IF NOT EXISTS application_title VARCHAR;

ALTER TABLE post_recruitment
    ADD COLUMN IF NOT EXISTS role_id INTEGER REFERENCES roles(role_id);
