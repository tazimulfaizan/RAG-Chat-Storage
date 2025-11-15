package com.example.ragchatstorage.service;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.model.ChatSession;
import com.example.ragchatstorage.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ChatSessionService.class})
@TestPropertySource(properties = {
        "spring.cache.type=caffeine"
})
class ChatSessionServiceCacheTest {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private ChatSessionRepository sessionRepository;

    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName ->
                cacheManager.getCache(cacheName).clear());

        testSession = ChatSession.builder()
                .id("session-1")
                .userId("user-123")
                .title("Test Session")
                .favorite(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getById_shouldCacheResult() {
        // Given
        when(sessionRepository.findById("session-1")).thenReturn(Optional.of(testSession));

        // When - First call
        ChatSession result1 = chatSessionService.getById("session-1");

        // When - Second call (should hit cache)
        ChatSession result2 = chatSessionService.getById("session-1");

        // Then - Repository called only once
        verify(sessionRepository, times(1)).findById("session-1");
        assertEquals(result1.getId(), result2.getId());
    }

    @Test
    void getSessionsForUser_shouldCacheResult() {
        // Given
        List<ChatSession> sessions = List.of(testSession);
        when(sessionRepository.findByUserIdOrderByUpdatedAtDesc("user-123"))
                .thenReturn(sessions);

        // When - First call
        List<ChatSession> result1 = chatSessionService.getSessionsForUser("user-123", null);

        // When - Second call (should hit cache)
        List<ChatSession> result2 = chatSessionService.getSessionsForUser("user-123", null);

        // Then - Repository called only once
        verify(sessionRepository, times(1)).findByUserIdOrderByUpdatedAtDesc("user-123");
        assertEquals(result1.size(), result2.size());
    }

    @Test
    void renameSession_shouldUpdateCache() {
        // Given
        when(sessionRepository.findById("session-1")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When - Get session (cache it)
        chatSessionService.getById("session-1");

        // When - Rename session (should update cache)
        ChatSession updated = chatSessionService.renameSession("session-1", "New Title");

        // Then - Cache should contain updated session
        assertNotNull(cacheManager.getCache("sessions").get("session-1"));
        verify(sessionRepository, times(1)).findById("session-1");
    }

    @Test
    void createSession_shouldEvictUserSessionsCache() {
        // Given
        CreateSessionRequest request = new CreateSessionRequest();
        request.setUserId("user-123");
        request.setTitle("New Session");

        when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
        when(sessionRepository.findByUserIdOrderByUpdatedAtDesc("user-123"))
                .thenReturn(List.of(testSession));

        // When - Cache user sessions
        chatSessionService.getSessionsForUser("user-123", null);

        // When - Create new session (should evict cache)
        chatSessionService.createSession(request);

        // Then - Next call should hit repository again
        chatSessionService.getSessionsForUser("user-123", null);
        verify(sessionRepository, times(2)).findByUserIdOrderByUpdatedAtDesc("user-123");
    }

    @Test
    void deleteSession_shouldClearCaches() {
        // Given
        when(sessionRepository.findById("session-1")).thenReturn(Optional.of(testSession));
        doNothing().when(sessionRepository).delete(any(ChatSession.class));

        // When - Cache session
        chatSessionService.getById("session-1");

        // When - Delete session (should clear cache)
        chatSessionService.deleteSession("session-1");

        // Then - Cache should be empty
        assertNull(cacheManager.getCache("sessions").get("session-1"));
    }

    @Test
    void getSessionsForUser_withDifferentFilters_shouldUseDifferentCacheKeys() {
        // Given
        when(sessionRepository.findByUserIdOrderByUpdatedAtDesc("user-123"))
                .thenReturn(List.of(testSession));
        when(sessionRepository.findByUserIdAndFavoriteOrderByUpdatedAtDesc("user-123", true))
                .thenReturn(List.of(testSession));

        // When - Call with null favorite
        chatSessionService.getSessionsForUser("user-123", null);

        // When - Call with favorite=true
        chatSessionService.getSessionsForUser("user-123", true);

        // Then - Both should hit repository (different cache keys)
        verify(sessionRepository, times(1)).findByUserIdOrderByUpdatedAtDesc("user-123");
        verify(sessionRepository, times(1))
                .findByUserIdAndFavoriteOrderByUpdatedAtDesc("user-123", true);
    }
}

