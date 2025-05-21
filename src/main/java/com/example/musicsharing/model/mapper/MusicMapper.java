package com.example.musicsharing.models.mappers;

import com.example.musicsharing.models.dto.SongUploadRequestDto;
import com.example.musicsharing.models.entities.Music;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MusicMapper {

    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "author", ignore = true)
    Music toMusic(SongUploadRequestDto dto);
}
