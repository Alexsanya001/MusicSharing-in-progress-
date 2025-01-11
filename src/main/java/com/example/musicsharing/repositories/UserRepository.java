package com.example.musicsharing.repositories;

import com.example.musicsharing.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String value);

    boolean existsByEmail(String value);

    Optional<User> findByUsername(String value);

    Optional<User> findByEmail(String email);
}
