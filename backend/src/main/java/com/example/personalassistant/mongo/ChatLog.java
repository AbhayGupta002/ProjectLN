package com.example.personalassistant.mongo;

import jakarta.persistence.Column;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_chat_logs")
public class ChatLog {

    @Id
    private String id;

    private String sessionId;
    private String email;

    private String userPrompt;
    private String aiResponse;

    private String intent;

    private LocalDateTime createdAt;
}