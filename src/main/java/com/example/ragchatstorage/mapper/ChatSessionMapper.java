package com.example.ragchatstorage.mapper;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.dto.SessionResponse;
import com.example.ragchatstorage.model.ChatSession;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for ChatSession entity and DTOs.
 * Automatically generates mapping implementations at compile-time.
 *
 * Benefits:
 * - Type-safe compile-time mapping
 * - No reflection overhead
 * - Clean and maintainable code
 *
 * MapStruct automatically maps matching field names between source and target.
 */
@Mapper(componentModel = "spring")
public interface ChatSessionMapper {

    /**
     * Maps CreateSessionRequest DTO to ChatSession entity.
     * MapStruct automatically maps: userId, title
     */
    ChatSession toEntity(CreateSessionRequest request);

    /**
     * Maps ChatSession entity to SessionResponse DTO.
     * MapStruct automatically maps all matching fields.
     */
    SessionResponse toDto(ChatSession session);

    /**
     * Maps list of ChatSession entities to list of SessionResponse DTOs.
     */
    List<SessionResponse> toDtoList(List<ChatSession> sessions);
}
