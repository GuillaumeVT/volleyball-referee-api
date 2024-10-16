package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.*;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@FieldNameConstants
public record UserSummary(@NotNull UUID id, @NotBlank String pseudo, boolean admin) {}
