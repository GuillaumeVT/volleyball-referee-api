package com.tonkar.volleyballreferee.entity;

import com.tonkar.volleyballreferee.dto.LeagueSummary;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
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

    public Team getTeam(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam() : getGuestTeam();
    }

    public boolean isLibero(TeamType teamType, int player) {
        return getTeam(teamType).getLiberos().stream().anyMatch(libero -> libero.getNum() == player);
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
    @FieldNameConstants
    public static class SelectedLeague extends LeagueSummary {
        @NotBlank
        private String division;
    }
}
