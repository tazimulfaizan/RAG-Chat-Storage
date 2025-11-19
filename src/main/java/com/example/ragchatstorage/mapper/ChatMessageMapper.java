package com.example.ragchatstorage.mapper;

import com.example.ragchatstorage.dto.ContextItemDto;
import com.example.ragchatstorage.dto.CreateMessageRequest;
import com.example.ragchatstorage.dto.MessageResponse;
import com.example.ragchatstorage.model.ChatMessage;
import com.example.ragchatstorage.model.ContextItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "userId", ignore = true)
    ChatMessage toEntity(CreateMessageRequest request);

    default MessageResponse toDto(ChatMessage message) {
        return MessageResponse.from(message);
    }


    default List<MessageResponse> toDtoList(List<ChatMessage> messages) {
        if (messages == null) {
            return null;
        }
        return messages.stream()
                .map(this::toDto)
                .toList();
    }
}
