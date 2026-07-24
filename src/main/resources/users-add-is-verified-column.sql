-- ============================================================================
-- BUGZAPPER - FIXED DATABASE SCHEMA WITH ROLE-BASED ACCESS CONTROL
-- ============================================================================
-- This schema consolidates duplicate tables and implements proper access control
-- Last Updated: 2026-06-13
-- ============================================================================

-- Drop existing problematic tables if needed (backup data first!)
-- DROP TABLE IF EXISTS notification CASCADE;
-- DROP TABLE IF EXISTS user_roles CASCADE;
-- DROP TABLE IF EXISTS task_submit CASCADE;
-- ... etc

-- ============================================================================
-- CORE TABLES (Keep as-is)
-- ============================================================================

CREATE TABLE IF NOT EXISTS roles (
                                     role_id SERIAL PRIMARY KEY,
                                     role_name VARCHAR(50) NOT NULL UNIQUE,
                                     description TEXT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert standard roles
INSERT INTO roles (role_name, description) VALUES
                                               ('COMPANY_OWNER', 'Owner of company - full access'),
                                               ('PROJECT_MANAGER', 'Manages assigned projects and team members'),
                                               ('PHASE_LEAD', 'Leads specific project phases'),
                                               ('DEVELOPER', 'Team member working on assigned tasks'),
                                               ('RECRUITER', 'Manages recruitment posts')
ON CONFLICT (role_name) DO NOTHING;

CREATE TABLE IF NOT EXISTS users (
                                     user_id SERIAL PRIMARY KEY,
                                     first_name VARCHAR(100) NOT NULL,
                                     last_name VARCHAR(100) NOT NULL,
                                     gender VARCHAR(10),
                                     dob DATE,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     password VARCHAR(255),
                                     bio TEXT,
                                     avatar VARCHAR(255),
                                     avatar_key VARCHAR(255),
                                     is_verified BOOLEAN DEFAULT FALSE NOT NULL,
                                     experience JSONB,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP,
                                     deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS company (
                                       company_id SERIAL PRIMARY KEY,
                                       company_name VARCHAR(100) NOT NULL,
                                       description TEXT,
                                       profile_image VARCHAR(255),
                                       profile_image_key VARCHAR(255),
                                       cover_image VARCHAR(255),
                                       cover_image_key VARCHAR(255),
                                       invite_code CHAR(15) UNIQUE,
                                       invite_link CHAR(50) UNIQUE,
                                       email VARCHAR(255),
                                       phone VARCHAR(20),
                                       address VARCHAR(255),
                                       created_by INTEGER NOT NULL REFERENCES users(user_id),
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP,
                                       deleted_at TIMESTAMP
);

-- ============================================================================
-- COMPANY USER ROLES (replaces user_roles with better structure)
-- ============================================================================

CREATE TABLE IF NOT EXISTS company_user_roles (
                                                  company_user_role_id SERIAL PRIMARY KEY,
                                                  user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                                  company_id INTEGER NOT NULL REFERENCES company(company_id) ON DELETE CASCADE,
                                                  role_id INTEGER NOT NULL REFERENCES roles(role_id),
                                                  assigned_by INTEGER REFERENCES users(user_id),
                                                  is_active BOOLEAN DEFAULT TRUE,
                                                  assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  UNIQUE(user_id, company_id)
);

CREATE INDEX idx_company_user_roles_user_company ON company_user_roles(user_id, company_id);
CREATE INDEX idx_company_user_roles_role ON company_user_roles(role_id);

-- ============================================================================
-- PROJECTS (consolidated from 'projects' and 'project')
-- ============================================================================

CREATE TABLE IF NOT EXISTS projects (
                                        project_id SERIAL PRIMARY KEY,
                                        project_name VARCHAR(100) NOT NULL,
                                        description TEXT,
                                        company_id INTEGER NOT NULL REFERENCES company(company_id) ON DELETE CASCADE,
                                        created_by INTEGER NOT NULL REFERENCES users(user_id),
                                        is_active BOOLEAN DEFAULT TRUE,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP,
                                        deleted_at TIMESTAMP
);

CREATE INDEX idx_projects_company ON projects(company_id);
CREATE INDEX idx_projects_created_by ON projects(created_by);

-- ============================================================================
-- PROJECT MEMBERS & MANAGERS
-- ============================================================================

CREATE TABLE IF NOT EXISTS project_members (
                                               project_member_id SERIAL PRIMARY KEY,
                                               project_id INTEGER NOT NULL REFERENCES projects(project_id) ON DELETE CASCADE,
                                               user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                               role_id INTEGER NOT NULL REFERENCES roles(role_id),
                                               assigned_by INTEGER NOT NULL REFERENCES users(user_id),
                                               is_active BOOLEAN DEFAULT TRUE,
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               UNIQUE(project_id, user_id)
);

CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);
CREATE INDEX idx_project_members_role ON project_members(role_id);

-- ============================================================================
-- RECRUITMENT
-- ============================================================================

CREATE TABLE IF NOT EXISTS post_recruitment (
                                                post_recruitment_id SERIAL PRIMARY KEY,
                                                title VARCHAR(100) NOT NULL,
                                                description TEXT,
                                                fee VARCHAR(255),
                                                image VARCHAR(255),
                                                image_key VARCHAR(255),
                                                post_data JSONB,
                                                company_id INTEGER NOT NULL REFERENCES company(company_id),
                                                created_by INTEGER NOT NULL REFERENCES users(user_id),
                                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                updated_at TIMESTAMP,
                                                deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS apply (
                                     apply_id SERIAL PRIMARY KEY,
                                     user_id INTEGER NOT NULL REFERENCES users(user_id),
                                     file_attachment VARCHAR(255),
                                     file_key VARCHAR(255),
                                     apply_data JSONB,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS apply_post_recruitment (
                                                      apply_post_recruitment_id SERIAL PRIMARY KEY,
                                                      post_recruitment_id INTEGER NOT NULL REFERENCES post_recruitment(post_recruitment_id) ON DELETE CASCADE,
                                                      apply_id INTEGER NOT NULL REFERENCES apply(apply_id) ON DELETE CASCADE,
                                                      status VARCHAR(50) DEFAULT 'PENDING',
                                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                      UNIQUE(post_recruitment_id, apply_id)
);

-- ============================================================================
-- AUTHENTICATION
-- ============================================================================

CREATE TABLE IF NOT EXISTS otps (
                                    otp_id SERIAL PRIMARY KEY,
                                    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                    code VARCHAR(255) NOT NULL,
                                    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    expiration TIMESTAMP NOT NULL,
                                    is_verified BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_otps_user ON otps(user_id);

-- ============================================================================
-- PHASES (Project phases/milestones)
-- ============================================================================

CREATE TABLE IF NOT EXISTS phases (
                                      phase_id SERIAL PRIMARY KEY,
                                      phase_name VARCHAR(100) NOT NULL,
                                      description TEXT NOT NULL,
                                      image VARCHAR(255),
                                      image_key VARCHAR(255),
                                      project_id INTEGER NOT NULL REFERENCES projects(project_id) ON DELETE CASCADE,
                                      is_private BOOLEAN DEFAULT FALSE,
                                      price NUMERIC DEFAULT 0,
                                      link VARCHAR(255),
                                      created_by INTEGER NOT NULL REFERENCES users(user_id),
                                      is_active BOOLEAN DEFAULT TRUE,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP,
                                      deleted_at TIMESTAMP
);

CREATE INDEX idx_phases_project ON phases(project_id);

CREATE TABLE IF NOT EXISTS phase_members (
                                             phase_member_id SERIAL PRIMARY KEY,
                                             phase_id INTEGER NOT NULL REFERENCES phases(phase_id) ON DELETE CASCADE,
                                             user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                             role_id INTEGER NOT NULL REFERENCES roles(role_id),
                                             assigned_by INTEGER NOT NULL REFERENCES users(user_id),
                                             is_active BOOLEAN DEFAULT TRUE,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             UNIQUE(phase_id, user_id)
);

CREATE INDEX idx_phase_members_phase ON phase_members(phase_id);
CREATE INDEX idx_phase_members_user ON phase_members(user_id);

CREATE TABLE IF NOT EXISTS phase_attachments (
                                                 phase_attachment_id SERIAL PRIMARY KEY,
                                                 phase_id INTEGER NOT NULL REFERENCES phases(phase_id) ON DELETE CASCADE,
                                                 attachment VARCHAR(255) NOT NULL,
                                                 attachment_key VARCHAR(255),
                                                 uploaded_by INTEGER REFERENCES users(user_id),
                                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- TASKS (consolidated from 'tasks' and 'task')
-- ============================================================================

CREATE TABLE IF NOT EXISTS tasks (
                                     task_id SERIAL PRIMARY KEY,
                                     task_name VARCHAR(100) NOT NULL,
                                     title VARCHAR(100),
                                     description TEXT,
                                     status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, SUBMITTED, COMPLETED, REJECTED
                                     phase_id INTEGER NOT NULL REFERENCES phases(phase_id) ON DELETE CASCADE,
                                     project_id INTEGER NOT NULL REFERENCES projects(project_id) ON DELETE CASCADE,
                                     attachment VARCHAR(255),
                                     attachment_key VARCHAR(255),
                                     due_date DATE,
                                     created_by INTEGER NOT NULL REFERENCES users(user_id),
                                     is_active BOOLEAN DEFAULT TRUE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP,
                                     deleted_at TIMESTAMP
);

CREATE INDEX idx_tasks_phase ON tasks(phase_id);
CREATE INDEX idx_tasks_project ON tasks(project_id);

-- ============================================================================
-- TASK ASSIGNMENTS & SUBMISSIONS
-- ============================================================================

CREATE TABLE IF NOT EXISTS task_assignments (
                                                task_assignment_id SERIAL PRIMARY KEY,
                                                task_id INTEGER NOT NULL REFERENCES tasks(task_id) ON DELETE CASCADE,
                                                user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                                assigned_by INTEGER NOT NULL REFERENCES users(user_id),
                                                role_id INTEGER REFERENCES roles(role_id),
                                                status VARCHAR(50) DEFAULT 'ASSIGNED', -- ASSIGNED, IN_PROGRESS, SUBMITTED, APPROVED, REJECTED
                                                assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                UNIQUE(task_id, user_id)
);

CREATE INDEX idx_task_assignments_task ON task_assignments(task_id);
CREATE INDEX idx_task_assignments_user ON task_assignments(user_id);

CREATE TABLE IF NOT EXISTS task_submissions (
                                                task_submission_id SERIAL PRIMARY KEY,
                                                task_id INTEGER NOT NULL REFERENCES tasks(task_id) ON DELETE CASCADE,
                                                user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                                attachment VARCHAR(255),
                                                attachment_key VARCHAR(255),
                                                submission_status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, REVISION_NEEDED
                                                submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                reviewed_by INTEGER REFERENCES users(user_id),
                                                reviewed_at TIMESTAMP,
                                                notes TEXT
);

CREATE INDEX idx_task_submissions_task ON task_submissions(task_id);
CREATE INDEX idx_task_submissions_user ON task_submissions(user_id);

-- ============================================================================
-- REPORTS & FEEDBACK
-- ============================================================================

CREATE TABLE IF NOT EXISTS reports (
                                       report_id SERIAL PRIMARY KEY,
                                       user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                       phase_id INTEGER NOT NULL REFERENCES phases(phase_id) ON DELETE CASCADE,
                                       task_id INTEGER REFERENCES tasks(task_id) ON DELETE CASCADE,
                                       description TEXT NOT NULL,
                                       location VARCHAR(255),
                                       problem VARCHAR(255) NOT NULL,
                                       status VARCHAR(50) DEFAULT 'OPEN',
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reports_user ON reports(user_id);
CREATE INDEX idx_reports_phase ON reports(phase_id);

CREATE TABLE IF NOT EXISTS rate_and_feedback (
                                                 rate_and_feedback_id SERIAL PRIMARY KEY,
                                                 company_id INTEGER REFERENCES company(company_id) ON DELETE CASCADE,
                                                 user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                                                 task_id INTEGER REFERENCES tasks(task_id) ON DELETE CASCADE,
                                                 title VARCHAR(100),
                                                 feedback TEXT NOT NULL,
                                                 rate_value INTEGER,
                                                 type BOOLEAN DEFAULT TRUE, -- true=positive, false=negative
                                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at TIMESTAMP,
                                                 deleted_at TIMESTAMP
);

-- ============================================================================
-- NOTIFICATIONS
-- ============================================================================

CREATE TABLE IF NOT EXISTS notifications (
                                             notification_id SERIAL PRIMARY KEY,
                                             user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                             title VARCHAR(100) NOT NULL,
                                             description TEXT NOT NULL,
                                             status VARCHAR(50) DEFAULT 'ACTIVE',
                                             is_read BOOLEAN DEFAULT FALSE,
                                             redirect_id INTEGER,
                                             company_id INTEGER REFERENCES company(company_id) ON DELETE CASCADE,
                                             project_id INTEGER REFERENCES projects(project_id) ON DELETE CASCADE,
                                             phase_id INTEGER REFERENCES phases(phase_id) ON DELETE CASCADE,
                                             task_id INTEGER REFERENCES tasks(task_id) ON DELETE CASCADE,
                                             report_id INTEGER REFERENCES reports(report_id) ON DELETE CASCADE,
                                             apply_id INTEGER REFERENCES apply(apply_id) ON DELETE CASCADE,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             deleted_at TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_company ON notifications(company_id);

-- ============================================================================
-- ADD FINAL CONSTRAINTS
-- ============================================================================

-- Ensure project managers are in project_members
-- Ensure phase members are in phases
-- Ensure task assignments reference existing tasks

ALTER TABLE projects
    OWNER TO postgres;
ALTER TABLE project_members
    OWNER TO postgres;
ALTER TABLE phases
    OWNER TO postgres;
ALTER TABLE phase_members
    OWNER TO postgres;
ALTER TABLE tasks
    OWNER TO postgres;
ALTER TABLE task_assignments
    OWNER TO postgres;
ALTER TABLE task_submissions
    OWNER TO postgres;
ALTER TABLE company_user_roles
    OWNER TO postgres;
ALTER TABLE notifications
    OWNER TO postgres;
