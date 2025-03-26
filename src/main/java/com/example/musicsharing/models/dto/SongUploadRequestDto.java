package com.example.musicsharing.models.dto;

import java.math.BigDecimal;

public record SongUploadRequestDto(
        String title,
        String genre,
        BigDecimal price) {
}
