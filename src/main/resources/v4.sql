-- PostgreSQL schema script for BugZapper
-- Compatible with PostgreSQL 12+

-- This script is intentionally split into standalone statements so PostgreSQL
-- reports the exact failing statement instead of aborting the whole transaction.

DROP TABLE IF EXISTS public.task_submissions CASCADE;
DROP TABLE IF EXISTS public.task_submit CASCADE;
DROP TABLE IF EXISTS public.task_assignments CASCADE;
DROP TABLE IF EXISTS public.task CASCADE;
DROP TABLE IF EXISTS public.tasks CASCADE;
DROP TABLE IF EXISTS public.phase_attachments CASCADE;
DROP TABLE IF EXISTS public.phase_members CASCADE;
DROP TABLE IF EXISTS public.phases CASCADE;
DROP TABLE IF EXISTS public.project_members CASCADE;
DROP TABLE IF EXISTS public.project CASCADE;
DROP TABLE IF EXISTS public.projects CASCADE;
DROP TABLE IF EXISTS public.apply_post_recruitment CASCADE;
DROP TABLE IF EXISTS public.post_recruitment CASCADE;
DROP TABLE IF EXISTS public.apply CASCADE;
DROP TABLE IF EXISTS public.notification CASCADE;
DROP TABLE IF EXISTS public.notifications CASCADE;
DROP TABLE IF EXISTS public.report CASCADE;
DROP TABLE IF EXISTS public.reports CASCADE;
DROP TABLE IF EXISTS public.rate_feedback CASCADE;
DROP TABLE IF EXISTS public.rate_and_feedback CASCADE;
DROP TABLE IF EXISTS public.company_user_roles CASCADE;
DROP TABLE IF EXISTS public.user_roles CASCADE;
DROP TABLE IF EXISTS public.otps CASCADE;
DROP TABLE IF EXISTS public.company CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;
DROP TABLE IF EXISTS public.roles CASCADE;

CREATE TABLE public.roles (
                              role_id SERIAL PRIMARY KEY,
                              role_name VARCHAR(50) NOT NULL UNIQUE,
                              description TEXT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.users (
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
                              is_verified BOOLEAN DEFAULT FALSE NOT NULL,
                              experience JSONB,
                              type BOOLEAN NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP,
                              deleted_at TIMESTAMP
);

CREATE TABLE public.company (
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

CREATE TABLE public.apply (
                              apply_id SERIAL PRIMARY KEY,
                              file_attachment VARCHAR(255),
                              file_key VARCHAR(255),
                              user_id INT NOT NULL REFERENCES public.users(user_id),
                              apply_data JSONB,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              status VARCHAR(50) DEFAULT 'PENDING' NOT NULL
);

CREATE TABLE public.otps (
                             otp_id SERIAL PRIMARY KEY,
                             code VARCHAR(255) NOT NULL,
                             issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             expiration TIMESTAMP,
                             verify BOOLEAN DEFAULT FALSE,
                             user_id INT NOT NULL REFERENCES public.users(user_id)
);

CREATE INDEX idx_otps_user ON public.otps(user_id);

CREATE TABLE public.project (
                                project_id SERIAL PRIMARY KEY,
                                project_name VARCHAR(100) NOT NULL,
                                company_id INT REFERENCES public.company(company_id) ON DELETE CASCADE,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP,
                                deleted_at TIMESTAMP,
                                description TEXT
);

CREATE TABLE public.project_members (
                                        id SERIAL PRIMARY KEY,
                                        project_id INT NOT NULL REFERENCES public.project(project_id) ON DELETE CASCADE,
                                        user_id INT NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
                                        role_id INT NOT NULL REFERENCES public.roles(role_id),
                                        assigned_by INT REFERENCES public.users(user_id),
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        UNIQUE(project_id, user_id)
);

CREATE INDEX idx_project_members_user ON public.project_members(user_id);
CREATE INDEX idx_project_members_project ON public.project_members(project_id);
CREATE INDEX idx_project_members_role ON public.project_members(role_id);

CREATE TABLE public.phases (
                               phase_id SERIAL PRIMARY KEY,
                               phase_name VARCHAR(100) NOT NULL,
                               description TEXT NOT NULL,
                               image VARCHAR(255),
                               image_key VARCHAR(255),
                               project_id INT NOT NULL REFERENCES public.project(project_id) ON DELETE CASCADE,
                               is_private BOOLEAN NOT NULL,
                               price NUMERIC DEFAULT 0,
                               link VARCHAR(255),
                               created_by INT REFERENCES public.users(user_id),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP,
                               deleted_at TIMESTAMP,
                               status VARCHAR(20) DEFAULT 'OPEN' NOT NULL
);

CREATE INDEX idx_phases_project ON public.phases(project_id);

CREATE TABLE public.phase_members (
                                      id SERIAL PRIMARY KEY,
                                      phase_id INT NOT NULL REFERENCES public.phases(phase_id) ON DELETE CASCADE,
                                      user_id INT NOT NULL REFERENCES public.users(user_id),
                                      role_id INT REFERENCES public.roles(role_id),
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      UNIQUE(phase_id, user_id)
);

CREATE INDEX idx_phase_members_phase ON public.phase_members(phase_id);
CREATE INDEX idx_phase_members_user ON public.phase_members(user_id);

CREATE TABLE public.post_recruitment (
                                         post_recruitment_id SERIAL PRIMARY KEY,
                                         title VARCHAR(100) NOT NULL,
                                         description TEXT,
                                         fee VARCHAR(255),
                                         image VARCHAR(255),
                                         image_key VARCHAR(255),
                                         post_data JSONB,
                                         created_by INT REFERENCES public.users(user_id),
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP,
                                         deleted_at TIMESTAMP,
                                         role_id INT REFERENCES public.roles(role_id),
                                         company_id INT REFERENCES public.company(company_id) ON DELETE CASCADE,
                                         application_title VARCHAR(255),
                                         status VARCHAR(20) DEFAULT 'OPEN' NOT NULL,
                                         project_id INT REFERENCES public.project(project_id)
);

CREATE TABLE public.apply_post_recruitment (
                                               id SERIAL PRIMARY KEY,
                                               post_recruitment_id INT NOT NULL REFERENCES public.post_recruitment(post_recruitment_id),
                                               apply_id INT NOT NULL REFERENCES public.apply(apply_id),
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.rate_and_feedback (
                                          rate_and_feedback_id SERIAL PRIMARY KEY,
                                          feedback TEXT NOT NULL,
                                          rate_value INT,
                                          user_id INT REFERENCES public.users(user_id) ON DELETE CASCADE,
                                          company_id INT REFERENCES public.company(company_id) ON DELETE CASCADE,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP,
                                          deleted_at TIMESTAMP,
                                          type BOOLEAN DEFAULT TRUE,
                                          title VARCHAR(100)
);

CREATE TABLE public.task (
                             task_id SERIAL PRIMARY KEY,
                             task_name VARCHAR(100) NOT NULL,
                             description TEXT,
                             status VARCHAR(20) NOT NULL,
                             phase_id INT REFERENCES public.phases(phase_id) ON DELETE CASCADE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP,
                             deleted_at TIMESTAMP,
                             due_date DATE,
                             attachment VARCHAR(255),
                             title VARCHAR(100),
                             project_id INT REFERENCES public.project(project_id) ON DELETE CASCADE
);

CREATE TABLE public.task_assignments (
                                         id SERIAL PRIMARY KEY,
                                         task_id INT NOT NULL REFERENCES public.task(task_id),
                                         user_id INT NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
                                         assigned_by INT REFERENCES public.users(user_id),
                                         role_id INT REFERENCES public.roles(role_id),
                                         status VARCHAR(50) DEFAULT 'ASSIGNED',
                                         assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         UNIQUE(task_id, user_id)
);

CREATE INDEX idx_task_assignments_user ON public.task_assignments(user_id);
CREATE INDEX idx_task_assignments_task ON public.task_assignments(task_id);

CREATE TABLE public.reports (
                                report_id SERIAL PRIMARY KEY,
                                description TEXT NOT NULL,
                                location VARCHAR(255),
                                problem VARCHAR(255) NOT NULL,
                                status VARCHAR(50),
                                user_id INT NOT NULL REFERENCES public.users(user_id),
                                phase_id INT NOT NULL REFERENCES public.phases(phase_id),
                                task_id INT REFERENCES public.task(task_id),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                image TEXT
);

CREATE INDEX idx_reports_user ON public.reports(user_id);
CREATE INDEX idx_reports_phase ON public.reports(phase_id);

CREATE TABLE public.rate_feedback (
                                      rate_feedback_id SERIAL PRIMARY KEY,
                                      feedback TEXT,
                                      rate_value DOUBLE PRECISION,
                                      company_id INT REFERENCES public.company(company_id),
                                      user_id INT REFERENCES public.users(user_id),
                                      task_id INT REFERENCES public.task(task_id),
                                      type BOOLEAN NOT NULL,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP,
                                      deleted_at TIMESTAMP
);

CREATE TABLE public.user_roles (
                                   user_role_id SERIAL PRIMARY KEY,
                                   user_id INT NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
                                   role_id INT NOT NULL REFERENCES public.roles(role_id),
                                   company_id INT REFERENCES public.company(company_id),
                                   project_id INT REFERENCES public.project(project_id) ON DELETE CASCADE,
                                   phase_id INT REFERENCES public.phases(phase_id),
                                   task_id INT REFERENCES public.task(task_id) ON DELETE SET NULL
);

CREATE TABLE public.task_submit (
                                    task_submit_id SERIAL PRIMARY KEY,
                                    attachment VARCHAR(255),
                                    create_at TIMESTAMP,
                                    task_id INT REFERENCES public.task(task_id) ON UPDATE CASCADE ON DELETE CASCADE,
                                    user_role_id INT NULL
);

CREATE TABLE public.company_user_roles (
                                           company_user_role_id SERIAL PRIMARY KEY,
                                           user_id INT NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
                                           company_id INT NOT NULL REFERENCES public.company(company_id) ON DELETE CASCADE,
                                           role_id INT NOT NULL REFERENCES public.roles(role_id),
                                           assigned_by INT REFERENCES public.users(user_id),
                                           is_active BOOLEAN DEFAULT TRUE,
                                           assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           UNIQUE(user_id, company_id)
);

CREATE INDEX idx_company_user_roles_user_company ON public.company_user_roles(user_id, company_id);
CREATE INDEX idx_company_user_roles_role ON public.company_user_roles(role_id);

CREATE TABLE public.phase_attachments (
                                          phase_attachment_id SERIAL PRIMARY KEY,
                                          phase_id INT NOT NULL REFERENCES public.phases(phase_id) ON DELETE CASCADE,
                                          attachment VARCHAR(255) NOT NULL,
                                          attachment_key VARCHAR(255),
                                          uploaded_by INT REFERENCES public.users(user_id),
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.task_submissions (
                                         task_submission_id SERIAL PRIMARY KEY,
                                         task_id INT NOT NULL REFERENCES public.task(task_id) ON DELETE CASCADE,
                                         user_id INT NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
                                         attachment VARCHAR(255),
                                         attachment_key VARCHAR(255),
                                         submission_status VARCHAR(50) DEFAULT 'PENDING',
                                         submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         reviewed_by INT REFERENCES public.users(user_id),
                                         reviewed_at TIMESTAMP,
                                         notes TEXT
);

CREATE INDEX idx_task_submissions_task ON public.task_submissions(task_id);
CREATE INDEX idx_task_submissions_user ON public.task_submissions(user_id);

CREATE TABLE public.notifications (
                                      notification_id SERIAL PRIMARY KEY,
                                      user_id INT NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
                                      title VARCHAR(100) NOT NULL,
                                      description TEXT NOT NULL,
                                      status VARCHAR(50) DEFAULT 'ACTIVE',
                                      is_read BOOLEAN DEFAULT FALSE,
                                      redirect_id INT,
                                      company_id INT REFERENCES public.company(company_id) ON DELETE CASCADE,
                                      project_id INT REFERENCES public.project(project_id) ON DELETE CASCADE,
                                      phase_id INT REFERENCES public.phases(phase_id) ON DELETE CASCADE,
                                      task_id INT REFERENCES public.task(task_id) ON DELETE CASCADE,
                                      report_id INT REFERENCES public.reports(report_id) ON DELETE CASCADE,
                                      apply_id INT REFERENCES public.apply(apply_id) ON DELETE CASCADE,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      deleted_at TIMESTAMP
);

CREATE INDEX idx_notifications_user ON public.notifications(user_id);
CREATE INDEX idx_notifications_company ON public.notifications(company_id);

INSERT INTO public.roles (role_name, description) VALUES
                                                      ('COMPANY_OWNER', 'Owner of company - full access'),
                                                      ('PROJECT_MANAGER', 'Manages assigned projects and team members'),
                                                      ('PHASE_LEAD', 'Leads specific project phases'),
                                                      ('DEVELOPER', 'Team member working on assigned tasks'),
                                                      ('RECRUITER', 'Manages recruitment posts'),
                                                      ('BUG_HUNTER', 'Default role for newly registered users')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO public.roles (role_name, description)
VALUES ('BUG_HUNTER', 'Default role for newly registered users')
ON CONFLICT (role_name) DO NOTHING;
