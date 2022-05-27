package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dao.RulesDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesSummary;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.CloseableIterator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RulesServiceImpl implements RulesService {

    private final RulesDao rulesDao;
    private final GameDao  gameDao;

    @Override
    public List<RulesSummary> listRules(User user, List<GameType> kinds) {
        return rulesDao.listRules(user.getId(), kinds);
    }

    @Override
    public List<RulesSummary> listRulesOfKind(User user, GameType kind) {
        return rulesDao.listRulesOfKind(user.getId(), kind);
    }

    @Override
    public Rules getRules(User user, UUID rulesId) {
        return rulesDao
                .findByIdAndCreatedBy(rulesId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find rules %s for user %s", rulesId, user.getId())));
    }

    @Override
    public RulesSummary getDefaultRules(GameType kind) {
        final Rules rules = switch (kind) {
            case INDOOR -> Rules.OFFICIAL_INDOOR_RULES;
            case INDOOR_4X4 -> Rules.DEFAULT_INDOOR_4X4_RULES;
            case BEACH -> Rules.OFFICIAL_BEACH_RULES;
            case SNOW -> Rules.OFFICIAL_SNOW_RULES;
        };

        return new RulesSummary(
                rules.getId(),
                rules.getCreatedBy(),
                rules.getCreatedAt(),
                rules.getUpdatedAt(),
                rules.getName(),
                rules.getKind()
        );
    }

    @Override
    public Count getNumberOfRules(User user) {
        return new Count(rulesDao.countByCreatedBy(user.getId()));
    }

    @Override
    public void createRules(User user, Rules rules) {
        if (Rules.getDefaultRules(rules.getId(), rules.getKind()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Could not create rules %s for user %s because they are default rules", rules.getId(), user.getId()));
        } else if (rulesDao.existsById(rules.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Could not create rules %s for user %s because they already exist", rules.getId(), user.getId()));
        } else if (rulesDao.existsByCreatedByAndNameAndKind(user.getId(), rules.getName(), rules.getKind())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Could not create rules %s %s for user %s because they already exist", rules.getName(), rules.getKind(), user.getId()));
        } else {
            rules.setCreatedBy(user.getId());
            rules.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            rulesDao.save(rules);
        }
    }

    @Override
    public void updateRules(User user, Rules rules) {
        Rules savedRules = rulesDao
                .findByIdAndCreatedBy(rules.getId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find rules %s %s for user %s", rules.getId(), rules.getKind(), user.getId())));

        savedRules.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
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

    @Override
    public void deleteRules(User user, UUID rulesId) {
        if (gameDao.existsByCreatedByAndRules_IdAndStatus(user.getId(), rulesId, GameStatus.SCHEDULED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Could not delete rules %s for user %s because they are used in a game", rulesId, user.getId()));
        } else {
            rulesDao.deleteByIdAndCreatedBy(rulesId, user.getId());
        }
    }

    @Override
    public void deleteAllRules(User user) {
        CloseableIterator<RulesSummary> rulesStream = rulesDao.findByCreatedByOrderByNameAsc(user.getId());
        rulesStream.forEachRemaining(rules  -> {
            if (!gameDao.existsByCreatedByAndRules_IdAndStatus(user.getId(), rules.id(), GameStatus.SCHEDULED)) {
                rulesDao.deleteByIdAndCreatedBy(rules.id(), user.getId());
            }
        });
        rulesStream.close();
    }
}
