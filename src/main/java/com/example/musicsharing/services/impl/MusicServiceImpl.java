package com.example.musicsharing.services.impl;

import com.example.musicsharing.models.dto.SongUploadRequestDto;
import com.example.musicsharing.models.entities.Genre;
import com.example.musicsharing.models.entities.Music;
import com.example.musicsharing.models.entities.MusicFile;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.models.mappers.MusicMapper;
import com.example.musicsharing.repositories.GenreRepository;
import com.example.musicsharing.repositories.MusicFileRepository;
import com.example.musicsharing.repositories.MusicRepository;
import com.example.musicsharing.services.MusicService;
import com.example.musicsharing.services.ObjectStorageService;
import com.example.musicsharing.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MusicServiceImpl implements MusicService {

    MusicMapper musicMapper;
    MusicFileRepository musicFileRepository;
    GenreRepository genreRepository;
    ObjectStorageService objectStorageService;


    @Override
    @Transactional
    public String uploadTrack(final MultipartFile file, final SongUploadRequestDto dto) {
        long start = System.currentTimeMillis();
        User author = SecurityUtils.getCurrentUser().orElseThrow(
                () -> new IllegalStateException("User not logged in")
        );

        String key = author.getUsername() + "/" + dto.title();
        String link;
        try {
            objectStorageService.uploadFile(key, file.getInputStream(), file.getSize());
            link = objectStorageService.getPublicUrl(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Music music = musicMapper.toMusic(dto);
        music.setGenres(getGenresForSong(dto));
        music.setAuthor(author);

        MusicFile musicFile = new MusicFile(link, music);
        musicFileRepository.save(musicFile);

        long end = System.currentTimeMillis();
        log.info("///////////////////////Uploaded music in {} ms/////////////////////", end - start);

        return link;
    }


    private Set<Genre> getGenresForSong(SongUploadRequestDto dto) {
        if (dto.genreNames().isEmpty()) {
            return Collections.emptySet();
        }
        return dto.genreNames().stream()
                .map(genreName -> genreRepository.findByGenreNameIgnoreCase(genreName)
                        .orElseGet(() -> new Genre(genreName))
                )
                .collect(Collectors.toSet());
    }
}
