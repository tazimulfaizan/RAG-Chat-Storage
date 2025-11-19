package com.example.ragchatstorage.mapper;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.dto.SessionResponse;
import com.example.ragchatstorage.model.ChatSession;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatSessionMapper {

    ChatSession toEntity(CreateSessionRequest request);

    SessionResponse toDto(ChatSession session);

    List<SessionResponse> toDtoList(List<ChatSession> sessions);
}
