package com.example.personalassistant.security;

import com.theokanning.openai.OpenAiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAiService openAiService() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your_api_key_here";
        }
        return new OpenAiService(apiKey, 80);
    }
}

