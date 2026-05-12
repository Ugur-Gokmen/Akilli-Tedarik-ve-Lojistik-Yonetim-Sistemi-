package com.project.infrastructure.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaCompatibilityInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaCompatibilityInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @org.springframework.context.annotation.Bean
    public ApplicationRunner ensureUsersCompatibility() {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_enabled boolean");
                jdbcTemplate.execute("UPDATE users SET two_factor_enabled = false WHERE two_factor_enabled IS NULL");
                jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN two_factor_enabled SET DEFAULT false");
                jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN two_factor_enabled SET NOT NULL");
                log.info("Schema compatibility check passed for users.two_factor_enabled");
            } catch (Exception ex) {
                log.error("Schema compatibility check failed for users.two_factor_enabled", ex);
            }
        };
    }
}
