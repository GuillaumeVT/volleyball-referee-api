package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameDescription;
import com.tonkar.volleyballreferee.entity.Set;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.GameRepository;
import com.tonkar.volleyballreferee.repository.RulesRepository;
import com.tonkar.volleyballreferee.repository.TeamRepository;
import com.tonkar.volleyballreferee.repository.UserRepository;
import com.tonkar.volleyballreferee.scoresheet.ScoreSheet;
import com.tonkar.volleyballreferee.scoresheet.ScoreSheetWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

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
    public Game getGame(UUID gameId) throws NotFoundException {
        Optional<Game> optGame = gameRepository.findById(gameId);
        if (optGame.isPresent()) {
            return optGame.get();
        } else {
            throw new NotFoundException(String.format("Could not find game %s", gameId));
        }
    }

    @Override
    public ScoreSheet getScoreSheet(UUID gameId) throws NotFoundException {
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
    public List<GameDescription> listGamesInLeague(String userId, UUID leagueId) {
        return gameDao.listGamesInLeague(userId, leagueId);
    }

    @Override
    public byte[] listGamesInLeagueCsv(String userId, UUID leagueId, Optional<String> divisionName) {
        final Stream<Game> gameStream;

        if (divisionName.isEmpty()) {
            gameStream = gameRepository.findByIdAndCreatedByAndLeagueIdAndStatus(userId, leagueId, GameStatus.COMPLETED);
        } else {
            gameStream = gameRepository.findByIdAndCreatedByAndLeagueIdAndStatusAndDivisionName(userId, leagueId, GameStatus.COMPLETED, divisionName.get());
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);

        appendCsvHeader(printWriter);
        gameStream.forEach(game -> appendCsvGame(game, printWriter));

        printWriter.close();

        return byteArrayOutputStream.toByteArray();
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
        } else if (gameDescription.getHTeamId().equals(gameDescription.getGTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameDescription.getId(), userId, gameDescription.getHTeamId()));
        } else if (!gameDescription.getCreatedBy().equals(gameDescription.getRefereedBy()) && !userRepository.areFriends(gameDescription.getCreatedBy(), gameDescription.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameDescription.getId(), userId, gameDescription.getCreatedBy(), gameDescription.getRefereedBy()));
        } else {
            Optional<Team> optHTeam = teamRepository.findByIdAndCreatedByAndKindAndGender(gameDescription.getHTeamId(), userId, gameDescription.getKind(), gameDescription.getGender());
            Optional<Team> optGTeam = teamRepository.findByIdAndCreatedByAndKindAndGender(gameDescription.getGTeamId(), userId, gameDescription.getKind(), gameDescription.getGender());
            Optional<Rules> optRules = rulesRepository.findByIdAndCreatedByAndKind(gameDescription.getRulesId(), userId, gameDescription.getKind());

            if (optHTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching home team %s for user %s", gameDescription.getHTeamId(), userId));
            } else if (optGTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching guest team %s for user %s", gameDescription.getGTeamId(), userId));
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
                game.setHTeam(optHTeam.get());
                game.setGTeam(optGTeam.get());
                game.setHSets(0);
                game.setGSets(0);
                game.setSets(new ArrayList<>());
                game.setHCards(new ArrayList<>());
                game.setGCards(new ArrayList<>());
                game.setRules(optRules.get());

                gameRepository.save(game);

                createOrUpdateLeagueIfNeeded(userId, game);
            }
        }
    }

    @Override
    public void createGame(String userId, Game game) throws ConflictException, NotFoundException {
        if (gameRepository.existsById(game.getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because it already exists", game.getId(), userId));
        } else if (game.getHTeam().getId().equals(game.getGTeam().getId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", game.getId(), userId, game.getHTeam().getId()));
        } else if (!game.getCreatedBy().equals(game.getRefereedBy()) && !userRepository.areFriends(game.getCreatedBy(), game.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", game.getId(), userId, game.getCreatedBy(), game.getRefereedBy()));
        } else {
            game.setCreatedBy(userId);
            game.getHTeam().setCreatedBy(userId);
            game.getGTeam().setCreatedBy(userId);
            game.getRules().setCreatedBy(userId);
            gameRepository.save(game);

            createTeamsAndRulesAndLeaguesIfNeeded(userId, game);
        }
    }

    private void createTeamsAndRulesAndLeaguesIfNeeded(String userId, Game game) {
        try {
            teamService.createTeam(userId, game.getHTeam());
        } catch (ConflictException e) { /* already exists */ }
        try {
            teamService.createTeam(userId, game.getGTeam());
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
        } else if (gameDescription.getHTeamId().equals(gameDescription.getGTeamId())) {
            throw new ConflictException(String.format("Could not create game %s for user %s because team %s cannot play against itself", gameDescription.getId(), userId, gameDescription.getHTeamId()));
        } else if (!gameDescription.getCreatedBy().equals(gameDescription.getRefereedBy()) && !userRepository.areFriends(gameDescription.getCreatedBy(), gameDescription.getRefereedBy())) {
            throw new NotFoundException(String.format("Could not create game %s for user %s because %s and %s are not friends", gameDescription.getId(), userId, gameDescription.getCreatedBy(), gameDescription.getRefereedBy()));
        } else {
            Game savedGame = optSavedGame.get();

            Optional<Team> optHTeam = teamRepository.findByIdAndCreatedByAndKindAndGender(gameDescription.getHTeamId(), userId, savedGame.getKind(), gameDescription.getGender());
            Optional<Team> optGTeam = teamRepository.findByIdAndCreatedByAndKindAndGender(gameDescription.getGTeamId(), userId, savedGame.getKind(), gameDescription.getGender());
            Optional<Rules> optRules = rulesRepository.findByIdAndCreatedByAndKind(gameDescription.getRulesId(), userId, savedGame.getKind());

            if (optHTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching home team %s for user %s", gameDescription.getHTeamId(), userId));
            } else if (optGTeam.isEmpty()) {
                throw new NotFoundException(String.format("Could not find matching guest team %s for user %s", gameDescription.getGTeamId(), userId));
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
                savedGame.setHTeam(optHTeam.get());
                savedGame.setGTeam(optGTeam.get());
                savedGame.setHSets(0);
                savedGame.setGSets(0);
                savedGame.setSets(new ArrayList<>());
                savedGame.setHCards(new ArrayList<>());
                savedGame.setGCards(new ArrayList<>());
                savedGame.setRules(optRules.get());

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
            savedGame.setHTeam(game.getHTeam());
            savedGame.setGTeam(game.getGTeam());
            savedGame.setHSets(game.getHSets());
            savedGame.setGSets(game.getGSets());
            savedGame.setSets(game.getSets());
            savedGame.setHCards(game.getHCards());
            savedGame.setGCards(game.getGCards());
            savedGame.setRules(game.getRules());

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

    private long epochDateNDaysAgo(int daysAgo) {
        return  System.currentTimeMillis() - (daysAgo * 86400000L);
    }

    private void appendCsvHeader(PrintWriter printWriter) {
        printWriter.append(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s\n",
                "Date",
                "League",
                "Division",
                "Gender",
                "Home", "Guest",
                "Sets Home", "Sets Guest",
                "Set 1 Home", "Set 1 Guest",
                "Set 2 Home", "Set 2 Guest",
                "Set 3 Home", "Set 3 Guest",
                "Set 4 Home", "Set 4 Guest",
                "Set 5 Home", "Set 5 Guest"
        ));
    }

    private void appendCsvGame(Game game, PrintWriter printWriter) {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        String[] hPoints = new String[5];
        String[] gPoints = new String[5];

        for (int index = 0; index < 5; index++) {
            if (index < game.getSets().size()) {
                Set set = game.getSets().get(index);
                hPoints[index] = String.valueOf(set.getHPoints());
                gPoints[index] = String.valueOf(set.getGPoints());
            } else {
                hPoints[index] = "";
                gPoints[index] = "";
            }
        }

        printWriter.append(String.format("\n%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s",
                formatter.format(game.getScheduledAt()),
                game.getLeagueName(),
                game.getDivisionName(),
                game.getGender(),
                game.getHTeam().getName(), game.getHTeam().getName(),
                game.getHSets(), game.getGSets(),
                hPoints[0], gPoints[0],
                hPoints[1], gPoints[1],
                hPoints[2], gPoints[2],
                hPoints[3], gPoints[3],
                hPoints[4], gPoints[4]
        ));
    }
}
