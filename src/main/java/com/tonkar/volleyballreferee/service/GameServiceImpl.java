package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.*;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.out.ExcelDivisionWriter;
import com.tonkar.volleyballreferee.out.ScoreSheetWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private GameDao gameDao;

    @Autowired
    private TeamDao teamDao;

    @Autowired
    private RulesDao rulesDao;

    @Autowired
    private LeagueDao leagueDao;

    @Autowired
    private UserDao userDao;

    @Override
    public Page<GameSummary> listLiveGames(List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        return gameDao.listLiveGames(kinds, genders, pageable);
    }

    @Override
    public Page<GameSummary> listGamesMatchingToken(String token, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        return gameDao.listGamesMatchingToken(token, statuses, kinds, genders, pageable);
    }

    @Override
    public Page<GameSummary> listGamesWithScheduleDate(LocalDate date, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        return gameDao.listGamesWithScheduleDate(date, statuses, kinds, genders, pageable);
    }

    @Override
    public Page<GameSummary> listGamesInLeague(UUID leagueId, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        return gameDao.listGamesInLeague(leagueId, statuses, genders, pageable);
    }

    @Override
    public Page<GameSummary> listGamesOfTeamInLeague(UUID leagueId, UUID teamId, List<GameStatus> statuses, Pageable pageable) {
        return gameDao.listGamesOfTeamInLeague(leagueId, teamId, statuses, pageable);
    }

    @Override
    public List<GameSummary> listLiveGamesInLeague(UUID leagueId) {
        return gameDao.listLiveGamesInLeague(leagueId);
    }

    @Override
    public List<GameSummary> listLast10GamesInLeague(UUID leagueId) {
        return gameDao.listLast10GamesInLeague(leagueId);
    }

    @Override
    public List<GameSummary> listNext10GamesInLeague(UUID leagueId) {
        return gameDao.listNext10GamesInLeague(leagueId);
    }

    @Override
    public Page<GameSummary> listGamesInDivision(UUID leagueId, String divisionName, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        return gameDao.listGamesInDivision(leagueId, divisionName, statuses, genders, pageable);
    }

    @Override
    public Page<GameSummary> listGamesOfTeamInDivision(UUID leagueId, String divisionName, UUID teamId, List<GameStatus> statuses, Pageable pageable) {
        return gameDao.listGamesOfTeamInDivision(leagueId, divisionName, teamId, statuses, pageable);
    }

    @Override
    public List<GameSummary> listLiveGamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listLiveGamesInDivision(leagueId, divisionName);
    }

    @Override
    public List<GameSummary> listLast10GamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listLast10GamesInDivision(leagueId, divisionName);
    }

    @Override
    public List<GameSummary> listNext10GamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listNext10GamesInDivision(leagueId, divisionName);
    }

    @Override
    public Game getGame(UUID gameId) throws NotFoundException {
        return gameDao
                .findById(gameId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find game %s", gameId)));
    }

    @Override
    public FileWrapper getScoreSheet(UUID gameId) throws NotFoundException {
        Game game = getGame(gameId);
        return ScoreSheetWriter.createScoreSheet(game);
    }

    @Override
    public FileWrapper listGamesInDivisionExcel(UUID leagueId, String divisionName) throws IOException {
        List<GameScore> games = gameDao.findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(leagueId, divisionName, GameStatus.COMPLETED);
        return ExcelDivisionWriter.writeExcelDivision(divisionName, games);
    }

    @Override
    public List<Ranking> listRankingsInDivision(UUID leagueId, String divisionName) {
        List<GameScore> games = gameDao.findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(leagueId, divisionName, GameStatus.COMPLETED);
        Rankings rankings = new Rankings();
        games.forEach(rankings::addGame);
        return rankings.list();
    }

    @Override
    public Page<GameSummary> listGames(User user, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        return gameDao.listGames(user.getId(), statuses, kinds, genders, pageable);
    }

    @Override
    public List<GameSummary> listAvailableGames(User user) {
        return gameDao.listAvailableGames(user.getId());
    }

    @Override
    public Page<GameSummary> listCompletedGames(User user, Pageable pageable) {
        return gameDao.listCompletedGames(user.getId(), pageable);
    }

    @Override
    public Page<GameSummary> listGamesInLeague(User user, UUID leagueId, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        return gameDao.listGamesInLeague(user.getId(), leagueId, statuses, genders, pageable);
    }

    @Override
    public Game getGame(User user, UUID gameId) throws NotFoundException {
        return gameDao
                .findByIdAndAllowedUser(gameId, user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find game %s for user %s", gameId, user.getId())));
    }

    @Override
    public GameIngredients getGameIngredientsOfKind(User user, GameType kind) {
        GameIngredients gameIngredients = new GameIngredients(kind);
        gameIngredients.setFriends(user.getFriends());
        gameIngredients.setDefaultRules(rulesService.getDefaultRules(kind));
        gameIngredients.setRules(rulesService.listRulesOfKind(user, kind));
        gameIngredients.setTeams(teamService.listTeamsOfKind(user, kind));
        gameIngredients.setLeagues(leagueService.listLeaguesOfKind(user, kind));
        return gameIngredients;
    }

    @Override
    public Count getNumberOfGames(User user) {
        return new Count(gameDao.countByCreatedBy(user.getId()));
    }

    @Override
    public Count getNumberOfGamesInLeague(User user, UUID leagueId) {
        return new Count(gameDao.countByCreatedByAndLeague_Id(user.getId(), leagueId));
    }

    @Override
    public Count getNumberOfAvailableGames(User user) {
        return new Count(gameDao.countByAllowedUserAndStatusNot(user.getId(), GameStatus.COMPLETED));
    }

    @Override
    public void createGame(User user, GameSummary gameSummary) throws ConflictException, NotFoundException {
        if (gameDao.existsById(gameSummary.getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because it already exists", gameSummary.getId(), user.getId()));
        } else if (gameSummary.getHomeTeamId().equals(gameSummary.getGuestTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameSummary.getId(), user.getId(), gameSummary.getHomeTeamId()));
        } else if (!gameSummary.getCreatedBy().equals(gameSummary.getRefereedBy()) && !userDao.areFriends(gameSummary.getCreatedBy(), gameSummary.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameSummary.getId(), user.getId(), gameSummary.getCreatedBy(), gameSummary.getRefereedBy()));
        } else {
            Optional<Team> optHTeam = teamDao.findByIdAndCreatedByAndKind(gameSummary.getHomeTeamId(), user.getId(), gameSummary.getKind());
            Optional<Team> optGTeam = teamDao.findByIdAndCreatedByAndKind(gameSummary.getGuestTeamId(), user.getId(), gameSummary.getKind());
            Optional<Rules> optRules = findRules(user, gameSummary.getRulesId(), gameSummary.getKind());

            if (optHTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching home team %s for user %s", gameSummary.getHomeTeamId(), user.getId()));
            } else if (optGTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching guest team %s for user %s", gameSummary.getGuestTeamId(), user.getId()));
            } else if (optRules.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching rules %s for user %s", gameSummary.getRulesId(), user.getId()));
            } else {
                Game.SelectedLeague league = findOrCreateLeague(user, gameSummary);

                Game game = new Game();

                game.setId(gameSummary.getId());
                game.setCreatedBy(gameSummary.getCreatedBy());
                game.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                game.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                game.setScheduledAt(gameSummary.getScheduledAt());
                game.setRefereedBy(gameSummary.getRefereedBy());
                game.setRefereeName(gameSummary.getRefereeName());
                game.setKind(gameSummary.getKind());
                game.setGender(gameSummary.getGender());
                game.setUsage(gameSummary.getUsage());
                game.setStatus(GameStatus.SCHEDULED);
                game.setIndexed(gameSummary.isIndexed());
                game.setLeague(league);
                game.setHomeTeam(optHTeam.get());
                game.setGuestTeam(optGTeam.get());
                game.setHomeSets(0);
                game.setGuestSets(0);
                game.setSets(new ArrayList<>());
                game.setHomeCards(new ArrayList<>());
                game.setGuestCards(new ArrayList<>());
                game.setRules(optRules.get());
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
    }

    @Override
    public void createGame(User user, Game game) throws ConflictException, NotFoundException {
        if (gameDao.existsById(game.getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because it already exists", game.getId(), user.getId()));
        } else if (game.getHomeTeam().getId().equals(game.getGuestTeam().getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", game.getId(), user.getId(), game.getHomeTeam().getId()));
        } else if (!game.getCreatedBy().equals(game.getRefereedBy()) && !userDao.areFriends(game.getCreatedBy(), game.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", game.getId(), user.getId(), game.getCreatedBy(), game.getRefereedBy()));
        } else {
            game.setCreatedBy(user.getId());
            game.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            game.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
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

    @Override
    public void updateGame(User user, GameSummary gameSummary) throws ConflictException, NotFoundException {
        Optional<Game> optSavedGame = gameDao.findByIdAndAllowedUserAndStatus(gameSummary.getId(), user.getId(), GameStatus.SCHEDULED);

        if (optSavedGame.isEmpty()) {
            throw new NotFoundException(String.format("Could not find game %s %s for user %s", gameSummary.getId(), GameStatus.SCHEDULED, user.getId()));
        } else if (gameSummary.getHomeTeamId().equals(gameSummary.getGuestTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameSummary.getId(), user.getId(), gameSummary.getHomeTeamId()));
        } else if (!gameSummary.getCreatedBy().equals(gameSummary.getRefereedBy()) && !userDao.areFriends(gameSummary.getCreatedBy(), gameSummary.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameSummary.getId(), user.getId(), gameSummary.getCreatedBy(), gameSummary.getRefereedBy()));
        } else {
            Game savedGame = optSavedGame.get();

            Optional<Team> optHTeam = teamDao.findByIdAndCreatedByAndKind(gameSummary.getHomeTeamId(), user.getId(), savedGame.getKind());
            Optional<Team> optGTeam = teamDao.findByIdAndCreatedByAndKind(gameSummary.getGuestTeamId(), user.getId(), savedGame.getKind());
            Optional<Rules> optRules = findRules(user, gameSummary.getRulesId(), savedGame.getKind());

            if (optHTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching home team %s for user %s", gameSummary.getHomeTeamId(), user.getId()));
            } else if (optGTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching guest team %s for user %s", gameSummary.getGuestTeamId(), user.getId()));
            } else if (optRules.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching rules %s for user %s", gameSummary.getRulesId(), user.getId()));
            } else {
                Game.SelectedLeague league = findOrCreateLeague(user, gameSummary);

                savedGame.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                savedGame.setScheduledAt(gameSummary.getScheduledAt());
                savedGame.setRefereedBy(gameSummary.getRefereedBy());
                savedGame.setRefereeName(gameSummary.getRefereeName());
                savedGame.setGender(gameSummary.getGender());
                savedGame.setUsage(gameSummary.getUsage());
                savedGame.setIndexed(gameSummary.isIndexed());
                savedGame.setLeague(league);
                savedGame.setHomeTeam(optHTeam.get());
                savedGame.setGuestTeam(optGTeam.get());
                savedGame.setHomeSets(0);
                savedGame.setGuestSets(0);
                savedGame.setSets(new ArrayList<>());
                savedGame.setHomeCards(new ArrayList<>());
                savedGame.setGuestCards(new ArrayList<>());
                savedGame.setRules(optRules.get());
                savedGame.setScore("");
                savedGame.setReferee1(gameSummary.getReferee1Name());
                savedGame.setReferee2(gameSummary.getReferee2Name());
                savedGame.setScorer(gameSummary.getScorerName());

                gameDao.save(savedGame);

                createOrUpdateLeagueIfNeeded(user, savedGame);
            }
        }
    }

    @Override
    public void updateGame(User user, Game game) throws NotFoundException {
        Optional<Game> optSavedGame = gameDao.findByIdAndAllowedUserAndStatusNot(game.getId(), user.getId(), GameStatus.COMPLETED);

        if (optSavedGame.isPresent()) {
            Game savedGame = optSavedGame.get();

            savedGame.setUpdatedAt(game.getUpdatedAt());
            savedGame.setStatus(game.getStatus());
            savedGame.setIndexed(game.isIndexed());
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
        } else {
            throw new NotFoundException(String.format("Could not find game %s for user %s", game.getId(), user.getId()));
        }
    }

    @Override
    public void updateSet(User user, UUID gameId, int setIndex, Set set) throws NotFoundException {
        Game savedGame = gameDao
                .findByIdAndAllowedUserAndStatus(gameId, user.getId(), GameStatus.LIVE)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find game %s for user %s", gameId, user.getId())));

        if (setIndex > 0 && setIndex <= savedGame.getSets().size()) {
            savedGame.getSets().set(setIndex - 1, set);
            savedGame.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            savedGame.setScore(buildScore(savedGame));
            gameDao.save(savedGame);
        } else {
            throw new NotFoundException(String.format("Could not find set %d of game %s for user %s", setIndex, savedGame.getId(), user.getId()));
        }
    }

    @Override
    public void setIndexed(User user, UUID gameId, boolean indexed) throws NotFoundException {
        Game game = getGame(user, gameId);
        gameDao.updateIndexed(game.getId(), indexed, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @Override
    public void setReferee(User user, UUID gameId, String refereeUserId) throws NotFoundException {
        Game game = gameDao
                .findByIdAndCreatedByAndStatusNot(gameId, user.getId(), GameStatus.COMPLETED)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find game %s for user %s", gameId, user.getId())));
        if (user.getId().equals(refereeUserId)) {
            gameDao.updateReferee(game.getId(), user.getId(), user.getPseudo(), LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        } else {
            User.Friend friend = user
                    .getFriend(refereeUserId)
                    .orElseThrow(() -> new NotFoundException(String.format("Could not find referee %s in friends for user %s", gameId, user.getId())));
            gameDao.updateReferee(game.getId(), friend.getId(), friend.getPseudo(), LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        }
    }

    @Override
    public void deleteGame(User user, UUID gameId) {
        gameDao.deleteByIdAndCreatedBy(gameId, user.getId());
    }

    @Override
    public void deleteAllGames(User user) {
        gameDao.deleteByCreatedByAndStatus(user.getId(), GameStatus.COMPLETED);
    }

    @Override
    public void deleteAllGamesInLeague(User user, UUID leagueId) {
        gameDao.deleteByCreatedByAndStatusAndLeague_Id(user.getId(), GameStatus.COMPLETED, leagueId);
    }

    @Override
    public void deleteOldLiveGames(int daysAgo) {
        gameDao.deleteByScheduledAtLessThanAndStatus(epochDateNDaysAgo(daysAgo), GameStatus.LIVE);
    }

    @Override
    public void deleteOldScheduledGames(int daysAgo) {
        gameDao.deleteByScheduledAtLessThanAndStatus(epochDateNDaysAgo(daysAgo), GameStatus.SCHEDULED);
    }

    private Optional<Rules> findRules(User user, UUID rulesId, GameType kind) {
        Optional<Rules> optRules = Rules.getDefaultRules(rulesId, kind);

        if (optRules.isEmpty()) {
            optRules = rulesDao.findByIdAndCreatedByAndKind(rulesId, user.getId(), kind);
        }

        return optRules;
    }

    private long epochDateNDaysAgo(int daysAgo) {
        return LocalDateTime.now(ZoneOffset.UTC).minusDays(daysAgo).toEpochSecond(ZoneOffset.UTC) * 1000L;
    }

    private Game.SelectedLeague findOrCreateLeague(User user, GameSummary gameSummary) {
        Game.SelectedLeague selectedLeague = null;

        if (gameSummary.getLeagueId() != null
                && gameSummary.getLeagueName() != null && !gameSummary.getLeagueName().isBlank()
                && gameSummary.getDivisionName() != null && !gameSummary.getDivisionName().isBlank()) {
            Optional<League> optLeague = leagueDao.findByIdAndCreatedBy(gameSummary.getLeagueId(), user.getId());
            League league;

            if (optLeague.isPresent()) {
                league = optLeague.get();
            } else {
                league = new League();
                league.setId(gameSummary.getLeagueId());
                league.setCreatedBy(user.getId());
                league.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                league.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                league.setKind(gameSummary.getKind());
                league.setName(gameSummary.getLeagueName());
                league.setDivisions(new ArrayList<>());
                league.getDivisions().add(gameSummary.getDivisionName());

                try {
                    leagueService.createLeague(user, league);
                } catch (ConflictException e) {
                    /* already exists */
                }
            }

            selectedLeague = buildSelectedLeague(league, gameSummary.getDivisionName());
        }

        return selectedLeague;
    }

    private void createOrUpdateLeagueIfNeeded(User user, Game game) {
        if (game.getLeague() != null) {
            try {
                leagueService.updateDivisions(user, game.getLeague().getId());
            } catch (NotFoundException e) {
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

                try {
                    leagueService.createLeague(user, league);
                } catch (ConflictException e2) {
                    /* already exists */
                }
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
