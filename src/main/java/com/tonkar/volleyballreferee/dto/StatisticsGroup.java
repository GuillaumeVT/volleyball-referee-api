package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class StatisticsGroup {
    private Statistics globalStatistics;
    private Statistics userStatistics;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Statistics {
        private List<Count> gameStatistics;
        private List<Count> teamStatistics;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Count {
        private GameType kind;
        private long     count;
    }

}
