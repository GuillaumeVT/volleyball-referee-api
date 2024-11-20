package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.TeamType;
import lombok.Getter;

@Getter
public class RankingDto implements Comparable<RankingDto> {

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

    public RankingDto(String teamName, String teamColor) {
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

    public void addGame(TeamType teamType, GameScoreDto game) {
        int setsDiff;

        if (TeamType.HOME.equals(teamType)) {
            setsDiff = game.getHomeSets() - game.getGuestSets();
            this.setsFor += game.getHomeSets();
            this.setsAgainst += game.getGuestSets();
            this.setsDiff += setsDiff;

            for (SetSummaryDto set : game.getSets()) {
                int pointsDiff = set.homePoints() - set.guestPoints();
                this.pointsFor += set.homePoints();
                this.pointsAgainst += set.guestPoints();
                this.pointsDiff += pointsDiff;
            }
        } else {
            setsDiff = game.getGuestSets() - game.getHomeSets();
            this.setsFor += game.getGuestSets();
            this.setsAgainst += game.getHomeSets();
            this.setsDiff += setsDiff;

            for (SetSummaryDto set : game.getSets()) {
                int pointsDiff = set.guestPoints() - set.homePoints();
                this.pointsFor += set.guestPoints();
                this.pointsAgainst += set.homePoints();
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
    public int compareTo(RankingDto other) {
        return Integer.compare(other.matchesFor, this.matchesFor);
    }
}
