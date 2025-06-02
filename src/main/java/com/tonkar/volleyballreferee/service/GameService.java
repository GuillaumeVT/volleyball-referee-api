package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.*;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.entity.Set;
import com.tonkar.volleyballreferee.export.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final LeagueService leagueService;
    private final TeamService   teamService;
    private final RulesService  rulesService;
    private final GameDao       gameDao;
    private final TeamDao       teamDao;
    private final RulesDao      rulesDao;
    private final LeagueDao     leagueDao;
    private final UserDao       userDao;

    public Page<GameSummaryDto> listLiveGames(java.util.Set<GameType> kinds, java.util.Set<GenderType> genders, Pageable pageable) {
        return gameDao.listLiveGames(kinds, genders, pageable);
    }

    public Page<GameSummaryDto> listGamesMatchingToken(String token,
                                                       java.util.Set<GameStatus> statuses,
                                                       java.util.Set<GameType> kinds,
                                                       java.util.Set<GenderType> genders,
                                                       Pageable pageable) {
        return gameDao.listGamesMatchingToken(token, statuses, kinds, genders, pageable);
    }

    public Page<GameSummaryDto> listGamesWithScheduleDate(LocalDate date,
                                                          java.util.Set<GameStatus> statuses,
                                                          java.util.Set<GameType> kinds,
                                                          java.util.Set<GenderType> genders,
                                                          Pageable pageable) {
        return gameDao.listGamesWithScheduleDate(date, statuses, kinds, genders, pageable);
    }

    public Page<GameSummaryDto> listGamesInLeague(UUID leagueId,
                                                  java.util.Set<GameStatus> statuses,
                                                  java.util.Set<GenderType> genders,
                                                  Pageable pageable) {
        return gameDao.listGamesInLeague(leagueId, statuses, genders, pageable);
    }

    public Page<GameSummaryDto> listGamesOfTeamInLeague(UUID leagueId, UUID teamId, java.util.Set<GameStatus> statuses, Pageable pageable) {
        return gameDao.listGamesOfTeamInLeague(leagueId, teamId, statuses, pageable);
    }

    public LeagueDashboardDto getGamesInLeagueGroupedByStatus(UUID leagueId) {
        return gameDao.findGamesInLeagueGroupedByStatus(leagueId);
    }

    public List<GameSummaryDto> listLiveGamesInLeague(UUID leagueId) {
        return gameDao.listLiveGamesInLeague(leagueId);
    }

    public List<GameSummaryDto> listLast10GamesInLeague(UUID leagueId) {
        return gameDao.listLast10GamesInLeague(leagueId);
    }

    public List<GameSummaryDto> listNext10GamesInLeague(UUID leagueId) {
        return gameDao.listNext10GamesInLeague(leagueId);
    }

    public Page<GameSummaryDto> listGamesInDivision(UUID leagueId,
                                                    String divisionName,
                                                    java.util.Set<GameStatus> statuses,
                                                    java.util.Set<GenderType> genders,
                                                    Pageable pageable) {
        return gameDao.listGamesInDivision(leagueId, divisionName, statuses, genders, pageable);
    }

    public Page<GameSummaryDto> listGamesOfTeamInDivision(UUID leagueId,
                                                          String divisionName,
                                                          UUID teamId,
                                                          java.util.Set<GameStatus> statuses,
                                                          Pageable pageable) {
        return gameDao.listGamesOfTeamInDivision(leagueId, divisionName, teamId, statuses, pageable);
    }

    public LeagueDashboardDto getGamesInDivisionGroupedByStatus(UUID leagueId, String divisionName) {
        return gameDao.findGamesInDivisionGroupedByStatus(leagueId, divisionName);
    }

    public List<GameSummaryDto> listLiveGamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listLiveGamesInDivision(leagueId, divisionName);
    }

    public List<GameSummaryDto> listLast10GamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listLast10GamesInDivision(leagueId, divisionName);
    }

    public List<GameSummaryDto> listNext10GamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listNext10GamesInDivision(leagueId, divisionName);
    }

    public Game getGame(UUID gameId) {
        return gameDao
                .findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find game %s", gameId)));
    }

    public FileWrapper getScoreSheet(UUID gameId) {
        Game game = getGame(gameId);
        return ScoreSheetWriter.createScoreSheet(game);
    }

    public FileWrapper listGamesInDivisionExcel(UUID leagueId, String divisionName) throws IOException {
        List<GameScoreDto> games = gameDao.findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(leagueId, divisionName,
                                                                                                           GameStatus.COMPLETED);
        return ExcelDivisionWriter.writeExcelDivision(divisionName, games);
    }

    public List<RankingDto> listRankingsInDivision(UUID leagueId, String divisionName) {
        List<GameScoreDto> games = gameDao.findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(leagueId, divisionName,
                                                                                                           GameStatus.COMPLETED);
        var rankings = new Rankings();
        games.forEach(rankings::addGame);
        return rankings.list();
    }

    public Page<GameSummaryDto> listGames(User user,
                                          java.util.Set<GameStatus> statuses,
                                          java.util.Set<GameType> kinds,
                                          java.util.Set<GenderType> genders,
                                          Pageable pageable) {
        return gameDao.listGames(user.getId(), statuses, kinds, genders, pageable);
    }

    public List<GameSummaryDto> listAvailableGames(User user) {
        return gameDao.listAvailableGames(user.getId());
    }

    public Page<GameSummaryDto> listCompletedGames(User user, Pageable pageable) {
        return gameDao.listCompletedGames(user.getId(), pageable);
    }

    public Page<GameSummaryDto> listGamesInLeague(User user,
                                                  UUID leagueId,
                                                  java.util.Set<GameStatus> statuses,
                                                  java.util.Set<GenderType> genders,
                                                  Pageable pageable) {
        return gameDao.listGamesInLeague(user.getId(), leagueId, statuses, genders, pageable);
    }

    public Game getGame(User user, UUID gameId) {
        return gameDao
                .findByIdAndAllowedUser(gameId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find game %s for user %s", gameId, user.getId())));
    }

    public GameIngredientsDto getGameIngredientsOfKind(User user, GameType kind) {
        return new GameIngredientsDto(kind, user.getFriends(), rulesService.getDefaultRules(kind), rulesService.listRulesOfKind(user, kind),
                                      teamService.listTeamsOfKind(user, kind), leagueService.listLeaguesOfKind(user, kind));
    }

    public CountDto getNumberOfGames(User user) {
        return new CountDto(gameDao.countByCreatedBy(user.getId()));
    }

    public CountDto getNumberOfGamesInLeague(User user, UUID leagueId) {
        return new CountDto(gameDao.countByCreatedByAndLeague_Id(user.getId(), leagueId));
    }

    public CountDto getNumberOfAvailableGames(User user) {
        return new CountDto(gameDao.countByAllowedUserAndStatusNot(user.getId(), GameStatus.COMPLETED));
    }

    public void createGame(User user, GameSummaryDto gameSummary) {
        if (gameDao.existsById(gameSummary.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not create game %s for user %s because it already exists",
                                                            gameSummary.getId(), user.getId()));
        } else if (gameSummary.getHomeTeamId().equals(gameSummary.getGuestTeamId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(
                    "Could not create game %s for user %s because team %s cannot play against itself", gameSummary.getId(), user.getId(),
                    gameSummary.getHomeTeamId()));
        } else if (!gameSummary.getCreatedBy().equals(gameSummary.getRefereedBy()) && !userDao.areFriends(gameSummary.getCreatedBy(),
                                                                                                          gameSummary.getRefereedBy())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                              String.format("Could not create game %s for user %s because %s and %s are not friends",
                                                            gameSummary.getId(), user.getId(), gameSummary.getCreatedBy(),
                                                            gameSummary.getRefereedBy()));
        } else {
            Team hTeam = teamDao
                    .findByIdAndCreatedByAndKind(gameSummary.getHomeTeamId(), user.getId(), gameSummary.getKind())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                   String.format("Could not find matching home team %s for user %s",
                                                                                 gameSummary.getHomeTeamId(), user.getId())));
            Team gTeam = teamDao
                    .findByIdAndCreatedByAndKind(gameSummary.getGuestTeamId(), user.getId(), gameSummary.getKind())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                   String.format("Could not find matching guest team %s for user %s",
                                                                                 gameSummary.getGuestTeamId(), user.getId())));
            Rules rules = findRules(user, gameSummary.getRulesId(), gameSummary.getKind()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find matching rules %s for user %s",
                                                                                          gameSummary.getRulesId(), user.getId())));

            Game.SelectedLeague league = findOrCreateLeague(user, gameSummary);

            Game game = new Game();

            game.setId(gameSummary.getId());
            game.setCreatedBy(gameSummary.getCreatedBy());
            game.setCreatedAt(Instant.now().toEpochMilli());
            game.setUpdatedAt(Instant.now().toEpochMilli());
            game.setScheduledAt(gameSummary.getScheduledAt());
            game.setRefereedBy(gameSummary.getRefereedBy());
            game.setRefereeName(gameSummary.getRefereeName());
            game.setKind(gameSummary.getKind());
            game.setGender(gameSummary.getGender());
            game.setUsage(gameSummary.getUsage());
            game.setStatus(GameStatus.SCHEDULED);
            game.setLeague(league);
            game.setHomeTeam(hTeam);
            game.setGuestTeam(gTeam);
            game.setHomeSets(0);
            game.setGuestSets(0);
            game.setSets(new ArrayList<>());
            game.setHomeCards(new ArrayList<>());
            game.setGuestCards(new ArrayList<>());
            game.setRules(rules);
            game.setScore("");
            game.setStartTime(0L);
            game.setEndTime(0L);
            game.setReferee1(gameSummary.getReferee1Name());
            game.setReferee2(gameSummary.getReferee2Name());
            game.setScorer(gameSummary.getScorerName());

            gameDao.save(game);

            createOrUpdateLeagueIfNeeded(user, game);
        }
    }

    public void updateGame(User user, GameSummaryDto gameSummary) {
        Optional<Game> optSavedGame = gameDao.findByIdAndAllowedUserAndStatus(gameSummary.getId(), user.getId(), GameStatus.SCHEDULED);

        if (optSavedGame.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                              String.format("Could not find game %s %s for user %s", gameSummary.getId(),
                                                            GameStatus.SCHEDULED, user.getId()));
        } else if (gameSummary.getHomeTeamId().equals(gameSummary.getGuestTeamId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(
                    "Could not create game %s for user %s because team %s cannot play against itself", gameSummary.getId(), user.getId(),
                    gameSummary.getHomeTeamId()));
        } else if (!gameSummary.getCreatedBy().equals(gameSummary.getRefereedBy()) && !userDao.areFriends(gameSummary.getCreatedBy(),
                                                                                                          gameSummary.getRefereedBy())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                              String.format("Could not create game %s for user %s because %s and %s are not friends",
                                                            gameSummary.getId(), user.getId(), gameSummary.getCreatedBy(),
                                                            gameSummary.getRefereedBy()));
        } else {
            Game savedGame = optSavedGame.get();

            Team hTeam = teamDao
                    .findByIdAndCreatedByAndKind(gameSummary.getHomeTeamId(), user.getId(), savedGame.getKind())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                   String.format("Could not find matching home team %s for user %s",
                                                                                 gameSummary.getHomeTeamId(), user.getId())));
            Team gTeam = teamDao
                    .findByIdAndCreatedByAndKind(gameSummary.getGuestTeamId(), user.getId(), savedGame.getKind())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                   String.format("Could not find matching guest team %s for user %s",
                                                                                 gameSummary.getGuestTeamId(), user.getId())));
            Rules rules = findRules(user, gameSummary.getRulesId(), savedGame.getKind()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find matching rules %s for user %s",
                                                                                          gameSummary.getRulesId(), user.getId())));

            Game.SelectedLeague league = findOrCreateLeague(user, gameSummary);

            savedGame.setUpdatedAt(Instant.now().toEpochMilli());
            savedGame.setScheduledAt(gameSummary.getScheduledAt());
            savedGame.setRefereedBy(gameSummary.getRefereedBy());
            savedGame.setRefereeName(gameSummary.getRefereeName());
            savedGame.setGender(gameSummary.getGender());
            savedGame.setUsage(gameSummary.getUsage());
            savedGame.setLeague(league);
            savedGame.setHomeTeam(hTeam);
            savedGame.setGuestTeam(gTeam);
            savedGame.setHomeSets(0);
            savedGame.setGuestSets(0);
            savedGame.setSets(new ArrayList<>());
            savedGame.setHomeCards(new ArrayList<>());
            savedGame.setGuestCards(new ArrayList<>());
            savedGame.setRules(rules);
            savedGame.setScore("");
            savedGame.setReferee1(gameSummary.getReferee1Name());
            savedGame.setReferee2(gameSummary.getReferee2Name());
            savedGame.setScorer(gameSummary.getScorerName());

            gameDao.save(savedGame);

            createOrUpdateLeagueIfNeeded(user, savedGame);
        }
    }

    public void upsertGame(User user, Game game) {
        Optional<Game> optionalGame = gameDao.findByIdAndAllowedUser(game.getId(), user.getId());

        if (optionalGame.isPresent()) {
            if (GameStatus.COMPLETED.equals(optionalGame.get().getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                                                  String.format("Could not create game %s for user %s because it already exists",
                                                                game.getId(), user.getId()));
            }

            Game savedGame = optionalGame.get();

            savedGame.setUpdatedAt(game.getUpdatedAt());
            savedGame.setStatus(game.getStatus());
            savedGame.setLeague(game.getLeague());
            savedGame.setHomeTeam(game.getHomeTeam());
            savedGame.setGuestTeam(game.getGuestTeam());
            savedGame.setHomeSets(game.getHomeSets());
            savedGame.setGuestSets(game.getGuestSets());
            savedGame.setSets(game.getSets());
            savedGame.setHomeCards(game.getHomeCards());
            savedGame.setGuestCards(game.getGuestCards());
            savedGame.setRules(game.getRules());
            savedGame.setScore(buildScore(savedGame));
            savedGame.setStartTime(game.getStartTime());
            savedGame.setEndTime(game.getEndTime());
            savedGame.setReferee1(game.getReferee1());
            savedGame.setReferee2(game.getReferee2());
            savedGame.setScorer(game.getScorer());

            gameDao.save(savedGame);

        } else if (game.getHomeTeam().getId().equals(game.getGuestTeam().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(
                    "Could not create game %s for user %s because team %s cannot play against itself", game.getId(), user.getId(),
                    game.getHomeTeam().getId()));
        } else if (!game.getCreatedBy().equals(game.getRefereedBy()) && !userDao.areFriends(game.getCreatedBy(), game.getRefereedBy())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                              String.format("Could not create game %s for user %s because %s and %s are not friends",
                                                            game.getId(), user.getId(), game.getCreatedBy(), game.getRefereedBy()));
        } else {
            game.setCreatedBy(user.getId());
            game.setCreatedAt(Instant.now().toEpochMilli());
            game.setUpdatedAt(Instant.now().toEpochMilli());
            game.getHomeTeam().setCreatedBy(user.getId());
            game.getGuestTeam().setCreatedBy(user.getId());
            game.getRules().setCreatedBy(user.getId());
            if (game.getLeague() != null) {
                game.getLeague().setCreatedBy(user.getId());
            }
            gameDao.save(game);

            createOrUpdateLeagueIfNeeded(user, game);
        }
    }

    public void updateSet(User user, UUID gameId, int setIndex, Set set) {
        Game savedGame = gameDao
                .findByIdAndAllowedUserAndStatus(gameId, user.getId(), GameStatus.LIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find game %s for user %s", gameId, user.getId())));

        if (setIndex > 0 && setIndex <= savedGame.getSets().size()) {
            savedGame.getSets().set(setIndex - 1, set);
            savedGame.setUpdatedAt(Instant.now().toEpochMilli());
            savedGame.setScore(buildScore(savedGame));
            gameDao.save(savedGame);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                              String.format("Could not find set %d of game %s for user %s", setIndex, savedGame.getId(),
                                                            user.getId()));
        }
    }

    public void setReferee(User user, UUID gameId, UUID refereeUserId) {
        Game game = gameDao
                .findByIdAndCreatedByAndStatusNot(gameId, user.getId(), GameStatus.COMPLETED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find game %s for user %s", gameId, user.getId())));
        if (user.getId().equals(refereeUserId)) {
            gameDao.updateReferee(game.getId(), user.getId(), user.getPseudo(), Instant.now().toEpochMilli());
        } else {
            User.Friend friend = user
                    .getFriend(refereeUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                   String.format("Could not find referee %s in friends for user %s", gameId,
                                                                                 user.getId())));
            gameDao.updateReferee(game.getId(), friend.getId(), friend.getPseudo(), Instant.now().toEpochMilli());
        }
    }

    public void deleteGame(User user, UUID gameId) {
        gameDao.deleteByIdAndCreatedBy(gameId, user.getId());
    }

    public void deleteAllGames(User user) {
        gameDao.deleteByCreatedByAndStatus(user.getId(), GameStatus.COMPLETED);
    }

    public void deleteAllGamesInLeague(User user, UUID leagueId) {
        gameDao.deleteByCreatedByAndStatusAndLeague_Id(user.getId(), GameStatus.COMPLETED, leagueId);
    }

    private Optional<Rules> findRules(User user, UUID rulesId, GameType kind) {
        Optional<Rules> optRules = Rules.getDefaultRules(rulesId, kind);

        if (optRules.isEmpty()) {
            optRules = rulesDao.findByIdAndCreatedByAndKind(rulesId, user.getId(), kind);
        }

        return optRules;
    }

    private Game.SelectedLeague findOrCreateLeague(User user, GameSummaryDto gameSummary) {
        Game.SelectedLeague selectedLeague = null;

        if (gameSummary.getLeagueId() != null && gameSummary.getLeagueName() != null && !gameSummary
                .getLeagueName()
                .isBlank() && gameSummary.getDivisionName() != null && !gameSummary.getDivisionName().isBlank()) {
            Optional<League> optLeague = leagueDao.findByIdAndCreatedBy(gameSummary.getLeagueId(), user.getId());
            League league;

            if (optLeague.isPresent()) {
                league = optLeague.get();
            } else {
                league = new League();
                league.setId(gameSummary.getLeagueId());
                league.setCreatedBy(user.getId());
                league.setCreatedAt(Instant.now().toEpochMilli());
                league.setUpdatedAt(Instant.now().toEpochMilli());
                league.setKind(gameSummary.getKind());
                league.setName(gameSummary.getLeagueName());
                league.setDivisions(new ArrayList<>());
                league.getDivisions().add(gameSummary.getDivisionName());

                leagueService.createLeague(user, league);
            }

            selectedLeague = buildSelectedLeague(league, gameSummary.getDivisionName());
        }

        return selectedLeague;
    }

    private void createOrUpdateLeagueIfNeeded(User user, Game game) {
        if (game.getLeague() != null) {
            try {
                leagueService.updateDivisions(user, game.getLeague().getId());
            } catch (ResponseStatusException e) {
                /* does not exist */
                Game.SelectedLeague selectedLeague = game.getLeague();
                League league = new League();
                league.setId(selectedLeague.getId());
                league.setCreatedBy(user.getId());
                league.setCreatedAt(selectedLeague.getCreatedAt());
                league.setUpdatedAt(selectedLeague.getUpdatedAt());
                league.setKind(selectedLeague.getKind());
                league.setName(selectedLeague.getName());
                league.setDivisions(new ArrayList<>());
                league.getDivisions().add(selectedLeague.getDivision());

                leagueService.createLeague(user, league);
            }
        }
    }

    private Game.SelectedLeague buildSelectedLeague(League league, String divisionName) {
        Game.SelectedLeague selectedLeague = new Game.SelectedLeague();
        selectedLeague.setId(league.getId());
        selectedLeague.setCreatedBy(league.getCreatedBy());
        selectedLeague.setCreatedAt(league.getCreatedAt());
        selectedLeague.setUpdatedAt(league.getUpdatedAt());
        selectedLeague.setKind(league.getKind());
        selectedLeague.setName(league.getName());
        selectedLeague.setDivision(divisionName);
        return selectedLeague;
    }

    private String buildScore(Game game) {
        StringBuilder scoreBuilder = new StringBuilder();

        for (Set set : game.getSets()) {
            scoreBuilder.append(String.format("%d-%d\t\t", set.getHomePoints(), set.getGuestPoints()));
        }

        return scoreBuilder.toString().trim();
    }
}
