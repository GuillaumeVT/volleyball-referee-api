package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesDescription;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface RulesService {

    List<RulesDescription> listRules(String userId);

    List<RulesDescription> listRulesOfKind(String userId, GameType kind);

    Rules getRules(String userId, UUID rulesId) throws NotFoundException;

    RulesDescription getDefaultRules(GameType kind);

    Count getNumberOfRules(String userId);

    void createRules(String userId, Rules rules) throws ConflictException;

    void updateRules(String userId, Rules rules) throws NotFoundException;

    void deleteRules(String userId, UUID rulesId) throws ConflictException;

    void deleteAllRules(String userId);
}
