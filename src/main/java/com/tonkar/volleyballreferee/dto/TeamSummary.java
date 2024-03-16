package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.*;
import jakarta.validation.constraints.*;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@FieldNameConstants
public record TeamSummary(@NotNull UUID id, @NotBlank String createdBy, long createdAt, long updatedAt, @NotBlank String name,
                          @NotNull GameType kind, @NotNull GenderType gender) {}
