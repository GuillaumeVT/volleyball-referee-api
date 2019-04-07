package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.GameRepository;
import com.tonkar.volleyballreferee.repository.LeagueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LeagueServiceImpl implements LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameDao gameDao;

    @Override
    public League getLeague(UUID leagueId) throws NotFoundException {
        Optional<League> optLeague = leagueRepository.findById(leagueId);
        if (optLeague.isPresent()) {
            return optLeague.get();
        } else {
            throw new NotFoundException(String.format("Could not find league %s", leagueId));
        }
    }

    @Override
    public List<League> listLeagues(String userId) {
        return leagueRepository.findByCreatedByOrderByNameAsc(userId);
    }

    @Override
    public List<League> listLeaguesOfKind(String userId, GameType kind) {
        return leagueRepository.findByCreatedByAndKindOrderByNameAsc(userId, kind);
    }

    @Override
    public League getLeague(String userId, UUID leagueId) throws NotFoundException {
        Optional<League> optLeague = leagueRepository.findByIdAndCreatedBy(leagueId, userId);
        if (optLeague.isPresent()) {
            return optLeague.get();
        } else {
            throw new NotFoundException(String.format("Could not find league %s for user %s", leagueId, userId));
        }
    }

    @Override
    public Count getNumberOfLeagues(String userId) {
        return new Count(leagueRepository.countByCreatedBy(userId));
    }

    @Override
    public void createLeague(String userId, League league) throws ConflictException {
        if (leagueRepository.existsById(league.getId())) {
            throw new ConflictException(String.format("Could not create league %s for user %s because it already exists", league.getId(), userId));
        } else if (leagueRepository.existsByCreatedByAndNameAndKind(userId, league.getName(), league.getKind())) {
            throw new ConflictException(String.format("Could not create league %s %s for user %s because it already exists", league.getName(), league.getKind(), userId));
        } else {
            league.setCreatedBy(userId);
            leagueRepository.save(league);
        }
    }

    @Override
    public void updateDivisions(String userId, UUID leagueId) throws NotFoundException {
        Optional<League> optSavedLeague = leagueRepository.findByIdAndCreatedBy(leagueId, userId);

        if (optSavedLeague.isPresent()) {
            League savedLeague = optSavedLeague.get();
            savedLeague.setDivisions(gameDao.listDivisionsInLeague(userId, savedLeague.getId()));
            leagueRepository.save(savedLeague);
        } else {
            throw new NotFoundException(String.format("Could not find league %s for user %s", leagueId, userId));
        }
    }

    @Override
    public void deleteLeague(String userId, UUID leagueId) throws ConflictException {
        if (gameRepository.existsByCreatedByAndLeagueIdAndStatus(userId, leagueId, GameStatus.SCHEDULED)) {
            throw new ConflictException(String.format("Could not delete league %s for user %s because it is used in a game", leagueId, userId));
        } else {
            leagueRepository.deleteByIdAndCreatedBy(leagueId, userId);
        }
    }

}