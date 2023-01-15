package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPseudo(
        @NotBlank
        @Size(min = 3)
        String userPseudo) {
}
