package com.example.ragchatstorage.mapper;

import com.example.ragchatstorage.dto.CreateSessionRequest;
import com.example.ragchatstorage.dto.SessionResponse;
import com.example.ragchatstorage.model.ChatSession;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatSessionMapper {

    ChatSession toEntity(CreateSessionRequest request);

    default SessionResponse toDto(ChatSession session) {
        return SessionResponse.from(session);
    }

    default List<SessionResponse> toDtoList(List<ChatSession> sessions) {
        if (sessions == null) {
            return null;
        }
        return sessions.stream()
                .map(SessionResponse::from)
                .collect(java.util.stream.Collectors.toList());
    }
}
