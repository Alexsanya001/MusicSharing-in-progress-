package com.example.musicsharing.models.dto;

import com.example.musicsharing.validation.annotations.ValidEmailNew;
import com.example.musicsharing.validation.annotations.ValidName;
import com.example.musicsharing.validation.annotations.ValidPasswordNew;
import com.example.musicsharing.validation.annotations.ValidUsernameNew;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    @ValidUsernameNew
    private String username;

    @ValidPasswordNew
    private String password;

    @ValidEmailNew
    private String email;

    @ValidName
    private String firstName;

    @ValidName
    private String lastName;
}
