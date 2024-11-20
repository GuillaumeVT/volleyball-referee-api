package com.tonkar.volleyballreferee.entity;

import com.tonkar.volleyballreferee.dto.*;

import java.util.*;
import java.util.stream.Collectors;

public class Rankings {

    private final Map<String, RankingDto> rankingMap;

    public Rankings() {
        this.rankingMap = new HashMap<>();
    }

    public void addGame(GameScoreDto game) {
        String teamName = game.getHomeTeamName();
        RankingDto ranking = rankingMap.get(teamName);

        if (ranking == null) {
            ranking = new RankingDto(teamName, game.getHomeTeamColor());
            rankingMap.put(teamName, ranking);
        }

        ranking.addGame(TeamType.HOME, game);

        teamName = game.getGuestTeamName();
        ranking = rankingMap.get(teamName);

        if (ranking == null) {
            ranking = new RankingDto(teamName, game.getGuestTeamColor());
            rankingMap.put(teamName, ranking);
        }

        ranking.addGame(TeamType.GUEST, game);
    }

    public List<RankingDto> list() {
        return rankingMap.values().stream().sorted().collect(Collectors.toList());
    }

}