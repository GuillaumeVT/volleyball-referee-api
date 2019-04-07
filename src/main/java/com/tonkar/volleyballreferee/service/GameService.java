package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameDescription;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.Set;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.scoresheet.ScoreSheet;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameService {

    // Public

    List<GameDescription> listLiveGames();

    List<GameDescription> listGamesMatchingToken(String token);

    List<GameDescription> listGamesWithScheduleDate(LocalDate date);

    List<GameDescription> listGamesInLeague(UUID leagueId);

    List<GameDescription> listGamesOfTeamInLeague(UUID leagueId, UUID teamId);

    List<GameDescription> listLiveGamesInLeague(UUID leagueId);

    List<GameDescription> listLast10GamesInLeague(UUID leagueId);

    List<GameDescription> listNext10GamesInLeague(UUID leagueId);

    Game getGame(UUID gameId) throws NotFoundException;

    ScoreSheet getScoreSheet(UUID gameId) throws NotFoundException;

    // User only

    List<GameDescription> listGames(String userId);

    List<GameDescription> listGamesWithStatus(String userId, GameStatus status);

    List<GameDescription> listAvailableGames(String userId);

    List<GameDescription> listGamesInLeague(String userId, UUID leagueId);

    byte[] listGamesInLeagueCsv(String userId, UUID leagueId, Optional<String> divisionName);

    Game getGame(String userId, UUID gameId) throws NotFoundException;

    Count getNumberOfGames(String userId);

    Count getNumberOfGamesInLeague(String userId, UUID leagueId);

    void createGame(String userId, GameDescription gameDescription) throws ConflictException, NotFoundException;

    void createGame(String userId, Game game) throws ConflictException, NotFoundException;

    void updateGame(String userId, GameDescription gameDescription) throws ConflictException, NotFoundException;

    void updateGame(String userId, Game game) throws NotFoundException;

    void updateSet(String userId, UUID gameId, int setIndex, Set set) throws NotFoundException;

    void deleteGame(String userId, UUID gameId);

    void deleteAllGames(String userId);

    void deleteOldLiveGames(int daysAgo);

    void deleteOldScheduledGames(int daysAgo);

}
