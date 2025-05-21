package com.example.musicsharing.models.dto;

import java.math.BigDecimal;
import java.util.Set;

public record SongUploadRequestDto(
        String title,
        Set<String> genreNames,
        BigDecimal price) {
}
