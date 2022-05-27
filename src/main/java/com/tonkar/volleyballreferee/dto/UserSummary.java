package com.tonkar.volleyballreferee.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record UserSummary(
        @NotBlank
        String id,
        @NotBlank
        String pseudo,
        @Email
        String email,
        boolean admin) {
}
