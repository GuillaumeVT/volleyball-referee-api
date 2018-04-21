package com.tonkar.volleyballreferee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Document(collection="gameDescriptions")
public class GameDescription {

    @Id
    private String id;
    @Valid
    @NotNull
    private UserId userId;
    @NotEmpty
    private String kind;
    @NotNull
    private long   date;
    @NotNull
    private long   schedule;
    @NotEmpty
    private String gender;
    @NotEmpty
    private String usage;
    @NotEmpty
    private String status;
    @NotNull
    private String referee;
    @NotNull
    private String league;
    @NotEmpty
    private String hName;
    @NotEmpty
    private String gName;
    @NotNull
    private int    hSets;
    @NotNull
    private int    gSets;
    @NotEmpty
    private String rules;

    public GameDescription() {}

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

    public String gethName() {
        return hName;
    }

    public void sethName(String hName) {
        this.hName = hName;
    }

    public String getgName() {
        return gName;
    }

    public void setgName(String gName) {
        this.gName = gName;
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

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }
}
