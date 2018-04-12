package com.tonkar.volleyballreferee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection="teams")
public class Team {

    @Id
    private String        id;
    @Valid
    @NotNull
    private UserId        userId;
    @NotEmpty
    private String        name;
    @NotNull
    private long          date;
    @NotEmpty
    private String        color;
    @NotEmpty
    private String        liberoColor;
    @NotNull
    private List<Integer> players;
    @NotNull
    private List<Integer> liberos;
    @NotNull
    private int           captain;
    @NotEmpty
    private String        gender;

    public Team() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
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
