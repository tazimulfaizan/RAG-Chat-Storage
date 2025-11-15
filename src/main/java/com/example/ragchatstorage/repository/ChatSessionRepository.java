package com.example.ragchatstorage.repository;

import com.example.ragchatstorage.model.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {

    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId);

    List<ChatSession> findByUserIdAndFavoriteOrderByUpdatedAtDesc(String userId, boolean favorite);
}
