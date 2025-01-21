package com.example.musicsharing.models.dto;

import com.example.musicsharing.validation.annotations.ValidEmail;
import com.example.musicsharing.validation.annotations.ValidName;
import com.example.musicsharing.validation.annotations.ValidUsername;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserInfoDTO {

    @ValidUsername
    private String username;

    @ValidEmail
    private String email;

    @ValidName
    private String firstName;

    @ValidName
    private String lastName;
}
