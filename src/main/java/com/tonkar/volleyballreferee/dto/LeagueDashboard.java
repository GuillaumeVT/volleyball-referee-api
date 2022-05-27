package com.tonkar.volleyballreferee.dto;

import java.util.List;

public record LeagueDashboard(
        List<GameSummary> liveGames,
        List<GameSummary> last10Games,
        List<GameSummary> next10Games) {
}
