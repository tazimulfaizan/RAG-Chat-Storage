package com.example.ragchatstorage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "chat_sessions", indexes = {
    @Index(name = "idx_user_id_updated_at", columnList = "user_id, updated_at DESC"),
    @Index(name = "idx_user_id_favorite_updated_at", columnList = "user_id, favorite, updated_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "favorite", nullable = false)
    private boolean favorite;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
