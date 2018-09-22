package com.tonkar.volleyballreferee.model;

import com.tonkar.volleyballreferee.security.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Document(collection="rules")
public class Rules {

    public static final transient int FIVB_LIMITATION = 1;
    public static final transient int ALTERNATIVE_LIMITATION_1 = 2;
    public static final transient int ALTERNATIVE_LIMITATION_2 = 3;
    public static final transient int NO_LIMITATION = 3;

    @Id
    private String  id;
    @Valid
    @NotNull
    private String  userId;
    @NotEmpty
    private String  name;
    @NotNull
    private long    date;
    @NotNull
    private int     setsPerGame;
    @NotNull
    private int     pointsPerSet;
    @NotNull
    private boolean tieBreakInLastSet;
    @NotNull
    private int     pointsInTieBreak;
    @NotNull
    private boolean twoPointsDifference;
    @NotNull
    private boolean sanctions;
    @NotNull
    private boolean teamTimeouts;
    @NotNull
    private int     teamTimeoutsPerSet;
    @NotNull
    private int     teamTimeoutDuration;
    @NotNull
    private boolean technicalTimeouts;
    @NotNull
    private int     technicalTimeoutDuration;
    @NotNull
    private boolean gameIntervals;
    @NotNull
    private int     gameIntervalDuration;
    @NotNull
    private int     substitutionsLimitation;
    @NotNull
    private int     teamSubstitutionsPerSet;
    @NotNull
    private boolean beachCourtSwitches;
    @NotNull
    private int     beachCourtSwitchFreq;
    @NotNull
    private int     beachCourtSwitchFreqTieBreak;
    @NotNull
    private int     customConsecutiveServesPerPlayer;

    public Rules() {}

    public Rules(String userId, String name, long date, int setsPerGame, int pointsPerSet, boolean tieBreakInLastSet, int pointsInTieBreak, boolean twoPointsDifference, boolean sanctions,
                 boolean teamTimeouts, int teamTimeoutsPerSet, int teamTimeoutDuration,
                 boolean technicalTimeouts, int technicalTimeoutDuration, boolean gameIntervals, int gameIntervalDuration,
                 int substitutionsLimitation, int teamSubstitutionsPerSet,
                 boolean beachCourtSwitches, int beachCourtSwitchFreq, int beachCourtSwitchFreqTieBreak, int customConsecutiveServesPerPlayer) {
        this.userId = userId;
        this.date = date;
        this.name = name;
        this.setsPerGame = setsPerGame;
        this.pointsPerSet = pointsPerSet;
        this.tieBreakInLastSet = tieBreakInLastSet;
        this.pointsInTieBreak = pointsInTieBreak;
        this.twoPointsDifference = twoPointsDifference;
        this.sanctions = sanctions;
        this.teamTimeouts = teamTimeouts;
        this.teamTimeoutsPerSet = teamTimeoutsPerSet;
        this.teamTimeoutDuration = teamTimeoutDuration;
        this.technicalTimeouts = technicalTimeouts;
        this.technicalTimeoutDuration = technicalTimeoutDuration;
        this.gameIntervals = gameIntervals;
        this.gameIntervalDuration = gameIntervalDuration;
        this.substitutionsLimitation = substitutionsLimitation;
        this.teamSubstitutionsPerSet = teamSubstitutionsPerSet;
        this.beachCourtSwitches = beachCourtSwitches;
        this.beachCourtSwitchFreq = beachCourtSwitchFreq;
        this.beachCourtSwitchFreqTieBreak = beachCourtSwitchFreqTieBreak;
        this.customConsecutiveServesPerPlayer = customConsecutiveServesPerPlayer;

        checkSubstitutions();
    }

    public static final Rules OFFICIAL_INDOOR_RULES    = new Rules(User.VBR_USER_ID, "FIVB indoor 6x6 rules", 0L,
            5, 25, true, 15, true, true, true, 2, 30,
            true, 60, true, 180,
            FIVB_LIMITATION, 6, false, 0, 0, 9999);
    public static final Rules OFFICIAL_BEACH_RULES     = new Rules(User.VBR_USER_ID, "FIVB beach rules", 0L,
            3, 21, true, 15, true, true, true, 1, 30,
            true, 30, true, 60,
            FIVB_LIMITATION, 0, true, 7, 5, 9999);
    public static final Rules DEFAULT_INDOOR_4X4_RULES = new Rules(User.VBR_USER_ID, "Default 4x4 rules", 0L,
            5, 25, true, 15, true, true, true, 2, 30,
            true, 60, true, 180,
            NO_LIMITATION, 4, false, 0, 0, 9999);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public int getSetsPerGame() {
        return setsPerGame;
    }

    public void setSetsPerGame(int setsPerGame) {
        this.setsPerGame = setsPerGame;
    }

    public int getPointsPerSet() {
        return pointsPerSet;
    }

    public void setPointsPerSet(int pointsPerSet) {
        this.pointsPerSet = pointsPerSet;
    }

    public boolean isTieBreakInLastSet() {
        return tieBreakInLastSet;
    }

    public void setTieBreakInLastSet(boolean tieBreakInLastSet) {
        this.tieBreakInLastSet = tieBreakInLastSet;
    }

    public int getPointsInTieBreak() {
        return pointsInTieBreak;
    }

    public void setPointsInTieBreak(int pointsInTieBreak) {
        this.pointsInTieBreak = pointsInTieBreak;
    }

    public boolean isTwoPointsDifference() {
        return twoPointsDifference;
    }

    public void setTwoPointsDifference(boolean twoPointsDifference) {
        this.twoPointsDifference = twoPointsDifference;
    }

    public boolean isSanctions() {
        return sanctions;
    }

    public void setSanctions(boolean sanctions) {
        this.sanctions = sanctions;
    }

    public boolean isTeamTimeouts() {
        return teamTimeouts;
    }

    public void setTeamTimeouts(boolean teamTimeouts) {
        this.teamTimeouts = teamTimeouts;
    }

    public int getTeamTimeoutsPerSet() {
        return teamTimeoutsPerSet;
    }

    public void setTeamTimeoutsPerSet(int teamTimeoutsPerSet) {
        this.teamTimeoutsPerSet = teamTimeoutsPerSet;
    }

    public int getTeamTimeoutDuration() {
        return teamTimeoutDuration;
    }

    public void setTeamTimeoutDuration(int teamTimeoutDuration) {
        this.teamTimeoutDuration = teamTimeoutDuration;
    }

    public boolean isTechnicalTimeouts() {
        return technicalTimeouts;
    }

    public void setTechnicalTimeouts(boolean technicalTimeouts) {
        this.technicalTimeouts = technicalTimeouts;
    }

    public int getTechnicalTimeoutDuration() {
        return technicalTimeoutDuration;
    }

    public void setTechnicalTimeoutDuration(int technicalTimeoutDuration) {
        this.technicalTimeoutDuration = technicalTimeoutDuration;
    }

    public boolean isGameIntervals() {
        return gameIntervals;
    }

    public void setGameIntervals(boolean gameIntervals) {
        this.gameIntervals = gameIntervals;
    }

    public int getGameIntervalDuration() {
        return gameIntervalDuration;
    }

    public void setGameIntervalDuration(int gameIntervalDuration) {
        this.gameIntervalDuration = gameIntervalDuration;
    }

    public int getSubstitutionsLimitation() {
        return substitutionsLimitation;
    }

    public void setSubstitutionsLimitation(int substitutionsLimitation) {
        this.substitutionsLimitation = substitutionsLimitation;
    }

    public int getTeamSubstitutionsPerSet() {
        return teamSubstitutionsPerSet;
    }

    public void setTeamSubstitutionsPerSet(int teamSubstitutionsPerSet) {
        this.teamSubstitutionsPerSet = teamSubstitutionsPerSet;
    }

    public boolean isBeachCourtSwitches() {
        return beachCourtSwitches;
    }

    public void setBeachCourtSwitches(boolean beachCourtSwitches) {
        this.beachCourtSwitches = beachCourtSwitches;
    }

    public int getBeachCourtSwitchFreq() {
        return beachCourtSwitchFreq;
    }

    public void setBeachCourtSwitchFreq(int beachCourtSwitchFreq) {
        this.beachCourtSwitchFreq = beachCourtSwitchFreq;
    }

    public int getBeachCourtSwitchFreqTieBreak() {
        return beachCourtSwitchFreqTieBreak;
    }

    public void setBeachCourtSwitchFreqTieBreak(int beachCourtSwitchFreqTieBreak) {
        this.beachCourtSwitchFreqTieBreak = beachCourtSwitchFreqTieBreak;
    }

    public int getCustomConsecutiveServesPerPlayer() {
        return customConsecutiveServesPerPlayer;
    }

    public void setCustomConsecutiveServesPerPlayer(int customConsecutiveServesPerPlayer) {
        this.customConsecutiveServesPerPlayer = customConsecutiveServesPerPlayer;
    }

    private void checkSubstitutions() {
        if (FIVB_LIMITATION == substitutionsLimitation && teamSubstitutionsPerSet > 12) {
            teamSubstitutionsPerSet = 12;
        }
    }


}
