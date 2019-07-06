package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.dto.Ranking;
import com.tonkar.volleyballreferee.entity.Set;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.generated.ExcelDivisionWriter;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.*;
import com.tonkar.volleyballreferee.generated.ScoreSheetWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameDao gameDao;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private RulesRepository rulesRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<GameSummary> listLiveGames() {
        return gameDao.listLiveGames();
    }

    @Override
    public List<GameSummary> listGamesMatchingToken(String token) {
        return gameDao.listGamesMatchingToken(token);
    }

    @Override
    public List<GameSummary> listGamesWithScheduleDate(LocalDate date) {
        return gameDao.listGamesWithScheduleDate(date);
    }

    @Override
    public List<GameSummary> listGamesInLeague(UUID leagueId) {
        return gameDao.listGamesInLeague(leagueId);
    }

    @Override
    public List<GameSummary> listGamesOfTeamInLeague(UUID leagueId, UUID teamId) {
        return gameDao.listGamesOfTeamInLeague(leagueId, teamId);
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
    public List<GameSummary> listGamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listGamesInDivision(leagueId, divisionName);
    }

    @Override
    public List<GameSummary> listGamesOfTeamInDivision(UUID leagueId, String divisionName, UUID teamId) {
        return gameDao.listGamesOfTeamInDivision(leagueId, divisionName, teamId);
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
        return gameRepository
                .findById(gameId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find game %s", gameId)));
    }

    @Override
    public FileWrapper getScoreSheet(UUID gameId) throws NotFoundException {
        Game game = getGame(gameId);
        return ScoreSheetWriter.writeGame(game);
    }

    @Override
    public FileWrapper listGamesInDivisionExcel(UUID leagueId, String divisionName) throws IOException {
        List<Game> games = gameRepository.findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(leagueId, divisionName, GameStatus.COMPLETED);
        return ExcelDivisionWriter.writeExcelDivision(divisionName, games);
    }

    @Override
    public List<Ranking> listRankingsInDivision(UUID leagueId, String divisionName) {
        List<Game> games = gameRepository.findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(leagueId, divisionName, GameStatus.COMPLETED);
        Rankings rankings = new Rankings();
        games.forEach(rankings::addGame);
        return rankings.list();
    }

    @Override
    public List<GameSummary> listGames(User user) {
        return gameDao.listGames(user.getId());
    }

    @Override
    public List<GameSummary> listGamesWithStatus(User user, GameStatus status) {
        return gameDao.listGamesWithStatus(user.getId(), status);
    }

    @Override
    public List<GameSummary> listAvailableGames(User user) {
        return gameDao.listAvailableGames(user.getId());
    }

    @Override
    public List<GameSummary> listCompletedGames(User user) {
        return gameDao.listCompletedGames(user.getId());
    }

    @Override
    public List<GameSummary> listGamesInLeague(User user, UUID leagueId) {
        return gameDao.listGamesInLeague(user.getId(), leagueId);
    }

    @Override
    public Game getGame(User user, UUID gameId) throws NotFoundException {
        return gameRepository
                .findByIdAndAllowedUser(gameId, user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find game %s for user %s", gameId, user.getId())));
    }

    @Override
    public Count getNumberOfGames(User user) {
        return new Count(gameRepository.countByCreatedBy(user.getId()));
    }

    @Override
    public Count getNumberOfGamesInLeague(User user, UUID leagueId) {
        return new Count(gameRepository.countByCreatedByAndLeagueId(user.getId(), leagueId));
    }

    @Override
    public Count getNumberOfAvailableGames(User user) {
        return new Count(gameRepository.countByAllowedUserAndStatusNot(user.getId(), GameStatus.COMPLETED));
    }

    @Override
    public void createGame(User user, GameSummary gameSummary) throws ConflictException, NotFoundException {
        if (gameRepository.existsById(gameSummary.getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because it already exists", gameSummary.getId(), user.getId()));
        } else if (gameSummary.getHomeTeamId().equals(gameSummary.getGuestTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameSummary.getId(), user.getId(), gameSummary.getHomeTeamId()));
        } else if (!gameSummary.getCreatedBy().equals(gameSummary.getRefereedBy()) && !userRepository.areFriends(gameSummary.getCreatedBy(), gameSummary.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameSummary.getId(), user.getId(), gameSummary.getCreatedBy(), gameSummary.getRefereedBy()));
        } else {
            Optional<Team> optHTeam = teamRepository.findByIdAndCreatedByAndKind(gameSummary.getHomeTeamId(), user.getId(), gameSummary.getKind());
            Optional<Team> optGTeam = teamRepository.findByIdAndCreatedByAndKind(gameSummary.getGuestTeamId(), user.getId(), gameSummary.getKind());
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

                gameRepository.save(game);

                createOrUpdateLeagueIfNeeded(user, game);
            }
        }
    }

    @Override
    public void createGame(User user, Game game) throws ConflictException, NotFoundException {
        if (gameRepository.existsById(game.getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because it already exists", game.getId(), user.getId()));
        } else if (game.getHomeTeam().getId().equals(game.getGuestTeam().getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", game.getId(), user.getId(), game.getHomeTeam().getId()));
        } else if (!game.getCreatedBy().equals(game.getRefereedBy()) && !userRepository.areFriends(game.getCreatedBy(), game.getRefereedBy())) {
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
            gameRepository.save(game);

            createOrUpdateLeagueIfNeeded(user, game);
        }
    }

    @Override
    public void updateGame(User user, GameSummary gameSummary) throws ConflictException, NotFoundException {
        Optional<Game> optSavedGame = gameRepository.findByIdAndAllowedUserAndStatus(gameSummary.getId(), user.getId(), GameStatus.SCHEDULED);

        if (optSavedGame.isEmpty()) {
            throw new NotFoundException(String.format("Could not find game %s %s for user %s", gameSummary.getId(), GameStatus.SCHEDULED, user.getId()));
        } else if (gameSummary.getHomeTeamId().equals(gameSummary.getGuestTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameSummary.getId(), user.getId(), gameSummary.getHomeTeamId()));
        } else if (!gameSummary.getCreatedBy().equals(gameSummary.getRefereedBy()) && !userRepository.areFriends(gameSummary.getCreatedBy(), gameSummary.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameSummary.getId(), user.getId(), gameSummary.getCreatedBy(), gameSummary.getRefereedBy()));
        } else {
            Game savedGame = optSavedGame.get();

            Optional<Team> optHTeam = teamRepository.findByIdAndCreatedByAndKind(gameSummary.getHomeTeamId(), user.getId(), savedGame.getKind());
            Optional<Team> optGTeam = teamRepository.findByIdAndCreatedByAndKind(gameSummary.getGuestTeamId(), user.getId(), savedGame.getKind());
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

                gameRepository.save(savedGame);

                createOrUpdateLeagueIfNeeded(user, savedGame);
            }
        }
    }

    @Override
    public void updateGame(User user, Game game) throws NotFoundException {
        Optional<Game> optSavedGame = gameRepository.findByIdAndAllowedUserAndStatusNot(game.getId(), user.getId(), GameStatus.COMPLETED);

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

            gameRepository.save(savedGame);
        } else {
            throw new NotFoundException(String.format("Could not find game %s for user %s", game.getId(), user.getId()));
        }
    }

    @Override
    public void updateSet(User user, UUID gameId, int setIndex, Set set) throws NotFoundException {
        Game savedGame = gameRepository
                .findByIdAndAllowedUserAndStatus(gameId, user.getId(), GameStatus.LIVE)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find game %s for user %s", gameId, user.getId())));

        if (setIndex > 0 && setIndex <= savedGame.getSets().size()) {
            savedGame.getSets().set(setIndex - 1, set);
            savedGame.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            savedGame.setScore(buildScore(savedGame));
            gameRepository.save(savedGame);
        } else {
            throw new NotFoundException(String.format("Could not find set %d of game %s for user %s", setIndex, savedGame.getId(), user.getId()));
        }
    }

    @Override
    public void setIndexed(User user, UUID gameId, boolean indexed) throws NotFoundException {
        Game game = getGame(user, gameId);
        game.setIndexed(indexed);
        game.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        gameRepository.save(game);
    }

    @Override
    public void setReferee(User user, UUID gameId, String refereeUserId) throws NotFoundException {
        Game game = gameRepository
                .findByIdAndCreatedByAndStatusNot(gameId, user.getId(), GameStatus.COMPLETED)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find game %s for user %s", gameId, user.getId())));
        if (user.getId().equals(refereeUserId)) {
            game.setRefereedBy(user.getId());
            game.setRefereeName(user.getPseudo());
            game.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            gameRepository.save(game);
        } else {
            User.Friend friend = user
                    .getFriend(refereeUserId)
                    .orElseThrow(() -> new NotFoundException(String.format("Could not find referee %s in friends for user %s", gameId, user.getId())));
            game.setRefereedBy(friend.getId());
            game.setRefereeName(friend.getPseudo());
            game.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            gameRepository.save(game);
        }
    }

    @Override
    public void deleteGame(User user, UUID gameId) {
        gameRepository.deleteByIdAndCreatedBy(gameId, user.getId());
    }

    @Override
    public void deleteAllGames(User user) {
        gameRepository.deleteByCreatedByAndStatus(user.getId(), GameStatus.COMPLETED);
    }

    @Override
    public void deleteOldLiveGames(int daysAgo) {
        gameRepository.deleteByScheduledAtLessThanAndStatus(epochDateNDaysAgo(daysAgo), GameStatus.LIVE);
    }

    @Override
    public void deleteOldScheduledGames(int daysAgo) {
        gameRepository.deleteByScheduledAtLessThanAndStatus(epochDateNDaysAgo(daysAgo), GameStatus.SCHEDULED);
    }

    private Optional<Rules> findRules(User user, UUID rulesId, GameType kind) {
        Optional<Rules> optRules = Rules.getDefaultRules(rulesId, kind);

        if (optRules.isEmpty()) {
            optRules = rulesRepository.findByIdAndCreatedByAndKind(rulesId, user.getId(), kind);
        }

        return optRules;
    }

    private long epochDateNDaysAgo(int daysAgo) {
        return  System.currentTimeMillis() - (daysAgo * 86400000L);
    }

    private Game.SelectedLeague findOrCreateLeague(User user, GameSummary gameSummary) {
        Game.SelectedLeague selectedLeague = null;

        if (gameSummary.getLeagueId() != null
                && gameSummary.getLeagueName() != null && !gameSummary.getLeagueName().isBlank()
                && gameSummary.getDivisionName() != null && !gameSummary.getDivisionName().isBlank()) {
            Optional<League> optLeague = leagueRepository.findByIdAndCreatedBy(gameSummary.getLeagueId(), user.getId());
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
