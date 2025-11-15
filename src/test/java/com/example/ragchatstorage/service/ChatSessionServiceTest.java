package com.example.ragchatstorage.service;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.exception.NotFoundException;
import com.example.ragchatstorage.model.ChatSession;
import com.example.ragchatstorage.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @InjectMocks
    private ChatSessionService chatSessionService;

    private ChatSession testSession;

    @BeforeEach
    void setUp() {
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
    void createSession_shouldCreateAndReturnSession() {
        // Given
        CreateSessionRequest request = new CreateSessionRequest();
        request.setUserId("user-123");
        request.setTitle("New Session");

        when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        ChatSession result = chatSessionService.createSession(request);

        // Then
        assertNotNull(result);
        verify(sessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    void getSessionsForUser_shouldReturnAllSessions() {
        // Given
        String userId = "user-123";
        List<ChatSession> sessions = List.of(testSession);
        when(sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(sessions);

        // When
        List<ChatSession> result = chatSessionService.getSessionsForUser(userId, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sessionRepository, times(1)).findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Test
    void getSessionsForUser_withFavoriteFilter_shouldReturnFilteredSessions() {
        // Given
        String userId = "user-123";
        List<ChatSession> sessions = List.of(testSession);
        when(sessionRepository.findByUserIdAndFavoriteOrderByUpdatedAtDesc(userId, true)).thenReturn(sessions);

        // When
        List<ChatSession> result = chatSessionService.getSessionsForUser(userId, true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sessionRepository, times(1)).findByUserIdAndFavoriteOrderByUpdatedAtDesc(userId, true);
    }

    @Test
    void getById_shouldReturnSession() {
        // Given
        String sessionId = "session-1";
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));

        // When
        ChatSession result = chatSessionService.getById(sessionId);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.getId());
        verify(sessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void getById_whenNotFound_shouldThrowNotFoundException() {
        // Given
        String sessionId = "non-existent";
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> chatSessionService.getById(sessionId));
        verify(sessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void renameSession_shouldUpdateTitle() {
        // Given
        String sessionId = "session-1";
        String newTitle = "Updated Title";
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        ChatSession result = chatSessionService.renameSession(sessionId, newTitle);

        // Then
        assertNotNull(result);
        verify(sessionRepository, times(1)).findById(sessionId);
        verify(sessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    void markFavorite_shouldUpdateFavoriteStatus() {
        // Given
        String sessionId = "session-1";
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        ChatSession result = chatSessionService.markFavorite(sessionId, true);

        // Then
        assertNotNull(result);
        verify(sessionRepository, times(1)).findById(sessionId);
        verify(sessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    void deleteSession_shouldDeleteSession() {
        // Given
        String sessionId = "session-1";
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        doNothing().when(sessionRepository).delete(any(ChatSession.class));

        // When
        chatSessionService.deleteSession(sessionId);

        // Then
        verify(sessionRepository, times(1)).findById(sessionId);
        verify(sessionRepository, times(1)).delete(any(ChatSession.class));
    }
}

