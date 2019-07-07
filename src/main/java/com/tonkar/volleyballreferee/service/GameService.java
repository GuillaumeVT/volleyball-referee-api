package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameIngredients;
import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.dto.Ranking;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GameService {

    // Public

    List<GameSummary> listLiveGames();

    List<GameSummary> listGamesMatchingToken(String token);

    List<GameSummary> listGamesWithScheduleDate(LocalDate date);

    List<GameSummary> listGamesInLeague(UUID leagueId);

    List<GameSummary> listGamesOfTeamInLeague(UUID leagueId, UUID teamId);

    List<GameSummary> listLiveGamesInLeague(UUID leagueId);

    List<GameSummary> listLast10GamesInLeague(UUID leagueId);

    List<GameSummary> listNext10GamesInLeague(UUID leagueId);

    List<GameSummary> listGamesInDivision(UUID leagueId, String divisionName);

    List<GameSummary> listGamesOfTeamInDivision(UUID leagueId, String divisionName, UUID teamId);

    List<GameSummary> listLiveGamesInDivision(UUID leagueId, String divisionName);

    List<GameSummary> listLast10GamesInDivision(UUID leagueId, String divisionName);

    List<GameSummary> listNext10GamesInDivision(UUID leagueId, String divisionName);

    Game getGame(UUID gameId) throws NotFoundException;

    FileWrapper getScoreSheet(UUID gameId) throws NotFoundException;

    FileWrapper listGamesInDivisionExcel(UUID leagueId, String divisionName) throws IOException;

    List<Ranking> listRankingsInDivision(UUID leagueId, String divisionName);

    // User only

    List<GameSummary> listGames(User user);

    List<GameSummary> listGamesWithStatus(User user, GameStatus status);

    List<GameSummary> listAvailableGames(User user);

    List<GameSummary> listCompletedGames(User user);

    List<GameSummary> listGamesInLeague(User user, UUID leagueId);

    Game getGame(User user, UUID gameId) throws NotFoundException;

    GameIngredients getGameIngredientsOfKind(User user, GameType kind);

    Count getNumberOfGames(User user);

    Count getNumberOfGamesInLeague(User user, UUID leagueId);

    Count getNumberOfAvailableGames(User user);

    void createGame(User user, GameSummary gameSummary) throws ConflictException, NotFoundException;

    void createGame(User user, Game game) throws ConflictException, NotFoundException;

    void updateGame(User user, GameSummary gameSummary) throws ConflictException, NotFoundException;

    void updateGame(User user, Game game) throws NotFoundException;

    void updateSet(User user, UUID gameId, int setIndex, Set set) throws NotFoundException;

    void setIndexed(User user, UUID gameId, boolean indexed) throws NotFoundException;

    void setReferee(User user, UUID gameId, String refereeUserId) throws NotFoundException;

    void deleteGame(User user, UUID gameId);

    void deleteAllGames(User user);

    void deleteOldLiveGames(int daysAgo);

    void deleteOldScheduledGames(int daysAgo);

}
