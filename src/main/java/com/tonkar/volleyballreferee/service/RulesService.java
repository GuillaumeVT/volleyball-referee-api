package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.*;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RulesService {

    private final RulesDao rulesDao;
    private final GameDao  gameDao;

    public List<RulesSummaryDto> listRules(User user, java.util.Set<GameType> kinds) {
        return rulesDao.listRules(user.getId(), kinds);
    }

    public List<RulesSummaryDto> listRulesOfKind(User user, GameType kind) {
        return rulesDao.listRulesOfKind(user.getId(), kind);
    }

    public Rules getRules(User user, UUID rulesId) {
        return rulesDao
                .findByIdAndCreatedBy(rulesId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find rules %s for user %s", rulesId,
                                                                             user.getId())));
    }

    public RulesSummaryDto getDefaultRules(GameType kind) {
        final Rules rules = switch (kind) {
            case INDOOR -> Rules.OFFICIAL_INDOOR_RULES;
            case INDOOR_4X4 -> Rules.DEFAULT_INDOOR_4X4_RULES;
            case BEACH -> Rules.OFFICIAL_BEACH_RULES;
            case SNOW -> Rules.OFFICIAL_SNOW_RULES;
        };

        return new RulesSummaryDto(rules.getId(), rules.getCreatedBy(), rules.getCreatedAt(), rules.getUpdatedAt(), rules.getName(),
                                   rules.getKind());
    }

    public CountDto getNumberOfRules(User user) {
        return new CountDto(rulesDao.countByCreatedBy(user.getId()));
    }

    public void createRules(User user, Rules rules) {
        if (Rules.getDefaultRules(rules.getId(), rules.getKind()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not create rules %s for user %s because they are default rules",
                                                            rules.getId(), user.getId()));
        } else if (rulesDao.existsById(rules.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not create rules %s for user %s because they already exist",
                                                            rules.getId(), user.getId()));
        } else if (rulesDao.existsByCreatedByAndNameAndKind(user.getId(), rules.getName(), rules.getKind())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not create rules %s %s for user %s because they already exist",
                                                            rules.getName(), rules.getKind(), user.getId()));
        } else {
            rules.setCreatedBy(user.getId());
            rules.setUpdatedAt(Instant.now().toEpochMilli());
            rulesDao.save(rules);
        }
    }

    public void updateRules(User user, Rules rules) {
        Rules savedRules = rulesDao
                .findByIdAndCreatedBy(rules.getId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find rules %s %s for user %s", rules.getId(),
                                                                             rules.getKind(), user.getId())));

        savedRules.setUpdatedAt(Instant.now().toEpochMilli());
        savedRules.setName(rules.getName());
        savedRules.setSetsPerGame(rules.getSetsPerGame());
        savedRules.setPointsPerSet(rules.getPointsPerSet());
        savedRules.setTieBreakInLastSet(rules.isTieBreakInLastSet());
        savedRules.setPointsInTieBreak(rules.getPointsInTieBreak());
        savedRules.setTwoPointsDifference(rules.isTwoPointsDifference());
        savedRules.setSanctions(rules.isSanctions());
        savedRules.setMatchTermination(rules.getMatchTermination());
        savedRules.setTeamTimeouts(rules.isTeamTimeouts());
        savedRules.setTeamTimeoutsPerSet(rules.getTeamTimeoutsPerSet());
        savedRules.setTeamTimeoutDuration(rules.getTeamTimeoutDuration());
        savedRules.setTechnicalTimeouts(rules.isTechnicalTimeouts());
        savedRules.setTechnicalTimeoutDuration(rules.getTechnicalTimeoutDuration());
        savedRules.setGameIntervals(rules.isGameIntervals());
        savedRules.setGameIntervalDuration(rules.getGameIntervalDuration());
        savedRules.setSubstitutionsLimitation(rules.getSubstitutionsLimitation());
        savedRules.setTeamSubstitutionsPerSet(rules.getTeamSubstitutionsPerSet());
        savedRules.setBeachCourtSwitches(rules.isBeachCourtSwitches());
        savedRules.setBeachCourtSwitchFreq(rules.getBeachCourtSwitchFreq());
        savedRules.setBeachCourtSwitchFreqTieBreak(rules.getBeachCourtSwitchFreqTieBreak());
        savedRules.setCustomConsecutiveServesPerPlayer(rules.getCustomConsecutiveServesPerPlayer());
        rulesDao.save(savedRules);

        rulesDao.updateScheduledGamesWithRules(user.getId(), savedRules);
    }

    public void deleteRules(User user, UUID rulesId) {
        if (gameDao.existsByCreatedByAndRules_IdAndStatus(user.getId(), rulesId, GameStatus.SCHEDULED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not delete rules %s for user %s because they are used in a game",
                                                            rulesId, user.getId()));
        } else {
            rulesDao.deleteByIdAndCreatedBy(rulesId, user.getId());
        }
    }

    public void deleteAllRules(User user) {
        rulesDao.findByCreatedByOrderByNameAsc(user.getId()).forEach(rules -> {
            if (!gameDao.existsByCreatedByAndRules_IdAndStatus(user.getId(), rules.id(), GameStatus.SCHEDULED)) {
                rulesDao.deleteByIdAndCreatedBy(rules.id(), user.getId());
            }
        });
    }
}
