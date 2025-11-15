package com.example.ragchatstorage.controller;

import com.example.ragchatstorage.dto.CreateMessageRequest;
import com.example.ragchatstorage.dto.MessageResponse;
import com.example.ragchatstorage.dto.PagedResponse;
import com.example.ragchatstorage.model.ChatMessage;
import com.example.ragchatstorage.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
@Tag(name = "Chat Messages", description = "APIs for managing chat messages within sessions")
@SecurityRequirement(name = "ApiKeyAuth")
public class ChatMessageController {

    private final ChatMessageService messageService;

    @Value("${app.pagination.default-page-size:20}")
    private int defaultPageSize;

    @Value("${app.pagination.max-page-size:100}")
    private int maxPageSize;

    @PostMapping
    @Operation(summary = "Add a message to a session", description = "Creates a new message within a chat session with optional RAG context")
    public ResponseEntity<MessageResponse> addMessage(
            @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
            @Valid @RequestBody CreateMessageRequest request) {
        ChatMessage created = messageService.addMessage(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MessageResponse.from(created));
    }

    @GetMapping
    @Operation(summary = "Get messages from a session", description = "Retrieves paginated message history for a chat session")
    public ResponseEntity<PagedResponse<MessageResponse>> getMessages(
            @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size) {
        int pageSize = size != null ? size : defaultPageSize;
        pageSize = Math.min(pageSize, maxPageSize);

        Page<ChatMessage> result = messageService.getMessages(sessionId, page, pageSize);

        var content = result.getContent().stream()
                .map(MessageResponse::from)
                .toList();

        PagedResponse<MessageResponse> response = new PagedResponse<>(
            content,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isLast()
        );

        return ResponseEntity.ok(response);
    }
}
