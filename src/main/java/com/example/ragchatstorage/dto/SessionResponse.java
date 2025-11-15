package com.example.ragchatstorage.dto;

import com.example.ragchatstorage.model.ChatSession;

import java.time.Instant;

public record SessionResponse(
    String id,
    String userId,
    String title,
    boolean favorite,
    Instant createdAt,
    Instant updatedAt
) {
    public static SessionResponse from(ChatSession session) {
        return new SessionResponse(
            session.getId(),
            session.getUserId(),
            session.getTitle(),
            session.isFavorite(),
            session.getCreatedAt(),
            session.getUpdatedAt()
        );
    }
}
