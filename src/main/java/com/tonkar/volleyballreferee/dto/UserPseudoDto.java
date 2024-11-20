package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.*;

public record UserPseudoDto(@NotBlank @Size(min = 3) String userPseudo) {}
