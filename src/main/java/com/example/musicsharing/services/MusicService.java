package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.SongUploadRequestDto;
import org.springframework.web.multipart.MultipartFile;

public interface MusicService {

    String uploadTrack(MultipartFile file, SongUploadRequestDto dto);
}
