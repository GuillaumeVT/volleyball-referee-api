package com.tonkar.volleyballreferee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Document(collection="games")
public class Game {

    @Id
    private String         id;
    @Valid
    @NotNull
    private String         userId;
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
    private boolean        indexed;
    @NotNull
    private String         referee;
    @NotNull
    private String         league;
    @NotNull
    private String         division;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
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

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
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

    public boolean isStartingLineupConfirmed(int setIndex) {
        Set set = getSets().get(setIndex);
        return !set.gethStartingPlayers().isEmpty() && !set.getgStartingPlayers().isEmpty();
    }

    public boolean hasSubstitutions(int setIndex) {
        Set set = getSets().get(setIndex);
        return !set.gethSubstitutions().isEmpty() || !set.getgSubstitutions().isEmpty();
    }

    public boolean hasTimeouts(int setIndex) {
        Set set = getSets().get(setIndex);
        return !set.gethCalledTimeouts().isEmpty() || !set.getgCalledTimeouts().isEmpty();
    }

    public boolean hasSanctions(int setIndex) {
        boolean found = false;

        Iterator<Sanction> sanctionsIt = gethCards().iterator();

        while (!found && sanctionsIt.hasNext()) {
            Sanction sanction = sanctionsIt.next();
            found = sanction.getSet() == setIndex;
        }

        sanctionsIt = getgCards().iterator();

        while (!found && sanctionsIt.hasNext()) {
            Sanction sanction = sanctionsIt.next();
            found = sanction.getSet() == setIndex;
        }

        return found;
    }

    public String getTeamColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? gethTeam().getColor() : getgTeam().getColor();
    }

    public String getLiberoColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? gethTeam().getLiberoColor() : getgTeam().getLiberoColor();
    }

    public List<Integer> getPlayers(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? gethTeam().getPlayers() : getgTeam().getPlayers();
    }

    public List<Integer> getLiberos(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? gethTeam().getLiberos() : getgTeam().getLiberos();
    }

    public boolean isLibero(TeamType teamType, int player) {
        return getLiberos(teamType).contains(player);
    }

    public int getCaptain(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? gethTeam().getCaptain() : getgTeam().getCaptain();
    }

    public List<Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.gethSubstitutions() : set.getgSubstitutions();
    }

    public List<Timeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.gethCalledTimeouts() : set.getgCalledTimeouts();
    }

    public List<Sanction> getGivenSanctions(TeamType teamType, int setIndex) {
        List<Sanction> sanctions = new ArrayList<>();
        List<Sanction> allSanctions = TeamType.HOME.equals(teamType) ? gethCards() : getgCards();

        for (Sanction sanction: allSanctions) {
            if (sanction.getSet() == setIndex) {
                sanctions.add(sanction);
            }
        }

        return sanctions;
    }

    public int getPlayerAtPositionInStartingLineup(TeamType teamType, int position, int setIndex) {
        Set set = getSets().get(setIndex);
        List<Player> startingLineup = TeamType.HOME.equals(teamType) ? set.gethStartingPlayers() : set.getgStartingPlayers();

        int number = -1;

        for (Player player: startingLineup) {
            if (player.getPos() == position) {
                number = player.getNum();
            }
        }

        return number;
    }
}
