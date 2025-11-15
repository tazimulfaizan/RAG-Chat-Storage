package com.example.ragchatstorage.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSessionRequest(
    @NotBlank String userId,
    String title
) {}
