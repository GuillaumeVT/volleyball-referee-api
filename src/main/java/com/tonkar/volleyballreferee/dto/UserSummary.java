package com.tonkar.volleyballreferee.dto;

import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@FieldNameConstants
public record UserSummary(
        @NotBlank
        String id,
        @NotBlank
        String pseudo,
        @Email
        String email,
        boolean admin) {
}
