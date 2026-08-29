package com.example.personalassistant.config;

import com.mongodb.ConnectionString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Robust MongoDB Configuration.
 * Automatically sanitizes and URL-encodes special characters in MONGO_URI username/password
 * (such as '@', ':', '#', '$') to prevent Spring Boot's MongoAutoConfiguration from failing on Render.
 */
@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/ProjectLN}")
    private String rawMongoUri;

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoPropertiesCustomizer() {
        return builder -> {
            String sanitized = sanitizeMongoUri(rawMongoUri);
            try {
                builder.applyConnectionString(new ConnectionString(sanitized));
            } catch (Exception e) {
                log.warn("Invalid MONGO_URI provided. Operating with fallback: {}", e.getMessage());
                builder.applyConnectionString(new ConnectionString("mongodb://localhost:27017/ProjectLN"));
            }
        };
    }

    private String sanitizeMongoUri(String uri) {
        if (uri == null || uri.isBlank()) {
            return "mongodb://localhost:27017/ProjectLN";
        }
        try {
            int schemeEnd = uri.indexOf("://");
            if (schemeEnd == -1) return uri;

            int lastAt = uri.lastIndexOf("@");
            if (lastAt == -1 || lastAt < schemeEnd) {
                // No authentication credentials in URI
                return uri;
            }

            String scheme = uri.substring(0, schemeEnd + 3);
            String credentials = uri.substring(schemeEnd + 3, lastAt);
            String rest = uri.substring(lastAt + 1);

            int colonIndex = credentials.indexOf(":");
            if (colonIndex != -1) {
                String username = credentials.substring(0, colonIndex);
                String password = credentials.substring(colonIndex + 1);

                // URL-encode special characters if not already encoded
                String encodedUser = encodeIfNecessary(username);
                String encodedPass = encodeIfNecessary(password);

                return scheme + encodedUser + ":" + encodedPass + "@" + rest;
            }
        } catch (Exception e) {
            log.warn("Error sanitizing MONGO_URI: {}", e.getMessage());
        }
        return uri;
    }

    private String encodeIfNecessary(String value) {
        if (value == null) return "";
        if (value.contains("%")) {
            // Already URL encoded
            return value;
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
