package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.*;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeagueService {

    private final LeagueDao leagueDao;
    private final GameDao   gameDao;

    public League getLeague(UUID leagueId) {
        return leagueDao
                .findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find league %s", leagueId)));
    }

    public List<LeagueSummaryDto> listLeagues(User user, Set<GameType> kinds) {
        return leagueDao.listLeagues(user.getId(), kinds);
    }

    public List<LeagueSummaryDto> listLeaguesOfKind(User user, GameType kind) {
        return leagueDao.listLeaguesOfKind(user.getId(), kind);
    }

    public League getLeague(User user, UUID leagueId) {
        return leagueDao
                .findByIdAndCreatedBy(leagueId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find league %s for user %s", leagueId,
                                                                             user.getId())));
    }

    public CountDto getNumberOfLeagues(User user) {
        return new CountDto(leagueDao.countByCreatedBy(user.getId()));
    }

    public void createLeague(User user, League league) {
        if (leagueDao.existsById(league.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not create league %s for user %s because it already exists",
                                                            league.getId(), user.getId()));
        } else if (leagueDao.existsByCreatedByAndNameAndKind(user.getId(), league.getName(), league.getKind())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not create league %s %s for user %s because it already exists",
                                                            league.getName(), league.getKind(), user.getId()));
        } else {
            league.setCreatedBy(user.getId());
            league.setUpdatedAt(Instant.now().toEpochMilli());
            leagueDao.save(league);
        }
    }

    public void updateDivisions(User user, UUID leagueId) {
        League savedLeague = leagueDao
                .findByIdAndCreatedBy(leagueId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find league %s for user %s", leagueId,
                                                                             user.getId())));

        savedLeague.setDivisions(gameDao.listDivisionsInLeague(user.getId(), savedLeague.getId()));
        savedLeague.setUpdatedAt(Instant.now().toEpochMilli());
        leagueDao.save(savedLeague);
    }

    public void deleteLeague(User user, UUID leagueId) {
        if (gameDao.existsByCreatedByAndLeague_IdAndStatus(user.getId(), leagueId, GameStatus.SCHEDULED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not delete league %s for user %s because it is used in a game", leagueId,
                                                            user.getId()));
        } else {
            leagueDao.deleteByIdAndCreatedBy(leagueId, user.getId());
        }
    }

    public void deleteAllLeagues(User user) {
        leagueDao.listLeagues(user.getId(), Set.of(GameType.values())).forEach(leagueSummary -> {
            if (!gameDao.existsByCreatedByAndLeague_IdAndStatus(user.getId(), leagueSummary.getId(), GameStatus.SCHEDULED)) {
                leagueDao.deleteByIdAndCreatedBy(leagueSummary.getId(), user.getId());
            }
        });
    }

}