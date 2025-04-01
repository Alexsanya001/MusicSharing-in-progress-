package com.example.musicsharing.repositories;

import com.example.musicsharing.models.entities.MusicFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicFileRepository extends JpaRepository<MusicFile, Long> {
}
