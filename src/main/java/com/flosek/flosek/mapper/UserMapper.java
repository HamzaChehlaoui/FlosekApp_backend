package com.flosek.flosek.mapper;

import com.flosek.flosek.dto.request.RegisterRequestDTO;
import com.flosek.flosek.dto.response.AuthResponseDTO;
import com.flosek.flosek.dto.response.UserResponseDTO;
import com.flosek.flosek.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for User entity
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(RegisterRequestDTO request);

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "expiresIn", ignore = true)
    AuthResponseDTO toAuthResponseDTO(User user);

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponseDTO toResponseDTO(User user);

    List<UserResponseDTO> toResponseDTOList(List<User> users);
}
