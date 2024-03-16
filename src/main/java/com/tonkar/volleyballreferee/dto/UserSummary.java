package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.*;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record UserSummary(@NotBlank String id, @NotBlank String pseudo, @Email String email, boolean admin, boolean subscription,
                          long subscriptionExpiryAt) {}
