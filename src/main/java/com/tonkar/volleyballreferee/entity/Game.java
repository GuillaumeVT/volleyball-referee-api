package com.tonkar.volleyballreferee.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Document(collection="games")
@NoArgsConstructor @Getter @Setter
public class Game {

    @Id
    @NotNull
    private UUID           id;
    @NotBlank
    private String         createdBy;
    private long           createdAt;
    private long           updatedAt;
    private long           scheduledAt;
    @NotBlank
    private String         refereedBy;
    @NotBlank
    private String         refereeName;
    @NotNull
    private GameType       kind;
    @NotNull
    private GenderType     gender;
    @NotNull
    private UsageType      usage;
    @NotNull
    private GameStatus     status;
    private boolean        indexed;
    private UUID           leagueId;
    private String         leagueName;
    private String         divisionName;
    @Valid
    @NotNull
    private Team           hTeam;
    @Valid
    @NotNull
    private Team           gTeam;
    private int            hSets;
    private int            gSets;
    @NotNull
    private List<Set>      sets;
    @NotNull
    private List<Sanction> hCards;
    @NotNull
    private List<Sanction> gCards;
    @Valid
    @NotNull
    private Rules          rules;

    public boolean isStartingLineupConfirmed(int setIndex) {
        Set set = getSets().get(setIndex);
        return set.getHStartingPlayers().isFilled() && set.getGStartingPlayers().isFilled();
    }

    public boolean hasSubstitutions(int setIndex) {
        Set set = getSets().get(setIndex);
        return !set.getHSubstitutions().isEmpty() || !set.getGSubstitutions().isEmpty();
    }

    public boolean hasTimeouts(int setIndex) {
        Set set = getSets().get(setIndex);
        return !set.getHCalledTimeouts().isEmpty() || !set.getGCalledTimeouts().isEmpty();
    }

    public boolean hasSanctions(int setIndex) {
        boolean found = false;

        Iterator<Sanction> sanctionsIt = getHCards().iterator();

        while (!found && sanctionsIt.hasNext()) {
            Sanction sanction = sanctionsIt.next();
            found = sanction.getSet() == setIndex;
        }

        sanctionsIt = getGCards().iterator();

        while (!found && sanctionsIt.hasNext()) {
            Sanction sanction = sanctionsIt.next();
            found = sanction.getSet() == setIndex;
        }

        return found;
    }

    public String getTeamColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHTeam().getColor() : getGTeam().getColor();
    }

    public String getLiberoColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHTeam().getLiberoColor() : getGTeam().getLiberoColor();
    }

    public List<Player> getPlayers(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHTeam().getPlayers() : getGTeam().getPlayers();
    }

    public List<Player> getLiberos(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHTeam().getLiberos() : getGTeam().getLiberos();
    }

    public boolean isLibero(TeamType teamType, int player) {
        return getLiberos(teamType).stream().anyMatch(libero -> libero.getNumber() == player);
    }

    public int getCaptain(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHTeam().getCaptain() : getGTeam().getCaptain();
    }

    public List<Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHSubstitutions() : set.getGSubstitutions();
    }

    public List<Timeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHCalledTimeouts() : set.getGCalledTimeouts();
    }

    public List<Sanction> getGivenSanctions(TeamType teamType, int setIndex) {
        List<Sanction> allSanctions = TeamType.HOME.equals(teamType) ? getHCards() : getGCards();
        return allSanctions.stream().filter(sanction -> sanction.getSet() == setIndex).collect(Collectors.toList());
    }

    public Court getStartingLineup(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHStartingPlayers() : set.getGStartingPlayers();
    }
}
