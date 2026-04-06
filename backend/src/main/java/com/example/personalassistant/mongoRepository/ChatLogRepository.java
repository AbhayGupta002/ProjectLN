package com.example.personalassistant.mongoRepository;

import com.example.personalassistant.mongo.ChatLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatLogRepository extends MongoRepository<ChatLog, String> {

    @Query(value = "{}", sort = "{ createdAt: -1 }")
    List<ChatLog> findAllByOrderByCreatedAtDesc();
}