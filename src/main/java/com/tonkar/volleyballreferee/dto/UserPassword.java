package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPassword(@NotBlank String userPassword) {}
