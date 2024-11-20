package com.tonkar.volleyballreferee.dto;

import lombok.experimental.FieldNameConstants;

import java.util.List;

@FieldNameConstants
public record LeagueDashboardDto(List<GameSummaryDto> liveGames, List<GameSummaryDto> last10Games, List<GameSummaryDto> next10Games) {}
