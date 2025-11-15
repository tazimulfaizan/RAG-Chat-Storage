package com.example.ragchatstorage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextItem {
    private String sourceId;
    private String snippet;
    private Map<String, Object> metadata;
}
