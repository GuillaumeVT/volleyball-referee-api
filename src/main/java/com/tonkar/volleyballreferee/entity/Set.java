package com.tonkar.volleyballreferee.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor @Getter @Setter
public class Set {

    private long               duration;
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
    private long               remainingTime;

}
