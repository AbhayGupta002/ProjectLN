package com.example.personalassistant.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates mandatory production environment secrets upon Spring application startup.
 * Ensures startup fails clearly when critical secrets (JWT_SECRET, DB_URL, DB_USERNAME, DB_PASSWORD)
 * are missing or insecure in the production environment.
 */
@Configuration
public class ProductionConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(ProductionConfigValidator.class);

    private final Environment environment;

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    public ProductionConfigValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateProductionSecrets() {
        boolean isProd = false;
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                isProd = true;
                break;
            }
        }

        if (!isProd) {
            log.info("Non-production profile active. Skipping strict mandatory production secret enforcement.");
            return;
        }

        log.info("Enforcing mandatory production environment secret validations...");

        List<String> missingSecrets = new ArrayList<>();

        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
            missingSecrets.add("JWT_SECRET (must be non-empty and at least 32 characters / 256 bits)");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            missingSecrets.add("DB_URL");
        }
        if (dbUsername == null || dbUsername.isBlank()) {
            missingSecrets.add("DB_USERNAME");
        }
        if (dbPassword == null || dbPassword.isBlank()) {
            missingSecrets.add("DB_PASSWORD");
        }

        if (!missingSecrets.isEmpty()) {
            String errorMsg = "\n======================================================================\n"
                    + "   CRITICAL PRODUCTION CONFIGURATION ERROR\n"
                    + "   Mandatory environment variable(s) missing or insecure:\n"
                    + "   " + missingSecrets + "\n"
                    + "   Please provide secure environment variables before starting in production!\n"
                    + "======================================================================\n";
            log.error(errorMsg);
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: Mandatory production secrets missing: " + missingSecrets);
        }

        log.info("Production configuration validation PASSED. All mandatory secrets are present.");
    }
}
