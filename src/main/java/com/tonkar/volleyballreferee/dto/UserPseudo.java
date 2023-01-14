package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPseudo(
        @NotBlank
        String userPseudo) {
}
