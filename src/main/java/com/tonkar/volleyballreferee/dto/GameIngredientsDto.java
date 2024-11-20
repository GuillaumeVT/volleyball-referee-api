package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.*;

import java.util.List;

public record GameIngredientsDto(GameType kind, List<User.Friend> friends, RulesSummaryDto defaultRules, List<RulesSummaryDto> rules,
                                 List<TeamSummaryDto> teams, List<LeagueSummaryDto> leagues) {}
