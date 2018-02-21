package com.tonkar.volleyballreferee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection="games")
public class Game {

    @Id
    private String         id;
    @NotNull
    private String         kind;
    @NotNull
    private long           date;
    @NotNull
    private String         gender;
    @NotNull
    private String         usage;
    @NotNull
    private boolean        live;
    @NotNull
    private String         league;
    @NotNull
    private Team           hTeam;
    @NotNull
    private Team           gTeam;
    @NotNull
    private int            hSets;
    @NotNull
    private int            gSets;
    @NotNull
    private List<Set>      sets;
    @NotNull
    private List<Sanction> hCards;
    @NotNull
    private List<Sanction> gCards;

    public Game() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public Team gethTeam() {
        return hTeam;
    }

    public void sethTeam(Team hTeam) {
        this.hTeam = hTeam;
    }

    public Team getgTeam() {
        return gTeam;
    }

    public void setgTeam(Team gTeam) {
        this.gTeam = gTeam;
    }

    public int gethSets() {
        return hSets;
    }

    public void sethSets(int hSets) {
        this.hSets = hSets;
    }

    public int getgSets() {
        return gSets;
    }

    public void setgSets(int gSets) {
        this.gSets = gSets;
    }

    public List<Set> getSets() {
        return sets;
    }

    public void setSets(List<Set> sets) {
        this.sets = sets;
    }

    public List<Sanction> gethCards() {
        return hCards;
    }

    public void sethCards(List<Sanction> hCards) {
        this.hCards = hCards;
    }

    public List<Sanction> getgCards() {
        return gCards;
    }

    public void setgCards(List<Sanction> gCards) {
        this.gCards = gCards;
    }
}
