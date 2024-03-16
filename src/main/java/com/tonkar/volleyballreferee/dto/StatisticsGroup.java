package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;

import java.util.List;

public record StatisticsGroup(Statistics globalStatistics, Statistics userStatistics) {

    public StatisticsGroup(Statistics globalStatistics) {
        this(globalStatistics, null);
    }

    public record Statistics(List<Count> gameStatistics, List<Count> teamStatistics) {}

    public record Count(GameType kind, long count) {}
}
