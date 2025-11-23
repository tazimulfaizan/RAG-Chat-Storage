package com.example.ragchatstorage.mapper;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.dto.SessionResponse;
import com.example.ragchatstorage.model.ChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatSessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "favorite", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ChatSession toEntity(CreateSessionRequest request);

    SessionResponse toDto(ChatSession session);

    List<SessionResponse> toDtoList(List<ChatSession> sessions);
}
