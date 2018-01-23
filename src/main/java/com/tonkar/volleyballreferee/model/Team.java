package com.tonkar.volleyballreferee.model;

import javax.validation.constraints.NotNull;
import java.util.List;

public class Team {

    @NotNull
    private String        name;
    @NotNull
    private String        color;
    private String        liberoColor;
    private List<Integer> players;
    private List<Integer> liberos;
    private int           captain;
    private String        gender;

    public Team() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLiberoColor() {
        return liberoColor;
    }

    public void setLiberoColor(String liberoColor) {
        this.liberoColor = liberoColor;
    }

    public List<Integer> getPlayers() {
        return players;
    }

    public void setPlayers(List<Integer> players) {
        this.players = players;
    }

    public List<Integer> getLiberos() {
        return liberos;
    }

    public void setLiberos(List<Integer> liberos) {
        this.liberos = liberos;
    }

    public int getCaptain() {
        return captain;
    }

    public void setCaptain(int captain) {
        this.captain = captain;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
