package com.example.ragchatstorage.mapper;

import com.example.ragchatstorage.dto.ContextItemDto;
import com.example.ragchatstorage.dto.CreateMessageRequest;
import com.example.ragchatstorage.dto.MessageResponse;
import com.example.ragchatstorage.model.ChatMessage;
import com.example.ragchatstorage.model.ContextItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    // Request -> Entity
    ChatMessage toEntity(CreateMessageRequest request);

    ContextItem toEntity(ContextItemDto dto);

    // Entity -> Response DTO
    default MessageResponse toDto(ChatMessage message) {
        return MessageResponse.from(message);
    }

    default List<MessageResponse> toDtoList(List<ChatMessage> messages) {
        if (messages == null) {
            return null;
        }
        return messages.stream()
                .map(MessageResponse::from)
                .collect(java.util.stream.Collectors.toList());
    }
}
