package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.SongUploadRequestDto;
import com.example.musicsharing.models.entities.Genre;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Set;

public interface MusicService {

    String uploadTrack(MultipartFile file, SongUploadRequestDto dto);
}
