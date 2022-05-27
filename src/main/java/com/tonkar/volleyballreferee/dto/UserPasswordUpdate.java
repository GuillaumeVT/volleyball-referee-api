package com.tonkar.volleyballreferee.dto;

import javax.validation.constraints.NotBlank;

public record UserPasswordUpdate(
        @NotBlank
        String currentPassword,
        @NotBlank
        String newPassword) {
}
