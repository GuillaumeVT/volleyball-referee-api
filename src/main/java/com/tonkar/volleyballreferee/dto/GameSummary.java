package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import com.tonkar.volleyballreferee.entity.UsageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class GameSummary {
    @NotNull
    private UUID       id;
    @NotBlank
    private String     createdBy;
    private long       createdAt;
    private long       updatedAt;
    private long       scheduledAt;
    @NotBlank
    private String     refereedBy;
    @NotBlank
    private String     refereeName;
    @NotNull
    private GameType   kind;
    @NotNull
    private GenderType gender;
    @NotNull
    private UsageType  usage;
    @NotNull
    private GameStatus status;
    private boolean    indexed;
    private UUID       leagueId;
    private String     leagueName;
    private String     divisionName;
    @NotNull
    private UUID       homeTeamId;
    @NotBlank
    private String     homeTeamName;
    @NotNull
    private UUID       guestTeamId;
    @NotBlank
    private String     guestTeamName;
    private int        homeSets;
    private int        guestSets;
    @NotNull
    private UUID       rulesId;
    @NotBlank
    private String     rulesName;
    @NotNull
    private String     score;
    private String     referee1Name;
    private String     referee2Name;
    private String     scorerName;
}
