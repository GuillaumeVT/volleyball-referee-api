package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.RulesDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesDescription;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.GameRepository;
import com.tonkar.volleyballreferee.repository.RulesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RulesServiceImpl implements RulesService {

    @Autowired
    private RulesRepository rulesRepository;

    @Autowired
    private RulesDao rulesDao;

    @Autowired
    private GameRepository gameRepository;

    @Override
    public List<RulesDescription> listRules(String userId) {
        return rulesDao.listRules(userId);
    }

    @Override
    public List<RulesDescription> listRulesOfKind(String userId, GameType kind) {
        return rulesDao.listRulesOfKind(userId, kind);
    }

    @Override
    public Rules getRules(String userId, UUID rulesId) throws NotFoundException {
        Optional<Rules> optRules = rulesRepository.findByIdAndCreatedBy(rulesId, userId);
        if (optRules.isPresent()) {
            return optRules.get();
        } else {
            throw new NotFoundException(String.format("Could not find rules %s for user %s", rulesId, userId));
        }
    }

    @Override
    public RulesDescription getDefaultRules(GameType kind) {
        final Rules rules;

        switch (kind) {
            case INDOOR:
                rules = Rules.OFFICIAL_INDOOR_RULES;
                break;
            case INDOOR_4X4:
                rules = Rules.DEFAULT_INDOOR_4X4_RULES;
                break;
            case BEACH:
                rules = Rules.OFFICIAL_BEACH_RULES;
                break;
            default:
                rules = null;
                break;
        }

        RulesDescription rulesDescription = new RulesDescription();
        rulesDescription.setId(rules.getId());
        rulesDescription.setCreatedBy(rules.getCreatedBy());
        rulesDescription.setCreatedAt(rules.getCreatedAt());
        rulesDescription.setUpdatedAt(rules.getUpdatedAt());
        rulesDescription.setKind(rules.getKind());
        rulesDescription.setName(rules.getName());

        return rulesDescription;
    }

    @Override
    public Count getNumberOfRules(String userId) {
        return new Count(rulesRepository.countByCreatedBy(userId));
    }

    @Override
    public void createRules(String userId, Rules rules) throws ConflictException {
        if (Rules.getDefaultRules(rules.getId(), rules.getKind()).isPresent()) {
            throw new ConflictException(String.format("Could not create rules %s for user %s because they are default rules", rules.getId(), userId));
        } else if (rulesRepository.existsById(rules.getId())) {
            throw new ConflictException(String.format("Could not create rules %s for user %s because they already exist", rules.getId(), userId));
        } else if (rulesRepository.existsByCreatedByAndNameAndKind(userId, rules.getName(), rules.getKind())) {
            throw new ConflictException(String.format("Could not create rules %s %s for user %s because they already exist", rules.getName(), rules.getKind(), userId));
        } else {
            rules.setCreatedBy(userId);
            rulesRepository.save(rules);
        }
    }

    @Override
    public void updateRules(String userId, Rules rules) throws NotFoundException {
        Optional<Rules> optSavedRules = rulesRepository.findByIdAndCreatedBy(rules.getId(), userId);

        if (optSavedRules.isPresent()) {
            Rules savedRules = optSavedRules.get();
            savedRules.setUpdatedAt(rules.getUpdatedAt());
            savedRules.setSetsPerGame(rules.getSetsPerGame());
            savedRules.setPointsPerSet(rules.getPointsPerSet());
            savedRules.setTieBreakInLastSet(rules.isTieBreakInLastSet());
            savedRules.setPointsInTieBreak(rules.getPointsInTieBreak());
            savedRules.setTwoPointsDifference(rules.isTwoPointsDifference());
            savedRules.setSanctions(rules.isSanctions());
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
            rulesRepository.save(savedRules);

            rulesDao.updateScheduledGamesWithRules(userId, savedRules);
        } else {
            throw new NotFoundException(String.format("Could not find rules %s %s for user %s", rules.getName(), rules.getKind(), userId));
        }
    }

    @Override
    public void deleteRules(String userId, UUID rulesId) throws ConflictException {
        if (gameRepository.existsByCreatedByAndRules_IdAndStatus(userId, rulesId, GameStatus.SCHEDULED)) {
            throw new ConflictException(String.format("Could not delete rules %s for user %s because they are used in a game", rulesId, userId));
        } else {
            rulesRepository.deleteByIdAndCreatedBy(rulesId, userId);
        }
    }

    @Override
    public void deleteAllRules(String userId) {
        rulesRepository.findByCreatedByOrderByNameAsc(userId).forEach(rules -> {
            if (!gameRepository.existsByCreatedByAndRules_IdAndStatus(userId, rules.getId(), GameStatus.SCHEDULED)) {
                rulesRepository.delete(rules);
            }
        });
    }
}
