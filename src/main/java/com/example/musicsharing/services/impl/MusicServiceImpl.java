package com.example.musicsharing.services.impl;

import com.example.musicsharing.models.dto.SongUploadRequestDto;
import com.example.musicsharing.models.entities.Genre;
import com.example.musicsharing.models.entities.Music;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.models.mappers.MusicMapper;
import com.example.musicsharing.repositories.GenreRepository;
import com.example.musicsharing.repositories.MusicRepository;
import com.example.musicsharing.services.MusicService;
import com.example.musicsharing.services.ObjectStorageService;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MusicServiceImpl implements MusicService {

    MusicRepository musicRepository;
    GenreRepository genreRepository;
    ObjectStorageService objectStorageService;
    UserService userService;
    MusicMapper musicMapper;

    @Override
    public String uploadTrack(final File file, final SongUploadRequestDto dto) {
        Music music = musicMapper.toMusic(dto);
        music.setGenres(getGenresForSong(dto));
        music.setAuthor(SecurityUtils.getCurrentUser());
        String key = music.getAuthor().getUsername() + "/" + file.getName();
        objectStorageService.uploadFile(key, file);

        return "music/" + key;
    }

    private Set<Genre> getGenresForSong(SongUploadRequestDto dto) {
        if (dto.genreNames().isEmpty()) {
            return Collections.emptySet();
        }
        return dto.genreNames().stream()
                .map(genreName -> genreRepository.findByGenreNameIgnoreCase(genreName)
                        .orElseGet(() -> {
                            Genre newGenre = new Genre(genreName);
                            return genreRepository.save(newGenre);
                        })
                )
                .collect(Collectors.toSet());
    }
}
