package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailCredentials(
        @NotBlank
        @Email
        String userEmail,
        @NotBlank
        String userPassword) {
}
