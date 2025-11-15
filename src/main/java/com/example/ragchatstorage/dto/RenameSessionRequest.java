package com.example.ragchatstorage.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameSessionRequest(@NotBlank String title) {}
