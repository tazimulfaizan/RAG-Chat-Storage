package com.example.ragchatstorage.service;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.mapper.ChatSessionMapper;
import com.example.ragchatstorage.model.ChatSession;
import com.example.ragchatstorage.repository.ChatSessionRepository;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {ChatSessionServiceCacheTest.TestConfig.class})
class ChatSessionServiceCacheTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private ChatSessionMapper sessionMapper;

    private ChatSessionService chatSessionService;
    private CacheManager cacheManager;
    private ChatSession testSession;

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        public CacheManager cacheManager() {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager("sessions", "userSessions");
            cacheManager.setCaffeine(Caffeine.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(10, TimeUnit.MINUTES));
            return cacheManager;
        }
    }

    @BeforeEach
    void setUp() {
        // Initialize cache manager from test config
        cacheManager = TestConfig.class.getAnnotation(Configuration.class) != null
            ? new TestConfig().cacheManager()
            : null;

        // Create service with mocks
        chatSessionService = new ChatSessionService(sessionRepository, sessionMapper);

        // Clear all caches before each test
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }

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
    void getById_shouldReturnSession() {
        // Given
        when(sessionRepository.findById("session-1")).thenReturn(Optional.of(testSession));

        // When
        ChatSession result = chatSessionService.getById("session-1");

        // Then
        assertNotNull(result);
        assertEquals("session-1", result.getId());
        verify(sessionRepository, times(1)).findById("session-1");
    }

    @Test
    void getSessionsForUser_shouldReturnSessions() {
        // Given
        List<ChatSession> sessions = List.of(testSession);
        when(sessionRepository.findByUserIdOrderByUpdatedAtDesc("user-123"))
                .thenReturn(sessions);

        // When
        List<ChatSession> result = chatSessionService.getSessionsForUser("user-123", null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sessionRepository, times(1)).findByUserIdOrderByUpdatedAtDesc("user-123");
    }

    @Test
    void renameSession_shouldUpdateTitle() {
        // Given
        when(sessionRepository.findById("session-1")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        ChatSession updated = chatSessionService.renameSession("session-1", "New Title");

        // Then
        assertNotNull(updated);
        verify(sessionRepository, times(1)).findById("session-1");
        verify(sessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    void createSession_shouldCreateNewSession() {
        // Given
        CreateSessionRequest request = new CreateSessionRequest("user-123", "New Session");

        when(sessionMapper.toEntity(any(CreateSessionRequest.class))).thenReturn(testSession);
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        ChatSession result = chatSessionService.createSession(request);

        // Then
        assertNotNull(result);
        verify(sessionMapper, times(1)).toEntity(any(CreateSessionRequest.class));
        verify(sessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    void deleteSession_shouldDeleteSession() {
        // Given
        when(sessionRepository.findById("session-1")).thenReturn(Optional.of(testSession));
        doNothing().when(sessionRepository).delete(any(ChatSession.class));

        // When
        chatSessionService.deleteSession("session-1");

        // Then
        verify(sessionRepository, times(1)).findById("session-1");
        verify(sessionRepository, times(1)).delete(any(ChatSession.class));
    }

    @Test
    void getSessionsForUser_withFavoriteFilter_shouldReturnFilteredSessions() {
        // Given
        when(sessionRepository.findByUserIdAndFavoriteOrderByUpdatedAtDesc("user-123", true))
                .thenReturn(List.of(testSession));

        // When
        List<ChatSession> result = chatSessionService.getSessionsForUser("user-123", true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sessionRepository, times(1))
                .findByUserIdAndFavoriteOrderByUpdatedAtDesc("user-123", true);
    }
}

