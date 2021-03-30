package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameIngredients;
import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.dto.Ranking;
import com.tonkar.volleyballreferee.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GameService {

    // Public

    Page<GameSummary> listLiveGames(List<GameType> kinds, List<GenderType> genders, Pageable pageable);

    Page<GameSummary> listGamesMatchingToken(String token, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable);

    Page<GameSummary> listGamesWithScheduleDate(LocalDate date, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable);

    Page<GameSummary> listGamesInLeague(UUID leagueId, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable);

    Page<GameSummary> listGamesOfTeamInLeague(UUID leagueId, UUID teamId, List<GameStatus> statuses, Pageable pageable);

    List<GameSummary> listLiveGamesInLeague(UUID leagueId);

    List<GameSummary> listLast10GamesInLeague(UUID leagueId);

    List<GameSummary> listNext10GamesInLeague(UUID leagueId);

    Page<GameSummary> listGamesInDivision(UUID leagueId, String divisionName, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable);

    Page<GameSummary> listGamesOfTeamInDivision(UUID leagueId, String divisionName, UUID teamId, List<GameStatus> statuses, Pageable pageable);

    List<GameSummary> listLiveGamesInDivision(UUID leagueId, String divisionName);

    List<GameSummary> listLast10GamesInDivision(UUID leagueId, String divisionName);

    List<GameSummary> listNext10GamesInDivision(UUID leagueId, String divisionName);

    Game getGame(UUID gameId);

    FileWrapper getScoreSheet(UUID gameId);

    FileWrapper listGamesInDivisionExcel(UUID leagueId, String divisionName) throws IOException;

    List<Ranking> listRankingsInDivision(UUID leagueId, String divisionName);

    // User only

    Page<GameSummary> listGames(User user, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable);

    List<GameSummary> listAvailableGames(User user);

    Page<GameSummary> listCompletedGames(User user, Pageable pageable);

    Page<GameSummary> listGamesInLeague(User user, UUID leagueId, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable);

    Game getGame(User user, UUID gameId);

    GameIngredients getGameIngredientsOfKind(User user, GameType kind);

    Count getNumberOfGames(User user);

    Count getNumberOfGamesInLeague(User user, UUID leagueId);

    Count getNumberOfAvailableGames(User user);

    void createGame(User user, GameSummary gameSummary);

    void createGame(User user, Game game);

    void updateGame(User user, GameSummary gameSummary);

    void updateGame(User user, Game game);

    void updateSet(User user, UUID gameId, int setIndex, Set set);

    void setIndexed(User user, UUID gameId, boolean indexed);

    void setReferee(User user, UUID gameId, String refereeUserId);

    void deleteGame(User user, UUID gameId);

    void deleteAllGames(User user);

    void deleteAllGamesInLeague(User user, UUID leagueId);

    void deleteOldLiveGames(int daysAgo);

    void deleteOldScheduledGames(int daysAgo);
}
