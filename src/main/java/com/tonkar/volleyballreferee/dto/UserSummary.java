package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record UserSummary(
        @NotBlank
        String id,
        @NotBlank
        String pseudo,
        @Email
        String email,
        boolean admin,
        boolean subscription,
        long subscriptionExpiryAt) {
}
