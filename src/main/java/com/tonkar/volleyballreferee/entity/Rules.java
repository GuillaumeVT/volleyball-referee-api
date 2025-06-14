package com.tonkar.volleyballreferee.entity;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
@Document(collection = "rules")
public class Rules {

    public static final int FIVB_LIMITATION          = 1;
    public static final int ALTERNATIVE_LIMITATION_1 = 2;
    public static final int ALTERNATIVE_LIMITATION_2 = 3;
    public static final int NO_LIMITATION            = 4;

    public static final int WIN_TERMINATION      = 1;
    public static final int ALL_SETS_TERMINATION = 2;

    @Id
    @NotNull
    private UUID     id;
    @NotNull
    private UUID     createdBy;
    private long     createdAt;
    private long     updatedAt;
    @NotBlank
    private String   name;
    @NotNull
    private GameType kind;
    private int      setsPerGame;
    private int      pointsPerSet;
    private boolean  tieBreakInLastSet;
    private int      pointsInTieBreak;
    private boolean  twoPointsDifference;
    private boolean  sanctions;
    private int      matchTermination;
    private boolean  teamTimeouts;
    private int      teamTimeoutsPerSet;
    private int      teamTimeoutDuration;
    private boolean  technicalTimeouts;
    private int      technicalTimeoutDuration;
    private boolean  gameIntervals;
    private int      gameIntervalDuration;
    private int      substitutionsLimitation;
    private int      teamSubstitutionsPerSet;
    private boolean  beachCourtSwitches;
    private int      beachCourtSwitchFreq;
    private int      beachCourtSwitchFreqTieBreak;
    private int      customConsecutiveServesPerPlayer;

    public Rules(UUID id,
                 UUID createdBy,
                 long createdAt,
                 long updatedAt,
                 String name,
                 GameType kind,
                 int setsPerGame,
                 int pointsPerSet,
                 boolean tieBreakInLastSet,
                 int pointsInTieBreak,
                 boolean twoPointsDifference,
                 boolean sanctions,
                 int matchTermination,
                 boolean teamTimeouts,
                 int teamTimeoutsPerSet,
                 int teamTimeoutDuration,
                 boolean technicalTimeouts,
                 int technicalTimeoutDuration,
                 boolean gameIntervals,
                 int gameIntervalDuration,
                 int substitutionsLimitation,
                 int teamSubstitutionsPerSet,
                 boolean beachCourtSwitches,
                 int beachCourtSwitchFreq,
                 int beachCourtSwitchFreqTieBreak,
                 int customConsecutiveServesPerPlayer) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.name = name;
        this.kind = kind;
        this.setsPerGame = setsPerGame;
        this.pointsPerSet = pointsPerSet;
        this.tieBreakInLastSet = tieBreakInLastSet;
        this.pointsInTieBreak = pointsInTieBreak;
        this.twoPointsDifference = twoPointsDifference;
        this.sanctions = sanctions;
        this.matchTermination = matchTermination;
        this.teamTimeouts = teamTimeouts;
        this.teamTimeoutsPerSet = teamTimeoutsPerSet;
        this.teamTimeoutDuration = teamTimeoutDuration;
        this.technicalTimeouts = technicalTimeouts;
        this.technicalTimeoutDuration = technicalTimeoutDuration;
        this.gameIntervals = gameIntervals;
        this.gameIntervalDuration = gameIntervalDuration;
        this.substitutionsLimitation = substitutionsLimitation;
        this.teamSubstitutionsPerSet = teamSubstitutionsPerSet;
        this.beachCourtSwitches = beachCourtSwitches;
        this.beachCourtSwitchFreq = beachCourtSwitchFreq;
        this.beachCourtSwitchFreqTieBreak = beachCourtSwitchFreqTieBreak;
        this.customConsecutiveServesPerPlayer = customConsecutiveServesPerPlayer;

        checkSubstitutions();
    }

    private static final UUID DEFAULT_CREATED_BY = UUID.fromString("3fc31b3b-4f2b-47c9-89c8-ad6ead6902ea");

    public static final Rules OFFICIAL_INDOOR_RULES = new Rules(UUID.fromString("efb06d97-264e-425d-b8ca-b499e3b63a95"), DEFAULT_CREATED_BY,
                                                                0L, 0L, "FIVB indoor 6x6 rules", GameType.INDOOR, 5, 25, true, 15, true,
                                                                true, WIN_TERMINATION, true, 2, 30, false, 60, true, 180, FIVB_LIMITATION,
                                                                6, false, 0, 0, 9999);

    public static final Rules OFFICIAL_BEACH_RULES = new Rules(UUID.fromString("cceb81c9-2201-4495-8a5e-e289a77e24bf"), DEFAULT_CREATED_BY,
                                                               0L, 0L, "FIVB beach rules", GameType.BEACH, 3, 21, true, 15, true, true,
                                                               WIN_TERMINATION, true, 1, 30, true, 30, true, 60, FIVB_LIMITATION, 0, true,
                                                               7, 5, 9999);

    public static final Rules DEFAULT_INDOOR_4X4_RULES = new Rules(UUID.fromString("375dd005-08b6-45f8-a60f-7e04e1e5ba71"),
                                                                   DEFAULT_CREATED_BY, 0L, 0L, "Default 4x4 rules", GameType.INDOOR_4X4, 5,
                                                                   25, true, 15, true, true, WIN_TERMINATION, true, 2, 30, true, 60, true,
                                                                   180, NO_LIMITATION, 4, false, 0, 0, 9999);

    public static final Rules OFFICIAL_SNOW_RULES = new Rules(UUID.fromString("ff03b7e2-f794-4d32-9e6c-a046f75eafa5"), DEFAULT_CREATED_BY,
                                                              0L, 0L, "FIVB snow rules", GameType.SNOW, 3, 15, false, 15, true, true,
                                                              WIN_TERMINATION, true, 1, 30, false, 0, true, 60, NO_LIMITATION, 2, true, 5,
                                                              5, 9999);

    public static Optional<Rules> getDefaultRules(UUID rulesId, GameType kind) {
        return switch (kind) {
            case INDOOR -> OFFICIAL_INDOOR_RULES.getId().equals(rulesId) ? Optional.of(OFFICIAL_INDOOR_RULES) : Optional.empty();
            case BEACH -> OFFICIAL_BEACH_RULES.getId().equals(rulesId) ? Optional.of(OFFICIAL_BEACH_RULES) : Optional.empty();
            case INDOOR_4X4 -> DEFAULT_INDOOR_4X4_RULES.getId().equals(rulesId) ? Optional.of(DEFAULT_INDOOR_4X4_RULES) : Optional.empty();
            case SNOW -> OFFICIAL_SNOW_RULES.getId().equals(rulesId) ? Optional.of(OFFICIAL_SNOW_RULES) : Optional.empty();
        };
    }

    private void checkSubstitutions() {
        if (FIVB_LIMITATION == substitutionsLimitation && teamSubstitutionsPerSet > 12) {
            teamSubstitutionsPerSet = 12;
        }
    }

}

