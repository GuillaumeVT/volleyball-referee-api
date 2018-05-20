package com.tonkar.volleyballreferee.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class Set {

    private long               duration;
    @NotNull
    private int                hPoints;
    @NotNull
    private int                gPoints;
    private int                hTimeouts;
    private int                gTimeouts;
    @NotNull
    private List<String>       ladder;
    @NotEmpty
    private String             serving;
    @NotEmpty
    private String             firstServing;
    private List<Player>       hCurrentPlayers;
    private List<Player>       gCurrentPlayers;
    private List<Player>       hStartingPlayers;
    private List<Player>       gStartingPlayers;
    private List<Substitution> hSubstitutions;
    private List<Substitution> gSubstitutions;
    private int                hCaptain;
    private int                gCaptain;
    private List<Timeout>      hCalledTimeouts;
    private List<Timeout>      gCalledTimeouts;
    private long               rTime;

    public Set() {}

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int gethPoints() {
        return hPoints;
    }

    public void sethPoints(int hPoints) {
        this.hPoints = hPoints;
    }

    public int getgPoints() {
        return gPoints;
    }

    public void setgPoints(int gPoints) {
        this.gPoints = gPoints;
    }

    public int gethTimeouts() {
        return hTimeouts;
    }

    public void sethTimeouts(int hTimeouts) {
        this.hTimeouts = hTimeouts;
    }

    public int getgTimeouts() {
        return gTimeouts;
    }

    public void setgTimeouts(int gTimeouts) {
        this.gTimeouts = gTimeouts;
    }

    public List<String> getLadder() {
        return ladder;
    }

    public void setLadder(List<String> ladder) {
        this.ladder = ladder;
    }

    public String getServing() {
        return serving;
    }

    public void setServing(String serving) {
        this.serving = serving;
    }

    public String getFirstServing() {
        return firstServing;
    }

    public void setFirstServing(String firstServing) {
        this.firstServing = firstServing;
    }

    public List<Player> gethCurrentPlayers() {
        return hCurrentPlayers;
    }

    public void sethCurrentPlayers(List<Player> hCurrentPlayers) {
        this.hCurrentPlayers = hCurrentPlayers;
    }

    public List<Player> getgCurrentPlayers() {
        return gCurrentPlayers;
    }

    public void setgCurrentPlayers(List<Player> gCurrentPlayers) {
        this.gCurrentPlayers = gCurrentPlayers;
    }

    public List<Player> gethStartingPlayers() {
        return hStartingPlayers;
    }

    public void sethStartingPlayers(List<Player> hStartingPlayers) {
        this.hStartingPlayers = hStartingPlayers;
    }

    public List<Player> getgStartingPlayers() {
        return gStartingPlayers;
    }

    public void setgStartingPlayers(List<Player> gStartingPlayers) {
        this.gStartingPlayers = gStartingPlayers;
    }

    public List<Substitution> gethSubstitutions() {
        return hSubstitutions;
    }

    public void sethSubstitutions(List<Substitution> hSubstitutions) {
        this.hSubstitutions = hSubstitutions;
    }

    public List<Substitution> getgSubstitutions() {
        return gSubstitutions;
    }

    public void setgSubstitutions(List<Substitution> gSubstitutions) {
        this.gSubstitutions = gSubstitutions;
    }

    public int gethCaptain() {
        return hCaptain;
    }

    public void sethCaptain(int hCaptain) {
        this.hCaptain = hCaptain;
    }

    public int getgCaptain() {
        return gCaptain;
    }

    public void setgCaptain(int gCaptain) {
        this.gCaptain = gCaptain;
    }

    public List<Timeout> gethCalledTimeouts() {
        return hCalledTimeouts;
    }

    public void sethCalledTimeouts(List<Timeout> hCalledTimeouts) {
        this.hCalledTimeouts = hCalledTimeouts;
    }

    public List<Timeout> getgCalledTimeouts() {
        return gCalledTimeouts;
    }

    public void setgCalledTimeouts(List<Timeout> gCalledTimeouts) {
        this.gCalledTimeouts = gCalledTimeouts;
    }

    public long getrTime() {
        return rTime;
    }

    public void setrTime(long rTime) {
        this.rTime = rTime;
    }
}
