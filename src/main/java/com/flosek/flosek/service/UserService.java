package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.UserRequestDTO;
import com.flosek.flosek.dto.response.UserResponseDTO;

import java.util.List;
import java.util.UUID;
import com.flosek.flosek.enums.Role;

public interface UserService {

    UserResponseDTO getProfile(UUID userId);

    UserResponseDTO updateProfile(UUID userId, UserRequestDTO request);

    void changePassword(UUID userId, String currentPassword, String newPassword);

    List<UserResponseDTO>getUsersRoleUser(Role role);
}
