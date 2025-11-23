package com.example.ragchatstorage.repository;

import com.example.ragchatstorage.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId);

    List<ChatSession> findByUserIdAndFavoriteOrderByUpdatedAtDesc(String userId, boolean favorite);
}
