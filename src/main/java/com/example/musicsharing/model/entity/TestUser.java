package com.example.musicsharing.models.entities;

import com.example.musicsharing.cache.annotations.Tracked;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Tracked
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class TestUser {
    private long id;
    private String firstName;
    private String lastName;
}
