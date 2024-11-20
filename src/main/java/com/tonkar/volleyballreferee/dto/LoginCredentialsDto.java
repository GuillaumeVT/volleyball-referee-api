package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginCredentialsDto(@NotBlank String pseudo, @NotBlank String password) {}
