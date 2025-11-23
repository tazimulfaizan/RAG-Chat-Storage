package com.example.ragchatstorage.service;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.exception.DatabaseException;
import com.example.ragchatstorage.exception.NotFoundException;
import com.example.ragchatstorage.mapper.ChatSessionMapper;
import com.example.ragchatstorage.model.ChatSession;
import com.example.ragchatstorage.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {

    private final ChatSessionRepository sessionRepository;
    private final ChatSessionMapper sessionMapper;

    @CacheEvict(value = "userSessions", key = "#request.userId")
    public ChatSession createSession(CreateSessionRequest request) {

        ChatSession session = sessionMapper.toEntity(request);
        session.setFavorite(false);

        Instant now = Instant.now();
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        ChatSession saved = sessionRepository.save(session);
        log.debug("Created session {} using MapStruct mapper and evicted userSessions cache for user {}",
                saved.getId(), request.userId());
        return saved;
    }

    @Cacheable(value = "userSessions", key = "#userId + '-' + (#favorite != null ? #favorite : 'all')")
    public List<ChatSession> getSessionsForUser(String userId, Boolean favorite) {
        log.debug("Fetching sessions from database for user: {}, favorite: {}", userId, favorite);
        if (favorite != null) {
            return sessionRepository.findByUserIdAndFavoriteOrderByUpdatedAtDesc(userId, favorite);
        }
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Cacheable(value = "sessions", key = "#id")
    public ChatSession getById(String id) {
        log.debug("Fetching session from database: {}", id);
        return sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Session not found: " + id));
    }

    @CachePut(value = "sessions", key = "#id")
    @CacheEvict(value = "userSessions", allEntries = true)
    public ChatSession renameSession(String id, String newTitle) {
        ChatSession session = getById(id);
        session.setTitle(newTitle);
        session.setUpdatedAt(Instant.now());
        ChatSession updated = sessionRepository.save(session);
        log.debug("Renamed session {} and updated cache", id);
        return updated;
    }

    @CachePut(value = "sessions", key = "#id")
    @CacheEvict(value = "userSessions", allEntries = true)
    public ChatSession markFavorite(String id, boolean favorite) {
        ChatSession session = getById(id);
        session.setFavorite(favorite);
        session.setUpdatedAt(Instant.now());
        ChatSession updated = sessionRepository.save(session);
        log.debug("Updated favorite status for session {} and refreshed cache", id);
        return updated;
    }

    @CacheEvict(value = {"sessions", "userSessions"}, allEntries = true)
    public void deleteSession(String id) {
        log.debug("[SERVICE] Deleting session: {}", id);

        try {
            ChatSession session = getById(id);
            sessionRepository.delete(session);

            log.info("[SERVICE] Session deleted. SessionId={}, Caches cleared", id);

        } catch (NotFoundException ex) {
            throw ex; // Re-throw NotFoundException
        } catch (org.springframework.dao.DataAccessException ex) {
            log.error("Database error deleting session. SessionId={}, Error={}",
                    id, ex.getMessage(), ex);
            throw new DatabaseException(
                    "Failed to delete session due to database error", ex);
        }
    }
}

