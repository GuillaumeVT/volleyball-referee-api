package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;

import java.util.List;

public record StatisticsGroupDto(StatisticsDto globalStatistics, StatisticsDto userStatistics) {

    public StatisticsGroupDto(StatisticsDto globalStatistics) {
        this(globalStatistics, null);
    }

    public record StatisticsDto(List<CountDto> gameStatistics, List<CountDto> teamStatistics) {}

    public record CountDto(GameType kind, long count) {}
}
