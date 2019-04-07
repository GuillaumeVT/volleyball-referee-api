package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Statistics {

    private final List<Count> gameStatistics;
    private final List<Count> teamStatistics;

    public Statistics() {
        this.gameStatistics = new ArrayList<>();
        this.teamStatistics = new ArrayList<>();
    }

    @NoArgsConstructor @Getter @Setter
    public static class Count {

        private GameType kind;
        private long     count;

    }

}
