package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.SongUploadRequestDto;
import com.example.musicsharing.services.MusicService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/music")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MusicController {

    MusicService musicService;

    //@PreAuthorize("hasRole('AUTHOR')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> upload(
            @RequestPart("dto") @Validated SongUploadRequestDto dto,
            @RequestPart("file") MultipartFile file) {

        String link = musicService.uploadTrack(file, dto);
        ApiResponse<String> response = ApiResponse.success(link);
        return ResponseEntity.ok(response);
    }

//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<String> upload(
//            @RequestPart("file") MultipartFile file) throws IOException {
//        storageService.uploadFile(file.getOriginalFilename(), file.getInputStream(), file.getSize());
//        return ResponseEntity.ok("ok");
//    }
}
