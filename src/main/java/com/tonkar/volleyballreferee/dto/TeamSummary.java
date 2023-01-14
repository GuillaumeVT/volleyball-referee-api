package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@FieldNameConstants
public record TeamSummary(
        @NotNull
        UUID id,
        @NotBlank
        String createdBy,
        long createdAt,
        long updatedAt,
        @NotBlank
        String name,
        @NotNull
        GameType kind,
        @NotNull
        GenderType gender) {
}
