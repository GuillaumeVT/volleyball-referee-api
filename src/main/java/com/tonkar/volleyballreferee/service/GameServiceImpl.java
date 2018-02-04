package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.Game;
import com.tonkar.volleyballreferee.model.GameDescription;
import com.tonkar.volleyballreferee.model.GameStatistics;
import com.tonkar.volleyballreferee.model.Set;
import com.tonkar.volleyballreferee.repository.GameDescriptionRepository;
import com.tonkar.volleyballreferee.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameServiceImpl.class);

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameDescriptionRepository gameDescriptionRepository;

    @Override
    public boolean hasGame(long date) {
        return gameDescriptionRepository.existsByDate(date);
    }

    @Override
    public boolean hasGameSynched(long date) {
        return gameDescriptionRepository.existsByDateAndLive(date, false);
    }

    @Override
    public List<GameDescription> listGameDescriptions(String token) {
        return gameDescriptionRepository.findGameDescriptionsByHNameIgnoreCaseLikeOrGNameIgnoreCaseLikeOrLeagueIgnoreCaseLike(token, token, token);
    }

    @Override
    public List<GameDescription> listGameDescriptionsBetween(long fromDate, long toDate) {
        return gameDescriptionRepository.findGameDescriptionsByDateBetween(fromDate, toDate);
    }

    @Override
    public List<GameDescription> listLiveGameDescriptions() {
        return gameDescriptionRepository.findGameDescriptionsByLive(true);
    }

    @Override
    public GameStatistics getGameStatistics() {
        GameStatistics gameStatistics = new GameStatistics();
        gameStatistics.setGamesCount(gameDescriptionRepository.count());
        gameStatistics.setLiveGamesCount(gameDescriptionRepository.countByLive(true));
        return gameStatistics;
    }

    @Override
    public Game getGame(long date) {
        return gameRepository.findGameByDate(date);
    }

    @Override
    public void createGame(Game game) {
        GameDescription gameDescription = new GameDescription();
        gameDescription.setKind(game.getKind());
        gameDescription.setDate(game.getDate());
        gameDescription.setGender(game.getGender());
        gameDescription.setUsage(game.getUsage());
        gameDescription.setLive(game.isLive());
        gameDescription.setLeague(game.getLeague());
        gameDescription.sethName(game.gethTeam().getName());
        gameDescription.setgName(game.getgTeam().getName());
        gameDescription.sethSets(game.gethSets());
        gameDescription.setgSets(game.getgSets());

        gameDescriptionRepository.insert(gameDescription);
        gameRepository.insert(game);

        LOGGER.debug(String.format("Created %s game with date %d (%s vs %s)", game.getKind(), game.getDate(), game.gethTeam().getName(), game.getgTeam().getName()));
    }

    @Override
    public void updateGame(long date, Game game) {
        GameDescription savedGameDescription = gameDescriptionRepository.findGameDescriptionByDate(date);
        Game savedGame = gameRepository.findGameByDate(date);

        if (savedGameDescription == null || savedGame == null) {
            LOGGER.error(String.format("Could not update %s game with date %d (%s vs %s) because either game or description was not found", savedGame.getKind(), date, savedGame.gethTeam().getName(), savedGame.getgTeam().getName()));
        } else {
            savedGameDescription.setLive(game.isLive());
            savedGameDescription.sethSets(game.gethSets());
            savedGameDescription.setgSets(game.getgSets());
            gameDescriptionRepository.save(savedGameDescription);

            savedGame.setLive(game.isLive());
            savedGame.sethTeam(game.gethTeam());
            savedGame.setgTeam(game.getgTeam());
            savedGame.sethSets(game.gethSets());
            savedGame.setgSets(game.getgSets());
            savedGame.setSets(game.getSets());
            gameRepository.save(savedGame);

            LOGGER.debug(String.format("Updated %s game with date %d (%s vs %s)", game.getKind(), date, game.gethTeam().getName(), game.getgTeam().getName()));
        }
    }

    @Override
    public void updateSet(long date, int setIndex, Set set) {
        Game savedGame = gameRepository.findGameByDate(date);

        if (setIndex < savedGame.getSets().size()) {
            savedGame.getSets().set(setIndex, set);
            gameRepository.save(savedGame);
            LOGGER.debug(String.format("Updated set #%d of %s game with date %d (%s vs %s)", setIndex, savedGame.getKind(), date, savedGame.gethTeam().getName(), savedGame.getgTeam().getName()));
        } else {
            LOGGER.error(String.format("Could not update %s game with date %d (%s vs %s) because set #%d does not exist", savedGame.getKind(), date, savedGame.gethTeam().getName(), savedGame.getgTeam().getName(), setIndex));
        }
    }

    @Override
    public void deleteGame(long date) {
        gameDescriptionRepository.deleteGameDescriptionByDate(date);
        gameRepository.deleteGameByDate(date);
    }

    @Override
    public void deleteLiveGame(long date) {
        gameDescriptionRepository.deleteGameDescriptionByLiveAndDate(true, date);
        gameRepository.deleteGameByLiveAndDate(true, date);
    }

    @Override
    public void deleteOldGames(int daysAgo) {
        long time2DaysAgo = System.currentTimeMillis() - (daysAgo * 86400000L);

        long count = gameDescriptionRepository.deleteGameDescriptionsByDateLessThan(time2DaysAgo);
        LOGGER.debug(String.format("Deleted %d game descriptions older than date %d", count, time2DaysAgo));

        count = gameRepository.deleteGamesByDateLessThan(time2DaysAgo);
        LOGGER.debug(String.format("Deleted %d games older than date %d", count, time2DaysAgo));
    }

    @Override
    public void deleteOldLiveGames(int daysAgo) {
        long time2DaysAgo = System.currentTimeMillis() - (daysAgo * 86400000L);

        long count = gameDescriptionRepository.deleteGameDescriptionsByLiveAndDateLessThan(true, time2DaysAgo);
        LOGGER.debug(String.format("Deleted %d live game descriptions older than date %d", count, time2DaysAgo));

        count = gameRepository.deleteGamesByLiveAndDateLessThan(true, time2DaysAgo);
        LOGGER.debug(String.format("Deleted %d live games older than date %d", count, time2DaysAgo));
    }

    @Override
    public void deleteTestGames(int setDurationMinutesUnder) {
        long setDurationMillisUnder = setDurationMinutesUnder * 60000L;

        List<Game> games = gameRepository.findGamesByLiveAndSets_DurationLessThan(false, setDurationMillisUnder);

        for (Game game : games) {
            boolean delete = true;

            for (Set set : game.getSets()) {
                delete = delete && (set.getDuration() < setDurationMillisUnder);
            }

            if (delete) {
                deleteGame(game.getDate());
                LOGGER.debug(String.format("Deleted game at date %d with set shorter than %d minutes", game.getDate(), setDurationMinutesUnder));
            }
        }
    }

}
