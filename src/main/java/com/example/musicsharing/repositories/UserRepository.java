package com.example.musicsharing.repositories;

import com.example.musicsharing.models.dto.AuthUserDto;
import com.example.musicsharing.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String value);

    boolean existsByEmail(String value);

    Optional<User> findByUsername(String value);

    Optional<User> findByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Optional<Long> findUserIdByEmail(@Param("email") String email);

    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Optional<Long> findUserIdByUsername(@Param("username") String username);

    @Query("""
            SELECT new com.example.musicsharing.models.dto.AuthUserDto(
            u.id,
            u.username,
            u.password,
            u.email,
            u.role,
            u.passwordChangedAt
            ) FROM User u
            WHERE u.username = :username
            """)
    Optional<AuthUserDto> findAuthUserDtoByUsername(@Param("username") String username);
}
