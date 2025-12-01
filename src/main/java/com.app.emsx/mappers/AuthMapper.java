package com.app.emsx.mappers;

import com.app.emsx.dtos.auth.UserResponse;
import com.app.emsx.entities.User;

public class AuthMapper {

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail().toLowerCase())
                .role(user.getRole())
                .build();
    }
}