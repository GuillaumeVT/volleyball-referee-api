package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.*;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@FieldNameConstants
public record TeamSummaryDto(UUID id, UUID createdBy, long createdAt, long updatedAt, String name, GameType kind, GenderType gender) {}
