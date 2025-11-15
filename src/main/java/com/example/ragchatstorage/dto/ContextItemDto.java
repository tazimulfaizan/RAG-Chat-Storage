package com.example.ragchatstorage.dto;

import java.util.Map;

public record ContextItemDto(
    String sourceId,
    String snippet,
    Map<String, Object> metadata
) {}
