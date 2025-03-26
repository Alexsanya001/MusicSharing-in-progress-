package com.example.musicsharing.services.impl;

import com.example.musicsharing.models.dto.SongUploadRequestDto;
import com.example.musicsharing.repositories.MusicRepository;
import com.example.musicsharing.services.MusicService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MusicServiceImpl implements MusicService {

    MusicRepository musicRepository;

    @Override
    public String uploadTrack(final MultipartFile file, final SongUploadRequestDto dto, final Principal author) {

        return "";
    }
}
