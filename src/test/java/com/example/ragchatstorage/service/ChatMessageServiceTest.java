package com.example.ragchatstorage.service;

import com.example.ragchatstorage.dto.CreateMessageRequest;
import com.example.ragchatstorage.exception.NotFoundException;
import com.example.ragchatstorage.mapper.ChatMessageMapper;
import com.example.ragchatstorage.model.ChatMessage;
import com.example.ragchatstorage.model.ChatSession;
import com.example.ragchatstorage.model.SenderType;
import com.example.ragchatstorage.repository.ChatMessageRepository;
import com.example.ragchatstorage.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private ChatMessageMapper messageMapper;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private ChatSession testSession;
    private ChatMessage testMessage;

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

        testMessage = ChatMessage.builder()
                .id("message-1")
                .sessionId("session-1")
                .sender(SenderType.USER)
                .content("Hello")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void addMessage_shouldCreateAndReturnMessage() {
        // Given
        String sessionId = "session-1";
        // CreateMessageRequest is a record: CreateMessageRequest(SenderType sender, String content, List<ContextItemDto> context)
        CreateMessageRequest request = new CreateMessageRequest(SenderType.USER, "Test message", null);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(messageMapper.toEntity(any(CreateMessageRequest.class))).thenReturn(testMessage);
        when(messageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);

        // When
        ChatMessage result = chatMessageService.addMessage(sessionId, request);

        // Then
        assertNotNull(result);
        verify(sessionRepository, times(1)).findById(sessionId);
        verify(messageMapper, times(1)).toEntity(any(CreateMessageRequest.class));
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void addMessage_whenSessionNotFound_shouldThrowNotFoundException() {
        // Given
        String sessionId = "non-existent";
        CreateMessageRequest request = new CreateMessageRequest(SenderType.USER, "Test message", null);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> chatMessageService.addMessage(sessionId, request));
        verify(sessionRepository, times(1)).findById(sessionId);
        verify(messageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void getMessages_shouldReturnPagedMessages() {
        // Given
        String sessionId = "session-1";
        int page = 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messagePage = new PageImpl<>(List.of(testMessage));

        when(sessionRepository.existsById(sessionId)).thenReturn(true);
        when(messageRepository.findBySessionIdOrderByCreatedAtAsc(eq(sessionId), any(Pageable.class)))
                .thenReturn(messagePage);

        // When
        Page<ChatMessage> result = chatMessageService.getMessages(sessionId, page, size);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(sessionRepository, times(1)).existsById(sessionId);
        verify(messageRepository, times(1)).findBySessionIdOrderByCreatedAtAsc(eq(sessionId), any(Pageable.class));
    }

    @Test
    void getMessages_whenSessionNotFound_shouldThrowNotFoundException() {
        // Given
        String sessionId = "non-existent";
        when(sessionRepository.existsById(sessionId)).thenReturn(false);

        // When & Then
        assertThrows(NotFoundException.class, () -> chatMessageService.getMessages(sessionId, 0, 20));
        verify(sessionRepository, times(1)).existsById(sessionId);
        verify(messageRepository, never()).findBySessionIdOrderByCreatedAtAsc(any(), any());
    }

    @Test
    void deleteMessagesForSession_shouldDeleteMessages() {
        // Given
        String sessionId = "session-1";
        doNothing().when(messageRepository).deleteBySessionId(sessionId);

        // When
        chatMessageService.deleteMessagesForSession(sessionId);

        // Then
        verify(messageRepository, times(1)).deleteBySessionId(sessionId);
    }
}

