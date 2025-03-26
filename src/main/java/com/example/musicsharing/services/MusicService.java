package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.SongUploadRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface MusicService {

    String uploadTrack(MultipartFile file, SongUploadRequestDto dto, Principal author);
}
