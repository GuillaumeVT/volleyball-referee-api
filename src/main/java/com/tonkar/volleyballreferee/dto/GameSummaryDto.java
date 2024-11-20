package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
public class GameSummaryDto {
    @NotNull
    private UUID       id;
    @NotNull
    private UUID       createdBy;
    private long       createdAt;
    private long       updatedAt;
    private long       scheduledAt;
    @NotNull
    private UUID       refereedBy;
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
    private String     score;
    private String     referee1Name;
    private String     referee2Name;
    private String     scorerName;
}
