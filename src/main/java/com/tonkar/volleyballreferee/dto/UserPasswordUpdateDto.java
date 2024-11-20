package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordUpdateDto(@NotBlank String currentPassword, @NotBlank String newPassword) {}
