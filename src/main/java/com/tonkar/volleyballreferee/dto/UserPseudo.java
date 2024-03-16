package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.*;

public record UserPseudo(@NotBlank @Size(min = 3) String userPseudo) {}
