package com.tonkar.volleyballreferee.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record EmailCredentials(
        @NotBlank
        @Email
        String userEmail,
        @NotBlank
        String userPassword) {
}
