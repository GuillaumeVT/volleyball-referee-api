package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameDescription;
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

    List<GameDescription> listLiveGames();

    List<GameDescription> listGamesMatchingToken(String token);

    List<GameDescription> listGamesWithScheduleDate(LocalDate date);

    List<GameDescription> listGamesInLeague(UUID leagueId);

    List<GameDescription> listGamesOfTeamInLeague(UUID leagueId, UUID teamId);

    List<GameDescription> listLiveGamesInLeague(UUID leagueId);

    List<GameDescription> listLast10GamesInLeague(UUID leagueId);

    List<GameDescription> listNext10GamesInLeague(UUID leagueId);

    List<GameDescription> listGamesInDivision(UUID leagueId, String divisionName);

    List<GameDescription> listGamesOfTeamInDivision(UUID leagueId, String divisionName, UUID teamId);

    List<GameDescription> listLiveGamesInDivision(UUID leagueId, String divisionName);

    List<GameDescription> listLast10GamesInDivision(UUID leagueId, String divisionName);

    List<GameDescription> listNext10GamesInDivision(UUID leagueId, String divisionName);

    Game getGame(UUID gameId) throws NotFoundException;

    FileWrapper getScoreSheet(UUID gameId) throws NotFoundException;

    FileWrapper listGamesInDivisionExcel(UUID leagueId, String divisionName) throws IOException;

    List<Ranking> listRankingsInDivision(UUID leagueId, String divisionName);

    // User only

    List<GameDescription> listGames(User user);

    List<GameDescription> listGamesWithStatus(User user, GameStatus status);

    List<GameDescription> listAvailableGames(User user);

    List<GameDescription> listCompletedGames(User user);

    List<GameDescription> listGamesInLeague(User user, UUID leagueId);

    Game getGame(User user, UUID gameId) throws NotFoundException;

    Count getNumberOfGames(User user);

    Count getNumberOfGamesInLeague(User user, UUID leagueId);

    void createGame(User user, GameDescription gameDescription) throws ConflictException, NotFoundException;

    void createGame(User user, Game game) throws ConflictException, NotFoundException;

    void updateGame(User user, GameDescription gameDescription) throws ConflictException, NotFoundException;

    void updateGame(User user, Game game) throws NotFoundException;

    void updateSet(User user, UUID gameId, int setIndex, Set set) throws NotFoundException;

    void setIndexed(User user, UUID gameId, boolean indexed) throws NotFoundException;

    void setReferee(User user, UUID gameId, String refereeUserId) throws NotFoundException;

    void deleteGame(User user, UUID gameId);

    void deleteAllGames(User user);

    void deleteOldLiveGames(int daysAgo);

    void deleteOldScheduledGames(int daysAgo);

}
