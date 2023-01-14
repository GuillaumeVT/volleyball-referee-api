package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordUpdate(
        @NotBlank
        String currentPassword,
        @NotBlank
        String newPassword) {
}
