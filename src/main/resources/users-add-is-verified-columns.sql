-- =========================
-- EXTENSION
-- =========================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       gender VARCHAR(10) NOT NULL,
                       dob DATE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255),
                       bio TEXT,

                       avatar VARCHAR(255),
                       avatar_key VARCHAR(255),

                       is_verify BOOLEAN DEFAULT FALSE,
                       experience JSONB,
                       type BOOLEAN NOT NULL,

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       deleted_at TIMESTAMP
);

-- =========================
-- ROLES
-- =========================
CREATE TABLE roles (
                       role_id SERIAL PRIMARY KEY,
                       role_name VARCHAR(50) NOT NULL UNIQUE
);

-- =========================
-- COMPANY
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
-- PROJECTS
-- =========================
CREATE TABLE projects (
                          project_id SERIAL PRIMARY KEY,
                          project_name VARCHAR(100) NOT NULL,
                          description VARCHAR(255),

                          company_id INT NOT NULL,
                          created_by INT,

                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          deleted_at TIMESTAMP,

                          FOREIGN KEY (company_id) REFERENCES company(company_id) ON DELETE CASCADE,
                          FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- =========================
-- PROJECT MEMBERS (ACCESS CONTROL)
-- =========================
CREATE TABLE project_members (
                                 id SERIAL PRIMARY KEY,
                                 project_id INT NOT NULL,
                                 user_id INT NOT NULL,
                                 role_id INT NOT NULL,
                                 assigned_by INT,

                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                 UNIQUE (project_id, user_id),

                                 FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
                                 FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                 FOREIGN KEY (role_id) REFERENCES roles(role_id),
                                 FOREIGN KEY (assigned_by) REFERENCES users(user_id)
);

-- =========================
-- PHASES
-- =========================
CREATE TABLE phases (
                        phase_id SERIAL PRIMARY KEY,
                        phase_name VARCHAR(100) NOT NULL,
                        description TEXT NOT NULL,

                        image VARCHAR(255),
                        image_key VARCHAR(255),

                        project_id INT NOT NULL,
                        is_private BOOLEAN NOT NULL,
                        price NUMERIC DEFAULT 0,
                        link VARCHAR(255),

                        created_by INT,

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP,
                        deleted_at TIMESTAMP,

                        FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
                        FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- =========================
-- PHASE MEMBERS
-- =========================
CREATE TABLE phase_members (
                               id SERIAL PRIMARY KEY,
                               phase_id INT NOT NULL,
                               user_id INT NOT NULL,
                               role_id INT,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               UNIQUE (phase_id, user_id),

                               FOREIGN KEY (phase_id) REFERENCES phases(phase_id) ON DELETE CASCADE,
                               FOREIGN KEY (user_id) REFERENCES users(user_id),
                               FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- =========================
-- TASKS
-- =========================
CREATE TABLE tasks (
                       task_id SERIAL PRIMARY KEY,
                       task_name VARCHAR(100) NOT NULL,
                       description TEXT,

                       phase_id INT NOT NULL,

                       attachment VARCHAR(255),
                       attachment_key VARCHAR(255),

                       due_date DATE,
                       status VARCHAR(100),

                       assigned_by INT,

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       deleted_at TIMESTAMP,

                       FOREIGN KEY (phase_id) REFERENCES phases(phase_id) ON DELETE CASCADE,
                       FOREIGN KEY (assigned_by) REFERENCES users(user_id)
);

-- =========================
-- TASK ASSIGNMENTS (MULTI-USER SUPPORT)
-- =========================
CREATE TABLE task_assignments (
                                  id SERIAL PRIMARY KEY,

                                  task_id INT NOT NULL,
                                  user_id INT NOT NULL,

                                  assigned_by INT,
                                  role_id INT,

                                  status VARCHAR(50) DEFAULT 'ASSIGNED',
                                  assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  UNIQUE (task_id, user_id),

                                  FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
                                  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                  FOREIGN KEY (assigned_by) REFERENCES users(user_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- =========================
-- REPORTS (BUG REPORT)
-- =========================
CREATE TABLE reports (
                         report_id SERIAL PRIMARY KEY,
                         description TEXT NOT NULL,
                         location VARCHAR(255) NOT NULL,
                         problem VARCHAR(255) NOT NULL,
                         status VARCHAR(50),

                         user_id INT NOT NULL,
                         phase_id INT NOT NULL,
                         task_id INT,

                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         FOREIGN KEY (user_id) REFERENCES users(user_id),
                         FOREIGN KEY (phase_id) REFERENCES phases(phase_id),
                         FOREIGN KEY (task_id) REFERENCES tasks(task_id)
);

-- =========================
-- NOTIFICATION
-- =========================
CREATE TABLE notification (
                              notification_id SERIAL PRIMARY KEY,
                              title VARCHAR(100) NOT NULL,
                              description TEXT NOT NULL,

                              user_id INT NOT NULL,
                              redirect_id INT,

                              company_id INT,
                              project_id INT,
                              phase_id INT,
                              task_id INT,
                              report_id INT,
                              apply_id INT,
                              user_role_id INT,

                              is_read BOOLEAN DEFAULT FALSE,
                              status VARCHAR(100),

                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              deleted_at TIMESTAMP,

                              FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =========================
-- RECRUITMENT
-- =========================
CREATE TABLE post_recruitment (
                                  post_recruitment_id SERIAL PRIMARY KEY,
                                  title VARCHAR(100) NOT NULL,
                                  description TEXT,
                                  fee VARCHAR(255),

                                  image VARCHAR(255),
                                  image_key VARCHAR(255),

                                  post_data JSONB,
                                  created_by INT,

                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP,
                                  deleted_at TIMESTAMP,

                                  FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- =========================
-- APPLY
-- =========================
CREATE TABLE apply (
                       apply_id SERIAL PRIMARY KEY,
                       file_attachment VARCHAR(255),
                       file_key VARCHAR(255),

                       user_id INT NOT NULL,
                       apply_data JSONB,

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE apply_post_recruitment (
                                        id SERIAL PRIMARY KEY,
                                        post_recruitment_id INT NOT NULL,
                                        apply_id INT NOT NULL,

                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                        FOREIGN KEY (post_recruitment_id) REFERENCES post_recruitment(post_recruitment_id),
                                        FOREIGN KEY (apply_id) REFERENCES apply(apply_id)
);

-- =========================
-- RATE & FEEDBACK
-- =========================
CREATE TABLE rate_feedback (
                               rate_feedback_id SERIAL PRIMARY KEY,
                               feedback TEXT,
                               rate_value FLOAT,

                               company_id INT,
                               user_id INT,
                               task_id INT,

                               type BOOLEAN NOT NULL,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP,
                               deleted_at TIMESTAMP,

                               FOREIGN KEY (company_id) REFERENCES company(company_id),
                               FOREIGN KEY (user_id) REFERENCES users(user_id),
                               FOREIGN KEY (task_id) REFERENCES tasks(task_id)
);

-- =========================
-- OTP
-- =========================
CREATE TABLE otps (
                      otp_id SERIAL PRIMARY KEY,
                      code VARCHAR(255) NOT NULL,
                      issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      expiration TIMESTAMP,
                      verify BOOLEAN DEFAULT FALSE,
                      user_id INT NOT NULL,

                      FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =========================
-- PHASE ATTACHMENT
-- =========================
CREATE TABLE phase_attachment (
                                  phase_attachment_id SERIAL PRIMARY KEY,
                                  attachment VARCHAR(255),
                                  attachment_key VARCHAR(255),

                                  phase_id INT,

                                  FOREIGN KEY (phase_id) REFERENCES phases(phase_id)
);
CREATE TABLE user_roles (
                            user_role_id SERIAL PRIMARY KEY,
                            user_id INT NOT NULL,
                            role_id INT NOT NULL,
                            company_id INT,
                            project_id INT,
                            phase_id INT,
                            task_id INT,
                            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(role_id),
                            FOREIGN KEY (company_id) REFERENCES company(company_id),
                            FOREIGN KEY (project_id) REFERENCES projects(project_id),
                            FOREIGN KEY (phase_id) REFERENCES phases(phase_id),
                            FOREIGN KEY (task_id) REFERENCES tasks(task_id)
);

INSERT INTO roles (role_name) VALUES
                                  ('COMPANY_ADMIN'),
                                  ('PROJECT_MANAGER'),
                                  ('DEVELOPER'),
                                  ('TESTER'),
                                  ('BUG_HUNTER');
-- =========================
-- INDEXES (PERFORMANCE)
-- =========================
CREATE INDEX idx_project_members_user ON project_members(user_id);
CREATE INDEX idx_tasks_phase ON tasks(phase_id);
CREATE INDEX idx_task_assignments_user ON task_assignments(user_id);
CREATE INDEX idx_reports_user ON reports(user_id);
CREATE INDEX idx_reports_phase ON reports(phase_id);