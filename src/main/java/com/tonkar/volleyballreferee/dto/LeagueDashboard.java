package com.tonkar.volleyballreferee.dto;

import lombok.experimental.FieldNameConstants;

import java.util.List;

@FieldNameConstants
public record LeagueDashboard(
        List<GameSummary> liveGames,
        List<GameSummary> last10Games,
        List<GameSummary> next10Games) {
}
