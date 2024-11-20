package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.TeamType;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.*;

@Getter
@Setter
@FieldNameConstants
public class GameScoreDto {
    private UUID                id;
    private long                scheduledAt;
    private String              homeTeamName;
    private String              guestTeamName;
    private String              homeTeamColor;
    private String              guestTeamColor;
    private int                 homeSets;
    private int                 guestSets;
    private List<SetSummaryDto> sets;

    public GameScoreDto() {
        this.sets = new ArrayList<>();
    }

    public String getTeamName(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? homeTeamName : guestTeamName;
    }

    public String getTeamColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? homeTeamColor : guestTeamColor;
    }
}
