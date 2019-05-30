package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameDescription;
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
    public List<GameDescription> listGames(User user) {
        return gameDao.listGames(user.getId());
    }

    @Override
    public List<GameDescription> listGamesWithStatus(User user, GameStatus status) {
        return gameDao.listGamesWithStatus(user.getId(), status);
    }

    @Override
    public List<GameDescription> listAvailableGames(User user) {
        return gameDao.listAvailableGames(user.getId());
    }

    @Override
    public List<GameDescription> listCompletedGames(User user) {
        return gameDao.listCompletedGames(user.getId());
    }

    @Override
    public List<GameDescription> listGamesInLeague(User user, UUID leagueId) {
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
    public void createGame(User user, GameDescription gameDescription) throws ConflictException, NotFoundException {
        if (gameRepository.existsById(gameDescription.getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because it already exists", gameDescription.getId(), user.getId()));
        } else if (gameDescription.getHomeTeamId().equals(gameDescription.getGuestTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameDescription.getId(), user.getId(), gameDescription.getHomeTeamId()));
        } else if (!gameDescription.getCreatedBy().equals(gameDescription.getRefereedBy()) && !userRepository.areFriends(gameDescription.getCreatedBy(), gameDescription.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameDescription.getId(), user.getId(), gameDescription.getCreatedBy(), gameDescription.getRefereedBy()));
        } else {
            Optional<Team> optHTeam = teamRepository.findByIdAndCreatedByAndKind(gameDescription.getHomeTeamId(), user.getId(), gameDescription.getKind());
            Optional<Team> optGTeam = teamRepository.findByIdAndCreatedByAndKind(gameDescription.getGuestTeamId(), user.getId(), gameDescription.getKind());
            Optional<Rules> optRules = findRules(user, gameDescription.getRulesId(), gameDescription.getKind());

            if (optHTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching home team %s for user %s", gameDescription.getHomeTeamId(), user.getId()));
            } else if (optGTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching guest team %s for user %s", gameDescription.getGuestTeamId(), user.getId()));
            } else if (optRules.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching rules %s for user %s", gameDescription.getRulesId(), user.getId()));
            } else {
                SelectedLeague league = findOrCreateLeague(user, gameDescription);

                Game game = new Game();

                game.setId(gameDescription.getId());
                game.setCreatedBy(gameDescription.getCreatedBy());
                game.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                game.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                game.setScheduledAt(gameDescription.getScheduledAt());
                game.setRefereedBy(gameDescription.getRefereedBy());
                game.setRefereeName(gameDescription.getRefereeName());
                game.setKind(gameDescription.getKind());
                game.setGender(gameDescription.getGender());
                game.setUsage(gameDescription.getUsage());
                game.setStatus(GameStatus.SCHEDULED);
                game.setIndexed(gameDescription.isIndexed());
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
    public void updateGame(User user, GameDescription gameDescription) throws ConflictException, NotFoundException {
        Optional<Game> optSavedGame = gameRepository.findByIdAndAllowedUserAndStatus(gameDescription.getId(), user.getId(), GameStatus.SCHEDULED);

        if (optSavedGame.isEmpty()) {
            throw new NotFoundException(String.format("Could not find game %s %s for user %s", gameDescription.getId(), GameStatus.SCHEDULED, user.getId()));
        } else if (gameDescription.getHomeTeamId().equals(gameDescription.getGuestTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameDescription.getId(), user.getId(), gameDescription.getHomeTeamId()));
        } else if (!gameDescription.getCreatedBy().equals(gameDescription.getRefereedBy()) && !userRepository.areFriends(gameDescription.getCreatedBy(), gameDescription.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameDescription.getId(), user.getId(), gameDescription.getCreatedBy(), gameDescription.getRefereedBy()));
        } else {
            Game savedGame = optSavedGame.get();

            Optional<Team> optHTeam = teamRepository.findByIdAndCreatedByAndKind(gameDescription.getHomeTeamId(), user.getId(), savedGame.getKind());
            Optional<Team> optGTeam = teamRepository.findByIdAndCreatedByAndKind(gameDescription.getGuestTeamId(), user.getId(), savedGame.getKind());
            Optional<Rules> optRules = findRules(user, gameDescription.getRulesId(), savedGame.getKind());

            if (optHTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching home team %s for user %s", gameDescription.getHomeTeamId(), user.getId()));
            } else if (optGTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching guest team %s for user %s", gameDescription.getGuestTeamId(), user.getId()));
            } else if (optRules.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching rules %s for user %s", gameDescription.getRulesId(), user.getId()));
            } else {
                SelectedLeague league = findOrCreateLeague(user, gameDescription);

                savedGame.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                savedGame.setScheduledAt(gameDescription.getScheduledAt());
                savedGame.setRefereedBy(gameDescription.getRefereedBy());
                savedGame.setRefereeName(gameDescription.getRefereeName());
                savedGame.setGender(gameDescription.getGender());
                savedGame.setUsage(gameDescription.getUsage());
                savedGame.setIndexed(gameDescription.isIndexed());
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
        gameRepository.deleteByCreatedBy(user.getId());
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

    private SelectedLeague findOrCreateLeague(User user, GameDescription gameDescription) {
        SelectedLeague selectedLeague = null;

        if (!GameType.TIME.equals(gameDescription.getKind()) && gameDescription.getLeagueId() != null
                && gameDescription.getLeagueName() != null && !gameDescription.getLeagueName().isBlank()
                && gameDescription.getDivisionName() != null && !gameDescription.getDivisionName().isBlank()) {
            Optional<League> optLeague = leagueRepository.findByIdAndCreatedBy(gameDescription.getLeagueId(), user.getId());
            League league;

            if (optLeague.isPresent()) {
                league = optLeague.get();
            } else {
                league = new League();
                league.setId(gameDescription.getLeagueId());
                league.setCreatedBy(user.getId());
                league.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                league.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                league.setKind(gameDescription.getKind());
                league.setName(gameDescription.getLeagueName());
                league.setDivisions(new ArrayList<>());
                league.getDivisions().add(gameDescription.getDivisionName());

                try {
                    leagueService.createLeague(user, league);
                } catch (ConflictException e) {
                    /* already exists */
                }
            }

            selectedLeague = buildSelectedLeague(league, gameDescription.getDivisionName());
        }

        return selectedLeague;
    }

    private void createOrUpdateLeagueIfNeeded(User user, Game game) {
        if (game.getLeague() != null && !GameType.TIME.equals(game.getKind())) {
            try {
                leagueService.updateDivisions(user, game.getLeague().getId());
            } catch (NotFoundException e) {
                /* does not exist */
                SelectedLeague selectedLeague = game.getLeague();
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

    private SelectedLeague buildSelectedLeague(League league, String divisionName) {
        SelectedLeague selectedLeague = new SelectedLeague();
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
