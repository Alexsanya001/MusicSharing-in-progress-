package com.example.musicsharing.models.mappers;

import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.models.entities.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(RegisterDTO registerDTO);

    UserInfoDTO toUserInfoDTO(User user);

    List<UserInfoDTO> toUserInfoDTOList(List<User> users);
}
