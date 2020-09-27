package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.TeamType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SetSummary {
    private int homePoints;
    private int guestPoints;

    public int getPoints(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? homePoints : guestPoints;
    }
}
