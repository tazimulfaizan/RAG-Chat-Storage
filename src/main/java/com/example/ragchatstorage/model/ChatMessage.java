package com.example.ragchatstorage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_session_id_created_at", columnList = "session_id, created_at ASC"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 50)
    private SenderType sender;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // Store context as JSON in MySQL (requires MySQL 5.7+)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", columnDefinition = "json")
    private List<ContextItem> context;

    @Column(name = "user_id", length = 255)
    private String userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
