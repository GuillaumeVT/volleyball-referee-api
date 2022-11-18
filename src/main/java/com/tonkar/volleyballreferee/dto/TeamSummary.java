package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
