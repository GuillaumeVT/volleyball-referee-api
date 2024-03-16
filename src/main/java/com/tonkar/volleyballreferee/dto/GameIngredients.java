package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.*;

import java.util.List;

public record GameIngredients(GameType kind, List<User.Friend> friends, RulesSummary defaultRules, List<RulesSummary> rules,
                              List<TeamSummary> teams, List<LeagueSummary> leagues) {}
