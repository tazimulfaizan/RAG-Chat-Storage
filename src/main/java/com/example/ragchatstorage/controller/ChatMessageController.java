package com.example.ragchatstorage.controller;

import com.example.ragchatstorage.dto.CreateMessageRequest;
import com.example.ragchatstorage.dto.MessageResponse;
import com.example.ragchatstorage.dto.PagedResponse;
import com.example.ragchatstorage.mapper.ChatMessageMapper;
import com.example.ragchatstorage.model.ChatMessage;
import com.example.ragchatstorage.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
@Tag(name = "Chat Messages", description = "APIs for managing chat messages within sessions")
@SecurityRequirement(name = "ApiKeyAuth")
public class ChatMessageController {

    private final ChatMessageService messageService;
    private final ChatMessageMapper messageMapper;

    @Value("${app.pagination.default-page-size:20}")
    private int defaultPageSize;

    @Value("${app.pagination.max-page-size:100}")
    private int maxPageSize;

    @PostMapping
    @Operation(summary = "Add a message to a session", description = "Creates a new message within a chat session with optional RAG context")
    public ResponseEntity<MessageResponse> addMessage(
            @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
            @Valid @RequestBody CreateMessageRequest request) {

        long startTime = System.currentTimeMillis();

        log.info("🔵 [START] Adding message to session. SessionId={}, Sender={}, UserId={}, HasContext={}",
                sessionId, request.sender(), request.userId(),
                request.context() != null && !request.context().isEmpty());

        log.debug("📝 Request details: Content length={}, Context items={}",
                request.content() != null ? request.content().length() : 0,
                request.context() != null ? request.context().size() : 0);

        try {
            log.debug("💾 Calling messageService.addMessage()...");
            ChatMessage created = messageService.addMessage(sessionId, request);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [SUCCESS] Message saved. MessageId={}, SessionId={}, Duration={}ms",
                    created.getId(), sessionId, duration);

            MessageResponse response = messageMapper.toDto(created);
            log.debug("📤 Returning response with MessageId={}", response.id());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ [ERROR] Failed to add message. SessionId={}, Duration={}ms, Error={}",
                    sessionId, duration, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    @Operation(summary = "Get messages from a session", description = "Retrieves paginated message history for a chat session")
    public ResponseEntity<PagedResponse<MessageResponse>> getMessages(
            @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size) {

        long startTime = System.currentTimeMillis();
        int pageSize = size != null ? size : defaultPageSize;
        pageSize = Math.min(pageSize, maxPageSize);

        log.info("🔍 [START] Fetching messages. SessionId={}, Page={}, PageSize={}",
                sessionId, page, pageSize);

        try {
            Page<ChatMessage> result = messageService.getMessages(sessionId, page, pageSize);

            log.debug("📊 Query result: TotalElements={}, TotalPages={}, CurrentPage={}, HasContent={}",
                    result.getTotalElements(), result.getTotalPages(), result.getNumber(),
                    !result.getContent().isEmpty());

            var content = messageMapper.toDtoList(result.getContent());

            PagedResponse<MessageResponse> response = new PagedResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [SUCCESS] Messages fetched. SessionId={}, Count={}, Duration={}ms",
                    sessionId, content.size(), duration);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ [ERROR] Failed to fetch messages. SessionId={}, Duration={}ms, Error={}",
                    sessionId, duration, e.getMessage(), e);
            throw e;
        }
    }
}
