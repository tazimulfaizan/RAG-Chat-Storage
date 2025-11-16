package com.example.ragchatstorage.service;

import com.example.ragchatstorage.dto.CreateMessageRequest;
import com.example.ragchatstorage.exception.NotFoundException;
import com.example.ragchatstorage.mapper.ChatMessageMapper;
import com.example.ragchatstorage.model.ChatMessage;
import com.example.ragchatstorage.model.ChatSession;
import com.example.ragchatstorage.model.ContextItem;
import com.example.ragchatstorage.repository.ChatMessageRepository;
import com.example.ragchatstorage.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatMessageMapper messageMapper;  // ✅ Added MapStruct mapper

    public ChatMessage addMessage(String sessionId, CreateMessageRequest request) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

        // ✅ Using MapStruct mapper for entity conversion
        ChatMessage message = messageMapper.toEntity(request);
        message.setSessionId(session.getId());
        message.setCreatedAt(Instant.now());

        // Convert context items using mapper
        if (request.context() != null) {
            List<ContextItem> contextItems = request.context().stream()
                    .map(messageMapper::toEntity)
                    .collect(Collectors.toList());
            message.setContext(contextItems);
        }

        return messageRepository.save(message);
    }

    public Page<ChatMessage> getMessages(String sessionId, int page, int size) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new NotFoundException("Session not found: " + sessionId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageable);
    }

    public void deleteMessagesForSession(String sessionId) {
        messageRepository.deleteBySessionId(sessionId);
    }
}

