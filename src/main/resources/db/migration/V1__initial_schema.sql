-- Initial schema for RAG Chat Storage
-- MySQL 8.0+ with JSON support

-- Chat Sessions Table
CREATE TABLE IF NOT EXISTS chat_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(500),
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    INDEX idx_user_id_updated_at (user_id, updated_at DESC),
    INDEX idx_user_id_favorite_updated_at (user_id, favorite, updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chat Messages Table
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    sender VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    context JSON,
    user_id VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    INDEX idx_session_id_created_at (session_id, created_at ASC),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

