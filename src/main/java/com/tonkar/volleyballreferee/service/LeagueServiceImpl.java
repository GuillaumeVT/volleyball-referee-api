package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dao.LeagueDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.LeagueSummary;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class LeagueServiceImpl implements LeagueService {

    @Autowired
    private LeagueDao leagueDao;

    @Autowired
    private GameDao gameDao;

    @Override
    public League getLeague(UUID leagueId) throws NotFoundException {
        return leagueDao
                .findById(leagueId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find league %s", leagueId)));
    }

    @Override
    public List<LeagueSummary> listLeagues(User user, List<GameType> kinds) {
        return leagueDao.listLeagues(user.getId(), kinds);
    }

    @Override
    public List<LeagueSummary> listLeaguesOfKind(User user, GameType kind) {
        return leagueDao.listLeaguesOfKind(user.getId(), kind);
    }

    @Override
    public League getLeague(User user, UUID leagueId) throws NotFoundException {
        return leagueDao
                .findByIdAndCreatedBy(leagueId, user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find league %s for user %s", leagueId, user.getId())));
    }

    @Override
    public Count getNumberOfLeagues(User user) {
        return new Count(leagueDao.countByCreatedBy(user.getId()));
    }

    @Override
    public void createLeague(User user, League league) throws ConflictException {
        if (leagueDao.existsById(league.getId())) {
            throw new ConflictException(String.format("Could not create league %s for user %s because it already exists", league.getId(), user.getId()));
        } else if (leagueDao.existsByCreatedByAndNameAndKind(user.getId(), league.getName(), league.getKind())) {
            throw new ConflictException(String.format("Could not create league %s %s for user %s because it already exists", league.getName(), league.getKind(), user.getId()));
        } else {
            league.setCreatedBy(user.getId());
            league.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            leagueDao.save(league);
        }
    }

    @Override
    public void updateDivisions(User user, UUID leagueId) throws NotFoundException {
        League savedLeague = leagueDao
                .findByIdAndCreatedBy(leagueId, user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find league %s for user %s", leagueId, user.getId())));

        savedLeague.setDivisions(gameDao.listDivisionsInLeague(user.getId(), savedLeague.getId()));
        savedLeague.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        leagueDao.save(savedLeague);
    }

    @Override
    public void deleteLeague(User user, UUID leagueId) throws ConflictException {
        if (gameDao.existsByCreatedByAndLeague_IdAndStatus(user.getId(), leagueId, GameStatus.SCHEDULED)) {
            throw new ConflictException(String.format("Could not delete league %s for user %s because it is used in a game", leagueId, user.getId()));
        } else {
            leagueDao.deleteByIdAndCreatedBy(leagueId, user.getId());
        }
    }

    @Override
    public void deleteAllLeagues(User user) {
        leagueDao.listLeagues(user.getId(), List.of(GameType.values())).forEach(leagueSummary -> {
            if (!gameDao.existsByCreatedByAndLeague_IdAndStatus(user.getId(), leagueSummary.getId(), GameStatus.SCHEDULED)) {
                leagueDao.deleteByIdAndCreatedBy(leagueSummary.getId(), user.getId());
            }
        });
    }

}