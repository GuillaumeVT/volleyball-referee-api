package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.TeamType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@FieldNameConstants
public class GameScore {
    private UUID             id;
    private long             scheduledAt;
    private String           homeTeamName;
    private String           guestTeamName;
    private String           homeTeamColor;
    private String           guestTeamColor;
    private int              homeSets;
    private int              guestSets;
    private List<SetSummary> sets;

    public GameScore() {
        this.sets = new ArrayList<>();
    }

    public String getTeamName(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? homeTeamName : guestTeamName;
    }

    public String getTeamColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? homeTeamColor : guestTeamColor;
    }
}
