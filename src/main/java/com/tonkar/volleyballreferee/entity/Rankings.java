package com.tonkar.volleyballreferee.entity;

import com.tonkar.volleyballreferee.dto.Ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Rankings {

    private final Map<String, Ranking> rankingMap;

    public Rankings() {
        this.rankingMap = new HashMap<>();
    }

    public void addGame(Game game) {
        String teamName = game.getHomeTeam().getName();
        Ranking ranking = rankingMap.get(teamName);

        if (ranking == null) {
            ranking = new Ranking(teamName, game.getHomeTeam().getColor());
            rankingMap.put(teamName, ranking);
        }

        ranking.addGame(TeamType.HOME, game);

        teamName = game.getGuestTeam().getName();
        ranking = rankingMap.get(teamName);

        if (ranking == null) {
            ranking = new Ranking(teamName, game.getGuestTeam().getColor());
            rankingMap.put(teamName, ranking);
        }

        ranking.addGame(TeamType.GUEST, game);
    }

    public List<Ranking> list() {
        return rankingMap.values().stream().sorted().collect(Collectors.toList());
    }

}