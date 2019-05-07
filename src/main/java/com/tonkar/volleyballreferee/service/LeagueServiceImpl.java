package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dao.LeagueDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.LeagueDescription;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.GameRepository;
import com.tonkar.volleyballreferee.repository.LeagueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class LeagueServiceImpl implements LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private LeagueDao leagueDao;

    @Autowired
    private GameDao gameDao;

    @Override
    public League getLeague(UUID leagueId) throws NotFoundException {
        return leagueRepository
                .findById(leagueId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find league %s", leagueId)));
    }

    @Override
    public List<LeagueDescription> listLeagues(String userId) {
        return leagueDao.listLeagues(userId);
    }

    @Override
    public List<LeagueDescription> listLeaguesOfKind(String userId, GameType kind) {
        return leagueDao.listLeaguesOfKind(userId, kind);
    }

    @Override
    public League getLeague(String userId, UUID leagueId) throws NotFoundException {
        return leagueRepository
                .findByIdAndCreatedBy(leagueId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find league %s for user %s", leagueId, userId)));
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
            league.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            leagueRepository.save(league);
        }
    }

    @Override
    public void updateDivisions(String userId, UUID leagueId) throws NotFoundException {
        League savedLeague = leagueRepository
                .findByIdAndCreatedBy(leagueId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find league %s for user %s", leagueId, userId)));

        savedLeague.setDivisions(gameDao.listDivisionsInLeague(userId, savedLeague.getId()));
        savedLeague.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        leagueRepository.save(savedLeague);
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