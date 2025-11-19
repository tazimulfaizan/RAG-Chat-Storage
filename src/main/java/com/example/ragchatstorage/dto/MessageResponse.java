package com.example.ragchatstorage.dto;

import com.example.ragchatstorage.model.ChatMessage;
import com.example.ragchatstorage.model.ContextItem;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record MessageResponse(
    String id,
    String sessionId,
    String sender,
    String content,
    String userId, // Added userId
    List<Map<String, Object>> context,
    Instant createdAt
) {
    public static MessageResponse from(ChatMessage message) {
        List<Map<String, Object>> ctx = null;
        if (message.getContext() != null) {
            ctx = message.getContext().stream()
                    .map(MessageResponse::toMap)
                    .toList();
        }

        return new MessageResponse(
            message.getId(),
            message.getSessionId(),
            message.getSender() != null ? message.getSender().name() : null,
            message.getContent(),
            message.getUserId(), // include userId
            ctx,
            message.getCreatedAt()
        );
    }

    private static Map<String, Object> toMap(ContextItem item) {
        return Map.of(
            "sourceId", item.getSourceId(),
            "snippet", item.getSnippet(),
            "metadata", item.getMetadata()
        );
    }
}
