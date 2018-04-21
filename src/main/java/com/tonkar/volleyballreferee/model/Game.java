package com.tonkar.volleyballreferee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection="games")
public class Game {

    @Id
    private String         id;
    @Valid
    @NotNull
    private UserId         userId;
    @NotEmpty
    private String         kind;
    @NotNull
    private long           date;
    @NotNull
    private long           schedule;
    @NotEmpty
    private String         gender;
    @NotEmpty
    private String         usage;
    @NotEmpty
    private String         status;
    @NotNull
    private String         referee;
    @NotNull
    private String         league;
    @Valid
    @NotNull
    private Team           hTeam;
    @Valid
    @NotNull
    private Team           gTeam;
    @NotNull
    private int            hSets;
    @NotNull
    private int            gSets;
    @Valid
    @NotNull
    private List<Set>      sets;
    @NotNull
    private List<Sanction> hCards;
    @NotNull
    private List<Sanction> gCards;
    @Valid
    @NotNull
    private Rules          rules;

    public Game() {}

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

    public long getSchedule() {
        return schedule;
    }

    public void setSchedule(long schedule) {
        this.schedule = schedule;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferee() {
        return referee;
    }

    public void setReferee(String referee) {
        this.referee = referee;
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

    public Rules getRules() {
        return rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }
}
