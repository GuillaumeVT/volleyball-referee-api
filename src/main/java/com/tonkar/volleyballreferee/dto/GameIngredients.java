package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class GameIngredients {
    private GameType            kind;
    private List<User.Friend>   friends;
    private RulesSummary        defaultRules;
    private List<RulesSummary>  rules;
    private List<TeamSummary>   teams;
    private List<LeagueSummary> leagues;

    public GameIngredients(GameType kind) {
        this.kind = kind;
        this.friends = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.leagues = new ArrayList<>();
    }

}
