package com.tonkar.volleyballreferee.entity;

import com.tonkar.volleyballreferee.dto.LeagueSummary;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "games")
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
    @Valid
    private SelectedLeague league;
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
    @NotNull
    private String         score;
    private long           startTime;
    private long           endTime;
    private String         referee1;
    private String         referee2;
    private String         scorer;

    public boolean isStartingLineupConfirmed(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHomeStartingPlayers().isFilled(kind) : set.getGuestStartingPlayers().isFilled(kind);
    }

    public String getTeamColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getColor() : getGuestTeam().getColor();
    }

    public String getLiberoColor(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getLiberoColor() : getGuestTeam().getLiberoColor();
    }

    public List<Team.Player> getPlayers(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getPlayers() : getGuestTeam().getPlayers();
    }

    public List<Team.Player> getLiberos(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getLiberos() : getGuestTeam().getLiberos();
    }

    public boolean isLibero(TeamType teamType, int player) {
        return getLiberos(teamType).stream().anyMatch(libero -> libero.getNum() == player);
    }

    public int getCaptain(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam().getCaptain() : getGuestTeam().getCaptain();
    }

    public int getPoints(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return set.getPoints(teamType);
    }

    public List<Set.Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHomeSubstitutions() : set.getGuestSubstitutions();
    }

    public List<Set.Timeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHomeCalledTimeouts() : set.getGuestCalledTimeouts();
    }

    public List<Sanction> getGivenSanctions(TeamType teamType, int setIndex) {
        List<Sanction> allSanctions = TeamType.HOME.equals(teamType) ? getHomeCards() : getGuestCards();
        return allSanctions.stream().filter(sanction -> sanction.getSet() == setIndex).collect(Collectors.toList());
    }

    public Set.Court getStartingLineup(TeamType teamType, int setIndex) {
        Set set = getSets().get(setIndex);
        return TeamType.HOME.equals(teamType) ? set.getHomeStartingPlayers() : set.getGuestStartingPlayers();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Sanction {
        @NotBlank
        private String card;
        private int    num;
        private int    set;
        private int    homePoints;
        private int    guestPoints;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class SelectedLeague extends LeagueSummary {
        @NotBlank
        private String division;
    }
}
