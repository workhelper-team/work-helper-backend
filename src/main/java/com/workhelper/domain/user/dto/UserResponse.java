package com.workhelper.domain.user.dto;

import com.workhelper.domain.user.entity.User;

public record UserResponse(
        Long id,
        String email,
        String name,
        String phone,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole().name()
        );
    }
}
