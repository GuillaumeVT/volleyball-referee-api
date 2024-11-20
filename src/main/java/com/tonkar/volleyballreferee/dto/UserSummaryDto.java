package com.tonkar.volleyballreferee.dto;

import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@FieldNameConstants
public record UserSummaryDto(UUID id, String pseudo, boolean admin) {}
