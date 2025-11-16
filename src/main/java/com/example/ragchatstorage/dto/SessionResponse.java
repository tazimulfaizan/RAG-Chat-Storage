package com.example.ragchatstorage.dto;

import java.time.Instant;

/**
 * DTO for ChatSession response.
 * Mapped by ChatSessionMapper using MapStruct.
 */
public record SessionResponse(
    String id,
    String userId,
    String title,
    boolean favorite,
    Instant createdAt,
    Instant updatedAt
) {
}
