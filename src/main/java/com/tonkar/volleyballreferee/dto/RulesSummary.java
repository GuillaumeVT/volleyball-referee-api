package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@FieldNameConstants
public record RulesSummary(
        @NotNull
        UUID id,
        @NotBlank
        String createdBy,
        long createdAt,
        long updatedAt,
        @NotBlank
        String name,
        @NotNull
        GameType kind) {
}
