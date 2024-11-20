package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.NotBlank;

public record NewUserDto(@NotBlank String pseudo, @NotBlank String password) {}
