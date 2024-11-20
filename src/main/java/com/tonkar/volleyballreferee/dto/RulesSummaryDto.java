package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@FieldNameConstants
public record RulesSummaryDto(UUID id, UUID createdBy, long createdAt, long updatedAt, String name, GameType kind) {}
