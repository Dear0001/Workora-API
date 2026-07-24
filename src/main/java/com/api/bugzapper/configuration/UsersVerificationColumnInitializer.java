package com.api.bugzapper.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures {@code users.is_verified} exists (PostgreSQL). OTP verification updates this column;
 * older DBs without it cause {@code BadSqlGrammarException}. Manual script:
 * {@code docs/db/users-add-is-verified-column.sql}
 */
@Component
@Order(1)
public class UsersVerificationColumnInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsersVerificationColumnInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public UsersVerificationColumnInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT false");
            log.info("Schema OK: users.is_verified column is present");
        } catch (Exception e) {
            log.warn(
                    "Could not add users.is_verified automatically. Apply docs/db/users-add-is-verified-column.sql to PostgreSQL. Reason: {}",
                    e.getMessage());
        }
    }
}
