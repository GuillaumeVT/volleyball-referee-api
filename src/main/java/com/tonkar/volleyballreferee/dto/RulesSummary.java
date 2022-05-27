package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

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
