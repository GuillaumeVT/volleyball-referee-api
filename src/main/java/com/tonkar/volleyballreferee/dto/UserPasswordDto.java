package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordDto(@NotBlank String userPassword) {}
