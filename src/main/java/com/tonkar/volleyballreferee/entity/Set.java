package com.tonkar.volleyballreferee.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
public class Set {
    private long               duration;
    private long               startTime;
    private long               endTime;
    private int                homePoints;
    private int                guestPoints;
    private int                homeTimeouts;
    private int                guestTimeouts;
    @NotNull
    private List<String>       ladder;
    @NotBlank
    private String             serving;
    @NotBlank
    private String             firstServing;
    @Valid
    private Court              homeCurrentPlayers;
    @Valid
    private Court              guestCurrentPlayers;
    @Valid
    private Court              homeStartingPlayers;
    @Valid
    private Court              guestStartingPlayers;
    @NotNull
    private List<Substitution> homeSubstitutions;
    @NotNull
    private List<Substitution> guestSubstitutions;
    private int                homeCaptain;
    private int                guestCaptain;
    @NotNull
    private List<Timeout>      homeCalledTimeouts;
    @NotNull
    private List<Timeout>      guestCalledTimeouts;

    public int getPoints(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? homePoints : guestPoints;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Court {
        private int p1;
        private int p2;
        private int p3;
        private int p4;
        private int p5;
        private int p6;

        public boolean isFilled(GameType kind) {
            return switch (kind) {
                case INDOOR -> p1 >= 0 && p2 >= 0 && p3 >= 0 && p4 >= 0 && p5 >= 0 && p6 >= 0;
                case INDOOR_4X4 -> p1 >= 0 && p2 >= 0 && p3 >= 0 && p4 >= 0;
                case SNOW -> p1 >= 0 && p2 >= 0 && p3 >= 0;
                default -> true;
            };
        }
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Substitution {
        private int playerIn;
        private int playerOut;
        private int homePoints;
        private int guestPoints;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Timeout {
        private int homePoints;
        private int guestPoints;
    }
}
