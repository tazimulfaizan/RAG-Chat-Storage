package com.example.ragchatstorage.mapper;

import com.example.ragchatstorage.dto.ContextItemDto;
import com.example.ragchatstorage.dto.CreateMessageRequest;
import com.example.ragchatstorage.dto.MessageResponse;
import com.example.ragchatstorage.model.ChatMessage;
import com.example.ragchatstorage.model.ContextItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for ChatMessage entity and DTOs.
 * Automatically generates mapping implementations at compile-time.
 *
 * Note: toDto() uses MessageResponse.from() due to complex context conversion
 * that MapStruct cannot auto-map (ContextItem -> Map<String,Object>)
 */
@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    /**
     * Maps CreateMessageRequest DTO to ChatMessage entity.
     * sessionId and createdAt are set manually in service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ChatMessage toEntity(CreateMessageRequest request);

    /**
     * Maps ContextItemDto to ContextItem entity.
     * MapStruct automatically maps all matching fields.
     */
    ContextItem toEntity(ContextItemDto dto);

    /**
     * Maps ChatMessage entity to MessageResponse DTO.
     * Uses MessageResponse.from() for complex context conversion.
     */
    default MessageResponse toDto(ChatMessage message) {
        return MessageResponse.from(message);
    }

    /**
     * Maps list of ChatMessage entities to list of MessageResponse DTOs.
     */
    default List<MessageResponse> toDtoList(List<ChatMessage> messages) {
        if (messages == null) {
            return null;
        }
        return messages.stream()
                .map(this::toDto)
                .toList();
    }
}
