package com.example.ragchatstorage.dto;

import com.example.ragchatstorage.model.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateMessageRequest(
    @NotNull SenderType sender,
    @NotBlank String content,
    @NotBlank String userId, // Added: userId supplied by client
    List<ContextItemDto> context
) {}
