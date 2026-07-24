package com.api.bugzapper.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Aligns DB schema with the API: table {@code project} (singular) and FKs must not reference {@code projects}.
 */
@Slf4j
@Component
public class SchemaCompatibilityRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaCompatibilityRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureProjectTable();
            mergeProjectsIntoProjectIfBothExist();
            repointAllProjectsForeignKeys();
            dropOrphanProjectsTable();
        } catch (Exception e) {
            log.error(
                    "Schema compatibility failed: {}. Run fix-project-foreign-keys.sql in pgAdmin if needed.",
                    e.getMessage(),
                    e);
        }
    }

    private void ensureProjectTable() {
        if (tableExists("project")) {
            return;
        }

        log.warn("Table public.project is missing. Applying compatibility migration...");

        if (tableExists("projects")) {
            jdbcTemplate.execute("ALTER TABLE projects RENAME TO project");
            try {
                jdbcTemplate.execute("ALTER TABLE project ALTER COLUMN description TYPE TEXT");
            } catch (Exception ignored) {
                // column layout may differ
            }
            log.info("Renamed table projects -> project.");
        } else if (tableExists("company")) {
            jdbcTemplate.execute("""
                    CREATE TABLE project (
                        project_id   SERIAL PRIMARY KEY,
                        project_name VARCHAR(100) NOT NULL,
                        company_id   INTEGER REFERENCES company (company_id) ON DELETE CASCADE,
                        created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at   TIMESTAMP,
                        deleted_at   TIMESTAMP,
                        description  TEXT
                    )
                    """);
            log.info("Created table project.");
        } else {
            log.error(
                    "Cannot create project table: company table is also missing. "
                            + "Run docs/db/schema-queue-08-baseline.sql on your database.");
        }
    }

    private void mergeProjectsIntoProjectIfBothExist() {
        if (!tableExists("project") || !tableExists("projects")) {
            return;
        }

        log.warn("Both public.project and public.projects exist. Merging rows into project...");
        try {
            jdbcTemplate.execute("""
                    INSERT INTO project (project_id, project_name, description, company_id, created_at, updated_at, deleted_at)
                    SELECT p.project_id, p.project_name, p.description, p.company_id, p.created_at, p.updated_at, p.deleted_at
                    FROM projects p
                    WHERE NOT EXISTS (SELECT 1 FROM project pr WHERE pr.project_id = p.project_id)
                    """);
        } catch (Exception e) {
            log.warn("Could not merge projects -> project (columns may differ): {}", e.getMessage());
            try {
                jdbcTemplate.execute("""
                        INSERT INTO project (project_id, project_name, company_id, created_at, updated_at, deleted_at)
                        SELECT p.project_id, p.project_name, p.company_id, p.created_at, p.updated_at, p.deleted_at
                        FROM projects p
                        WHERE NOT EXISTS (SELECT 1 FROM project pr WHERE pr.project_id = p.project_id)
                        """);
            } catch (Exception e2) {
                log.warn("Merge without description failed: {}", e2.getMessage());
            }
        }
    }

    private void repointAllProjectsForeignKeys() {
        if (!tableExists("project")) {
            return;
        }

        List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList("""
                SELECT
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
                """);

        for (Map<String, Object> fk : foreignKeys) {
            String tableName = (String) fk.get("table_name");
            String constraintName = (String) fk.get("constraint_name");
            String columnName = (String) fk.get("column_name");

            jdbcTemplate.execute(
                    "ALTER TABLE \"" + tableName + "\" DROP CONSTRAINT IF EXISTS \"" + constraintName + "\"");
            jdbcTemplate.execute(
                    "ALTER TABLE \""
                            + tableName
                            + "\" ADD CONSTRAINT \""
                            + constraintName
                            + "\" FOREIGN KEY (\""
                            + columnName
                            + "\") REFERENCES project (project_id) ON DELETE CASCADE");

            log.info("Repointed FK {} on {}.{} -> project", constraintName, tableName, columnName);
        }

        if (!foreignKeys.isEmpty()) {
            log.info("Fixed {} foreign key(s) that referenced projects.", foreignKeys.size());
        }
    }

    private void dropOrphanProjectsTable() {
        if (!tableExists("projects") || !tableExists("project")) {
            return;
        }

        Long projectsRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM projects", Long.class);
        if (projectsRows != null && projectsRows > 0) {
            log.warn(
                    "Leaving public.projects in place ({} rows). After verifying data in project, drop it manually.",
                    projectsRows);
            return;
        }

        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS projects CASCADE");
            log.info("Dropped empty orphan table public.projects.");
        } catch (Exception e) {
            log.warn("Could not drop public.projects: {}", e.getMessage());
        }
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = ?
                )
                """,
                Boolean.class,
                tableName);
        return Boolean.TRUE.equals(exists);
    }
}
