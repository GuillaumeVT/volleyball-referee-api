package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameDescription;
import com.tonkar.volleyballreferee.entity.Set;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.generated.ExcelDivisionWriter;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.GameRepository;
import com.tonkar.volleyballreferee.repository.RulesRepository;
import com.tonkar.volleyballreferee.repository.TeamRepository;
import com.tonkar.volleyballreferee.repository.UserRepository;
import com.tonkar.volleyballreferee.generated.ScoreSheetWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private TeamService teamService;

    @Autowired
    private RulesService rulesService;

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
    private UserRepository userRepository;

    @Override
    public List<GameDescription> listLiveGames() {
        return gameDao.listLiveGames();
    }

    @Override
    public List<GameDescription> listGamesMatchingToken(String token) {
        return gameDao.listGamesMatchingToken(token);
    }

    @Override
    public List<GameDescription> listGamesWithScheduleDate(LocalDate date) {
        return gameDao.listGamesWithScheduleDate(date);
    }

    @Override
    public List<GameDescription> listGamesInLeague(UUID leagueId) {
        return gameDao.listGamesInLeague(leagueId);
    }

    @Override
    public List<GameDescription> listGamesOfTeamInLeague(UUID leagueId, UUID teamId) {
        return gameDao.listGamesOfTeamInLeague(leagueId, teamId);
    }

    @Override
    public List<GameDescription> listLiveGamesInLeague(UUID leagueId) {
        return gameDao.listLiveGamesInLeague(leagueId);
    }

    @Override
    public List<GameDescription> listLast10GamesInLeague(UUID leagueId) {
        return gameDao.listLast10GamesInLeague(leagueId);
    }

    @Override
    public List<GameDescription> listNext10GamesInLeague(UUID leagueId) {
        return gameDao.listNext10GamesInLeague(leagueId);
    }

    @Override
    public List<GameDescription> listGamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listGamesInDivision(leagueId, divisionName);
    }

    @Override
    public List<GameDescription> listGamesOfTeamInDivision(UUID leagueId, String divisionName, UUID teamId) {
        return gameDao.listGamesOfTeamInDivision(leagueId, divisionName, teamId);
    }

    @Override
    public List<GameDescription> listLiveGamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listLiveGamesInDivision(leagueId, divisionName);
    }

    @Override
    public List<GameDescription> listLast10GamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listLast10GamesInDivision(leagueId, divisionName);
    }

    @Override
    public List<GameDescription> listNext10GamesInDivision(UUID leagueId, String divisionName) {
        return gameDao.listNext10GamesInDivision(leagueId, divisionName);
    }

    @Override
    public Game getGame(UUID gameId) throws NotFoundException {
        Optional<Game> optGame = gameRepository.findById(gameId);
        if (optGame.isPresent()) {
            return optGame.get();
        } else {
            throw new NotFoundException(String.format("Could not find game %s", gameId));
        }
    }

    @Override
    public FileWrapper getScoreSheet(UUID gameId) throws NotFoundException {
        Optional<Game> optGame = gameRepository.findById(gameId);
        if (optGame.isPresent()) {
            return ScoreSheetWriter.writeGame(optGame.get());
        } else {
            throw new NotFoundException(String.format("Could not find game %s", gameId));
        }
    }

    @Override
    public List<GameDescription> listGames(String userId) {
        return gameDao.listGames(userId);
    }

    @Override
    public List<GameDescription> listGamesWithStatus(String userId, GameStatus status) {
        return gameDao.listGamesWithStatus(userId, status);
    }

    @Override
    public List<GameDescription> listAvailableGames(String userId) {
        return gameDao.listAvailableGames(userId);
    }

    @Override
    public List<GameDescription> listCompletedGames(String userId) {
        return gameDao.listCompletedGames(userId);
    }

    @Override
    public List<GameDescription> listGamesInLeague(String userId, UUID leagueId) {
        return gameDao.listGamesInLeague(userId, leagueId);
    }

    @Override
    public FileWrapper listGamesInDivisionExcel(String userId, UUID leagueId, String divisionName) throws IOException {
        List<Game> games = gameRepository.findByCreatedByAndLeagueIdAndDivisionNameAndStatusOrderByScheduledAtAsc(userId, leagueId, divisionName, GameStatus.COMPLETED);
        return ExcelDivisionWriter.writeExcelDivision(divisionName, games);
    }

    @Override
    public Game getGame(String userId, UUID gameId) throws NotFoundException {
        Optional<Game> optGame = gameRepository.findByIdAndCreatedBy(gameId, userId);
        if (optGame.isPresent()) {
            return optGame.get();
        } else {
            throw new NotFoundException(String.format("Could not find game %s for user %s", gameId, userId));
        }
    }

    @Override
    public Count getNumberOfGames(String userId) {
        return new Count(gameRepository.countByCreatedBy(userId));
    }

    @Override
    public Count getNumberOfGamesInLeague(String userId, UUID leagueId) {
        return new Count(gameRepository.countByCreatedByAndLeagueId(userId, leagueId));
    }

    @Override
    public void createGame(String userId, GameDescription gameDescription) throws ConflictException, NotFoundException {
        if (gameRepository.existsById(gameDescription.getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because it already exists", gameDescription.getId(), userId));
        } else if (gameDescription.getHomeTeamId().equals(gameDescription.getGuestTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameDescription.getId(), userId, gameDescription.getHomeTeamId()));
        } else if (!gameDescription.getCreatedBy().equals(gameDescription.getRefereedBy()) && !userRepository.areFriends(gameDescription.getCreatedBy(), gameDescription.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameDescription.getId(), userId, gameDescription.getCreatedBy(), gameDescription.getRefereedBy()));
        } else {
            Optional<Team> optHTeam = teamRepository.findByIdAndCreatedByAndKind(gameDescription.getHomeTeamId(), userId, gameDescription.getKind());
            Optional<Team> optGTeam = teamRepository.findByIdAndCreatedByAndKind(gameDescription.getGuestTeamId(), userId, gameDescription.getKind());
            Optional<Rules> optRules = findRules(userId, gameDescription.getRulesId(), gameDescription.getKind());

            if (optHTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching home team %s for user %s", gameDescription.getHomeTeamId(), userId));
            } else if (optGTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching guest team %s for user %s", gameDescription.getGuestTeamId(), userId));
            } else if (optRules.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching rules %s for user %s", gameDescription.getRulesId(), userId));
            } else {
                Game game = new Game();

                game.setId(gameDescription.getId());
                game.setCreatedBy(gameDescription.getCreatedBy());
                game.setCreatedAt(gameDescription.getCreatedAt());
                game.setUpdatedAt(gameDescription.getUpdatedAt());
                game.setScheduledAt(gameDescription.getScheduledAt());
                game.setRefereedBy(gameDescription.getRefereedBy());
                game.setRefereeName(gameDescription.getRefereeName());
                game.setKind(gameDescription.getKind());
                game.setGender(gameDescription.getGender());
                game.setUsage(gameDescription.getUsage());
                game.setStatus(GameStatus.SCHEDULED);
                game.setIndexed(gameDescription.isIndexed());
                game.setLeagueId(gameDescription.getLeagueId());
                game.setLeagueName(gameDescription.getLeagueName());
                game.setDivisionName(gameDescription.getDivisionName());
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

                createOrUpdateLeagueIfNeeded(userId, game);
            }
        }
    }

    @Override
    public void createGame(String userId, Game game) throws ConflictException, NotFoundException {
        if (gameRepository.existsById(game.getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because it already exists", game.getId(), userId));
        } else if (game.getHomeTeam().getId().equals(game.getGuestTeam().getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", game.getId(), userId, game.getHomeTeam().getId()));
        } else if (!game.getCreatedBy().equals(game.getRefereedBy()) && !userRepository.areFriends(game.getCreatedBy(), game.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", game.getId(), userId, game.getCreatedBy(), game.getRefereedBy()));
        } else {
            game.setCreatedBy(userId);
            game.getHomeTeam().setCreatedBy(userId);
            game.getGuestTeam().setCreatedBy(userId);
            game.getRules().setCreatedBy(userId);
            gameRepository.save(game);

            createTeamsAndRulesAndLeaguesIfNeeded(userId, game);
        }
    }

    private void createTeamsAndRulesAndLeaguesIfNeeded(String userId, Game game) {
        try {
            teamService.createTeam(userId, game.getHomeTeam());
        } catch (ConflictException e) { /* already exists */ }
        try {
            teamService.createTeam(userId, game.getGuestTeam());
        } catch (ConflictException e) { /* already exists */ }
        try {
            rulesService.createRules(userId, game.getRules());
        } catch (ConflictException e) { /* already exists */ }

        createOrUpdateLeagueIfNeeded(userId, game);
    }

    private void createOrUpdateLeagueIfNeeded(String userId, Game game) {
        if (game.getLeagueId() != null) {
            try {
                leagueService.updateDivisions(userId, game.getLeagueId());
            } catch (NotFoundException e) {
                /* does not exist */
                if (game.getLeagueName() != null && !game.getLeagueName().isBlank()) {
                    League league = new League();
                    league.setId(game.getLeagueId());
                    league.setCreatedBy(userId);
                    league.setCreatedAt(game.getCreatedAt());
                    league.setKind(game.getKind());
                    league.setName(game.getLeagueName());
                    league.setDivisions(new ArrayList<>());
                    if (game.getDivisionName() != null && !game.getDivisionName().isBlank()) {
                        league.getDivisions().add(game.getDivisionName());
                    }

                    try {
                        leagueService.createLeague(userId, league);
                    } catch (ConflictException e2) {
                        /* already exists */
                    }
                }
            }
        }
    }

    @Override
    public void updateGame(String userId, GameDescription gameDescription) throws ConflictException, NotFoundException {
        Optional<Game> optSavedGame = gameRepository.findByIdAndAllowedUserAndStatus(gameDescription.getId(), userId, GameStatus.SCHEDULED);

        if (optSavedGame.isEmpty()) {
            throw new NotFoundException(String.format("Could not find game %s %s for user %s", gameDescription.getId(), GameStatus.SCHEDULED, userId));
        } else if (gameDescription.getHomeTeamId().equals(gameDescription.getGuestTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameDescription.getId(), userId, gameDescription.getHomeTeamId()));
        } else if (!gameDescription.getCreatedBy().equals(gameDescription.getRefereedBy()) && !userRepository.areFriends(gameDescription.getCreatedBy(), gameDescription.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameDescription.getId(), userId, gameDescription.getCreatedBy(), gameDescription.getRefereedBy()));
        } else {
            Game savedGame = optSavedGame.get();

            Optional<Team> optHTeam = teamRepository.findByIdAndCreatedByAndKind(gameDescription.getHomeTeamId(), userId, savedGame.getKind());
            Optional<Team> optGTeam = teamRepository.findByIdAndCreatedByAndKind(gameDescription.getGuestTeamId(), userId, savedGame.getKind());
            Optional<Rules> optRules = findRules(userId, gameDescription.getRulesId(), savedGame.getKind());

            if (optHTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching home team %s for user %s", gameDescription.getHomeTeamId(), userId));
            } else if (optGTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching guest team %s for user %s", gameDescription.getGuestTeamId(), userId));
            } else if (optRules.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching rules %s for user %s", gameDescription.getRulesId(), userId));
            } else {
                savedGame.setUpdatedAt(gameDescription.getUpdatedAt());
                savedGame.setScheduledAt(gameDescription.getScheduledAt());
                savedGame.setRefereedBy(gameDescription.getRefereedBy());
                savedGame.setRefereeName(gameDescription.getRefereeName());
                savedGame.setGender(gameDescription.getGender());
                savedGame.setUsage(gameDescription.getUsage());
                savedGame.setIndexed(gameDescription.isIndexed());
                savedGame.setLeagueId(gameDescription.getLeagueId());
                savedGame.setLeagueName(gameDescription.getLeagueName());
                savedGame.setDivisionName(gameDescription.getDivisionName());
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

                createOrUpdateLeagueIfNeeded(userId, savedGame);
            }
        }
    }

    @Override
    public void updateGame(String userId, Game game) throws NotFoundException {
        Optional<Game> optSavedGame = gameRepository.findByIdAndAllowedUserAndStatusNot(game.getId(), userId, GameStatus.COMPLETED);

        if (optSavedGame.isPresent()) {
            Game savedGame = optSavedGame.get();

            savedGame.setUpdatedAt(game.getUpdatedAt());
            savedGame.setStatus(game.getStatus());
            savedGame.setIndexed(game.isIndexed());
            savedGame.setLeagueId(game.getLeagueId());
            savedGame.setLeagueName(game.getLeagueName());
            savedGame.setDivisionName(game.getDivisionName());
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
            throw new NotFoundException(String.format("Could not find game %s for user %s", game.getId(), userId));
        }
    }

    @Override
    public void updateSet(String userId, UUID gameId, int setIndex, Set set) throws NotFoundException {
        Optional<Game> optSavedGame = gameRepository.findByIdAndAllowedUserAndStatus(gameId, userId, GameStatus.LIVE);

        if (optSavedGame.isPresent()) {
            Game savedGame = optSavedGame.get();

            if (setIndex > 0 && setIndex <= savedGame.getSets().size()) {
                savedGame.getSets().set(setIndex - 1, set);
                gameRepository.save(savedGame);
            } else {
                throw new NotFoundException(String.format("Could not find set %d of game %s for user %s", setIndex, savedGame.getId(), userId));
            }
        } else {
            throw new NotFoundException(String.format("Could not find game %s for user %s", gameId, userId));
        }
    }

    @Override
    public void deleteGame(String userId, UUID gameId) {
        gameRepository.deleteByIdAndCreatedBy(gameId, userId);
    }

    @Override
    public void deleteAllGames(String userId) {
        gameRepository.deleteByCreatedBy(userId);
    }

    @Override
    public void deleteOldLiveGames(int daysAgo) {
        gameRepository.deleteByScheduledAtLessThanAndStatus(epochDateNDaysAgo(daysAgo), GameStatus.LIVE);
    }

    @Override
    public void deleteOldScheduledGames(int daysAgo) {
        gameRepository.deleteByScheduledAtLessThanAndStatus(epochDateNDaysAgo(daysAgo), GameStatus.SCHEDULED);
    }

    private Optional<Rules> findRules(String userId, UUID rulesId, GameType kind) {
        Optional<Rules> optRules = Rules.getDefaultRules(rulesId, kind);

        if (optRules.isEmpty()) {
            optRules = rulesRepository.findByIdAndCreatedByAndKind(rulesId, userId, kind);
        }

        return optRules;
    }

    private long epochDateNDaysAgo(int daysAgo) {
        return  System.currentTimeMillis() - (daysAgo * 86400000L);
    }

    private String buildScore(Game game) {
        StringBuilder scoreBuilder = new StringBuilder();

        for (Set set : game.getSets()) {
            scoreBuilder.append(String.format("%d-%d\t\t", set.getHomePoints(), set.getGuestPoints()));
        }

        return scoreBuilder.toString().trim();
    }
}
