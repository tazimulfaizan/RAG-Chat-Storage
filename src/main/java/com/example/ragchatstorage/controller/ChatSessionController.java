package com.example.ragchatstorage.controller;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.dto.FavoriteSessionRequest;
import com.example.ragchatstorage.dto.RenameSessionRequest;
import com.example.ragchatstorage.dto.SessionResponse;
import com.example.ragchatstorage.mapper.ChatSessionMapper;
import com.example.ragchatstorage.service.ChatMessageService;
import com.example.ragchatstorage.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Chat Sessions", description = "APIs for managing chat sessions")
@SecurityRequirement(name = "ApiKeyAuth")
public class ChatSessionController {

    private final ChatSessionService sessionService;
    private final ChatMessageService messageService;
    private final ChatSessionMapper sessionMapper;  // ✅ Added MapStruct mapper

    @PostMapping
    @Operation(summary = "Create a new chat session", description = "Creates a new chat session for a user")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        var created = sessionService.createSession(request);
        // ✅ Using MapStruct mapper for DTO conversion
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionMapper.toDto(created));
    }

    @GetMapping
    @Operation(summary = "Get sessions for a user", description = "Retrieves all chat sessions for a user with optional favorite filter")
    public ResponseEntity<List<SessionResponse>> getSessions(
            @Parameter(description = "User ID", required = true) @RequestParam String userId,
            @Parameter(description = "Filter by favorite status") @RequestParam(required = false) Boolean favorite) {
        var sessions = sessionService.getSessionsForUser(userId, favorite);
        // ✅ Using MapStruct mapper for list conversion
        var response = sessionMapper.toDtoList(sessions);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/rename")
    @Operation(summary = "Rename a session", description = "Updates the title of a chat session")
    public ResponseEntity<SessionResponse> rename(
            @Parameter(description = "Session ID", required = true) @PathVariable String id,
            @Valid @RequestBody RenameSessionRequest request) {
        var updated = sessionService.renameSession(id, request.title());
        // ✅ Using MapStruct mapper
        return ResponseEntity.ok(sessionMapper.toDto(updated));
    }

    @PatchMapping("/{id}/favorite")
    @Operation(summary = "Mark/unmark session as favorite", description = "Updates the favorite status of a chat session")
    public ResponseEntity<SessionResponse> markFavorite(
            @Parameter(description = "Session ID", required = true) @PathVariable String id,
            @Valid @RequestBody FavoriteSessionRequest request) {
        var updated = sessionService.markFavorite(id, request.favorite());
        // ✅ Using MapStruct mapper
        return ResponseEntity.ok(sessionMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a session", description = "Deletes a chat session and all its messages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@Parameter(description = "Session ID", required = true) @PathVariable String id) {
        // delete messages first, then session
        messageService.deleteMessagesForSession(id);
        sessionService.deleteSession(id);
    }
}
