package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.SongUploadRequestDto;
import com.example.musicsharing.services.MusicService;
import com.example.musicsharing.services.ObjectStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("api/music")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MusicController {

    private final MusicService musicService;
    private final ObjectStorageService service;

//    @PreAuthorize("hasRole('AUTHOR')")
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<String> upload(
//            @RequestPart("dto") @Validated SongUploadRequestDto dto,
//            @RequestPart("file") File file,
//            Principal author) {
//
//        String link = musicService.uploadTrack(file, dto, author);
//
//        return ResponseEntity.ok(link);
//    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(
            @RequestPart("file") MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("upload_", file.getOriginalFilename());
        file.transferTo(tempFile);
        service.uploadFile(tempFile.getName(), tempFile);
        return ResponseEntity.ok("ok");
    }
}
