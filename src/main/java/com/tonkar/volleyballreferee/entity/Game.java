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
    private Team           homeTeam;
    @Valid
    @NotNull
    private Team           guestTeam;
    private int            homeSets;
    private int            guestSets;
    @NotNull
    private List<Set>      sets;
    @NotNull
    private List<Sanction> homeCards;
    @NotNull
    private List<Sanction> guestCards;
    @Valid
    @NotNull
    private Rules          rules;

    public boolean isStartingLineupConfirmed(int setIndex) {
        Set set = getSets().get(setIndex);
        return set.getHomeStartingPlayers().isFilled() && set.getGuestStartingPlayers().isFilled();
    }

    public boolean hasSubstitutions(int setIndex) {
        Set set = getSets().get(setIndex);
        return !set.getHomeSubstitutions().isEmpty() || !set.getGuestSubstitutions().isEmpty();
    }

    public boolean hasTimeouts(int setIndex) {
        Set set = getSets().get(setIndex);
        return !set.getHomeCalledTimeouts().isEmpty() || !set.getGuestCalledTimeouts().isEmpty();
    }

    public boolean hasSanctions(int setIndex) {
        boolean found = false;

        Iterator<Sanction> sanctionsIt = getHomeCards().iterator();

        while (!found && sanctionsIt.hasNext()) {
            Sanction sanction = sanctionsIt.next();
            found = sanction.getSet() == setIndex;
        }

        sanctionsIt = getGuestCards().iterator();

        while (!found && sanctionsIt.hasNext()) {
            Sanction sanction = sanctionsIt.next();
            found = sanction.getSet() == setIndex;
        }

        return found;
    }

    public String getTeamColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getColor() : getGuestTeam().getColor();
    }

    public String getLiberoColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getLiberoColor() : getGuestTeam().getLiberoColor();
    }

    public List<Player> getPlayers(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getPlayers() : getGuestTeam().getPlayers();
    }

    public List<Player> getLiberos(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getLiberos() : getGuestTeam().getLiberos();
    }

    public boolean isLibero(TeamType teamType, int player) {
        return getLiberos(teamType).stream().anyMatch(libero -> libero.getNum() == player);
    }

    public int getCaptain(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getCaptain() : getGuestTeam().getCaptain();
    }

    public List<Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHomeSubstitutions() : set.getGuestSubstitutions();
    }

    public List<Timeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHomeCalledTimeouts() : set.getGuestCalledTimeouts();
    }

    public List<Sanction> getGivenSanctions(TeamType teamType, int setIndex) {
        List<Sanction> allSanctions = TeamType.HOME.equals(teamType) ? getHomeCards() : getGuestCards();
        return allSanctions.stream().filter(sanction -> sanction.getSet() == setIndex).collect(Collectors.toList());
    }

    public Court getStartingLineup(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHomeStartingPlayers() : set.getGuestStartingPlayers();
    }
}
