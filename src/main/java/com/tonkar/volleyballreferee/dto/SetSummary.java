package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.TeamType;

public record SetSummary(int homePoints, int guestPoints) {
    public int getPoints(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? homePoints : guestPoints;
    }
}
