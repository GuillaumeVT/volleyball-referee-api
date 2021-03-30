package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import com.tonkar.volleyballreferee.entity.User;

import java.util.List;
import java.util.UUID;

public interface RulesService {

    List<RulesSummary> listRules(User user, List<GameType> kinds);

    List<RulesSummary> listRulesOfKind(User user, GameType kind);

    Rules getRules(User user, UUID rulesId);

    RulesSummary getDefaultRules(GameType kind);

    Count getNumberOfRules(User user);

    void createRules(User user, Rules rules);

    void updateRules(User user, Rules rules);

    void deleteRules(User user, UUID rulesId);

    void deleteAllRules(User user);
}
