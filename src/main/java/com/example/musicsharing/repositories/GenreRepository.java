package com.example.musicsharing.repositories;

import com.example.musicsharing.models.entities.Genre;
import com.example.musicsharing.models.entities.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Short> {
    Optional<Genre> findByGenreNameIgnoreCase(String genreName);
}
