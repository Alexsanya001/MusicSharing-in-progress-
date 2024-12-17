package com.example.musicsharing.repositories;

import com.example.musicsharing.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String value);

    boolean existsByEmail(String value);
}
