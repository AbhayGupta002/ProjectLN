package com.example.personalassistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for Security and Production Alignment.
 * Uses FRONTEND_URL environment variable to restrict cross-origin access in production
 * while supporting multi-port fallbacks for local development.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.frontend-url:http://localhost:3000,http://localhost:3001}")
    private String frontendUrl;

    private List<String> getAllowedOrigins() {
        if (frontendUrl == null || frontendUrl.trim().isEmpty() || "*".equals(frontendUrl.trim())) {
            return List.of("*");
        }
        return Arrays.stream(frontendUrl.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = getAllowedOrigins();
        var mapping = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");

        if (origins.contains("*")) {
            mapping.allowedOriginPatterns("*").allowCredentials(true);
        } else {
            mapping.allowedOrigins(origins.toArray(new String[0])).allowCredentials(true);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = getAllowedOrigins();

        if (origins.contains("*")) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(origins);
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}