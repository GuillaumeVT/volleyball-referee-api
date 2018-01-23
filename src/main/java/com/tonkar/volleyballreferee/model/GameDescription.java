package com.tonkar.volleyballreferee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="gameDescriptions")
public class GameDescription {

    @Id
    private String  id;
    private String  kind;
    private long    date;
    private String  gender;
    private String  usage;
    private boolean live;
    private String  league;
    private String  hName;
    private String  gName;
    private int     hSets;
    private int     gSets;

    public GameDescription() {}

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
}
