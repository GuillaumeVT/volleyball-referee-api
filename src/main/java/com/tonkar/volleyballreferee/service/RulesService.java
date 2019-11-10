package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface RulesService {

    List<RulesSummary> listRules(User user, List<GameType> kinds);

    List<RulesSummary> listRulesOfKind(User user, GameType kind);

    Rules getRules(User user, UUID rulesId) throws NotFoundException;

    RulesSummary getDefaultRules(GameType kind);

    Count getNumberOfRules(User user);

    void createRules(User user, Rules rules) throws ConflictException;

    void updateRules(User user, Rules rules) throws NotFoundException;

    void deleteRules(User user, UUID rulesId) throws ConflictException;

    void deleteAllRules(User user);
}
