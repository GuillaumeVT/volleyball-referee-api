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
    private int                hPoints;
    private int                gPoints;
    private int                hTimeouts;
    private int                gTimeouts;
    @NotNull
    private List<String>       ladder;
    @NotBlank
    private String             serving;
    @NotBlank
    private String             firstServing;
    @Valid
    private Court              hCurrentPlayers;
    @Valid
    private Court              gCurrentPlayers;
    @Valid
    private Court              hStartingPlayers;
    @Valid
    private Court              gStartingPlayers;
    @NotNull
    private List<Substitution> hSubstitutions;
    @NotNull
    private List<Substitution> gSubstitutions;
    private int                hCaptain;
    private int                gCaptain;
    @NotNull
    private List<Timeout>      hCalledTimeouts;
    @NotNull
    private List<Timeout>      gCalledTimeouts;
    private long               rTime;

}
