package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.TeamType;
import lombok.Getter;

@Getter
public class Ranking implements Comparable<Ranking> {

    private final String teamName;
    private final String teamColor;
    private       int    matchesFor;
    private       int    matchesAgainst;
    private       int    matchesDiff;
    private       int    setsFor;
    private       int    setsAgainst;
    private       int    setsDiff;
    private       int    pointsFor;
    private       int    pointsAgainst;
    private       int    pointsDiff;

    public Ranking(String teamName, String teamColor) {
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.matchesFor = 0;
        this.matchesAgainst = 0;
        this.matchesDiff = 0;
        this.setsFor = 0;
        this.setsAgainst = 0;
        this.setsDiff = 0;
        this.pointsFor = 0;
        this.pointsAgainst = 0;
        this.pointsDiff = 0;
    }

    public void addGame(TeamType teamType, GameScore game) {
        int setsDiff;

        if (TeamType.HOME.equals(teamType)) {
            setsDiff = game.getHomeSets() - game.getGuestSets();
            this.setsFor += game.getHomeSets();
            this.setsAgainst += game.getGuestSets();
            this.setsDiff += setsDiff;

            for (SetSummary set : game.getSets()) {
                int pointsDiff = set.getHomePoints() - set.getGuestPoints();
                this.pointsFor += set.getHomePoints();
                this.pointsAgainst += set.getGuestPoints();
                this.pointsDiff += pointsDiff;
            }
        } else {
            setsDiff = game.getGuestSets() - game.getHomeSets();
            this.setsFor += game.getGuestSets();
            this.setsAgainst += game.getHomeSets();
            this.setsDiff += setsDiff;

            for (SetSummary set : game.getSets()) {
                int pointsDiff = set.getGuestPoints() - set.getHomePoints();
                this.pointsFor += set.getGuestPoints();
                this.pointsAgainst += set.getHomePoints();
                this.pointsDiff += pointsDiff;
            }
        }

        if (setsDiff > 0) {
            this.matchesFor++;
            this.matchesDiff++;
        } else {
            this.matchesAgainst++;
            this.matchesDiff--;
        }
    }


    @Override
    public int compareTo(Ranking other) {
        return Integer.compare(other.matchesFor, this.matchesFor);
    }
}
