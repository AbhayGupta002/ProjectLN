package com.example.personalassistant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Production Keep-Alive Service.
 * Periodically pings the Render backend health endpoint every 10 minutes while active
 * to prevent cloud container spin-down/cold-start delays on idle periods.
 */
@Service
public class BackendKeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(BackendKeepAliveService.class);

    @Value("${app.backend.url:https://world-tour-app-onc4.onrender.com}")
    private String backendUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Runs every 10 minutes (600,000 ms), with an initial delay of 2 minutes (120,000 ms)
    @Scheduled(fixedRate = 600000, initialDelay = 120000)
    public void keepAlivePing() {
        if (backendUrl == null || backendUrl.isBlank() || backendUrl.contains("localhost")) {
            return;
        }

        try {
            String pingUrl = backendUrl.replaceAll("/+$", "") + "/api/health";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pingUrl))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Keep-alive ping sent to {} - Status: {}", pingUrl, response.statusCode());
        } catch (Exception e) {
            log.debug("Keep-alive ping completed: {}", e.getMessage());
        }
    }
}
