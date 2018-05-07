package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.*;
import com.tonkar.volleyballreferee.repository.CodeRepository;
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

    @Autowired
    private CodeRepository codeRepository;

    @Override
    public boolean hasGame(long date) {
        return gameDescriptionRepository.existsByDate(date);
    }

    @Override
    public boolean hasGameSynched(long date) {
        return gameDescriptionRepository.existsByDateAndStatus(date, GameStatus.COMPLETED.toString());
    }

    @Override
    public List<GameDescription> listGameDescriptions(String token) {
        return gameDescriptionRepository.findByHNameIgnoreCaseLikeOrGNameIgnoreCaseLikeOrLeagueIgnoreCaseLikeOrRefereeIgnoreCaseLike(token, token, token, token);
    }

    @Override
    public List<GameDescription> listGameDescriptionsBetween(long fromDate, long toDate) {
        return gameDescriptionRepository.findByDateBetween(fromDate, toDate);
    }

    @Override
    public List<GameDescription> listLiveGameDescriptions() {
        return gameDescriptionRepository.findByStatus(GameStatus.LIVE.toString());
    }

    @Override
    public GameStatistics getGameStatistics() {
        GameStatistics gameStatistics = new GameStatistics();
        gameStatistics.setGamesCount(gameDescriptionRepository.count());
        gameStatistics.setLiveGamesCount(gameDescriptionRepository.countByStatus(GameStatus.LIVE.toString()));
        return gameStatistics;
    }

    @Override
    public Game getGame(long date) {
        return gameRepository.findByDate(date);
    }

    @Override
    public Game getGameFromCode(int code) {
        final Code fullCode = codeRepository.findByCode(code);

        final Game game;

        if (fullCode == null) {
            game = null;
        } else {
            game = gameRepository.findByDate(fullCode.getDate());
        }

        return game;
    }

    @Override
    public void createGame(Game game) {
        GameDescription gameDescription = new GameDescription();
        gameDescription.setUserId(game.getUserId());
        gameDescription.setKind(game.getKind());
        gameDescription.setDate(game.getDate());
        gameDescription.setSchedule(game.getSchedule());
        gameDescription.setGender(game.getGender());
        gameDescription.setUsage(game.getUsage());
        gameDescription.setStatus(game.getStatus());
        gameDescription.setReferee(game.getReferee());
        gameDescription.setLeague(game.getLeague());
        gameDescription.setRules(game.getRules().getName());
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
        GameDescription savedGameDescription = gameDescriptionRepository.findByDate(date);
        Game savedGame = gameRepository.findByDate(date);

        if (savedGameDescription == null || savedGame == null) {
            LOGGER.error(String.format("Could not update %s game with date %d (%s vs %s) because either game or description was not found", savedGame.getKind(), date, savedGame.gethTeam().getName(), savedGame.getgTeam().getName()));
        } else {
            if (!game.getStatus().equals(savedGame.getStatus()) && GameStatus.COMPLETED.equals(game.getStatus())) {
                codeRepository.deleteByDate(game.getDate());
            }

            savedGameDescription.setKind(game.getKind());
            savedGameDescription.setSchedule(game.getSchedule());
            savedGameDescription.setGender(game.getGender());
            savedGameDescription.setUsage(game.getUsage());
            savedGameDescription.setStatus(game.getStatus());
            savedGameDescription.setReferee(game.getReferee());
            savedGameDescription.setLeague(game.getLeague());
            savedGameDescription.setRules(game.getRules().getName());
            savedGameDescription.sethName(game.gethTeam().getName());
            savedGameDescription.setgName(game.getgTeam().getName());
            savedGameDescription.sethSets(game.gethSets());
            savedGameDescription.setgSets(game.getgSets());
            gameDescriptionRepository.save(savedGameDescription);

            savedGame.setKind(game.getKind());
            savedGame.setSchedule(game.getSchedule());
            savedGame.setGender(game.getGender());
            savedGame.setUsage(game.getUsage());
            savedGame.setStatus(game.getStatus());
            savedGame.setReferee(game.getReferee());
            savedGame.setLeague(game.getLeague());
            savedGame.setRules(game.getRules());
            savedGame.sethTeam(game.gethTeam());
            savedGame.setgTeam(game.getgTeam());
            savedGame.sethSets(game.gethSets());
            savedGame.setgSets(game.getgSets());
            savedGame.setSets(game.getSets());
            savedGame.sethCards(game.gethCards());
            savedGame.setgCards(game.getgCards());
            gameRepository.save(savedGame);

            LOGGER.debug(String.format("Updated %s game with date %d (%s vs %s)", game.getKind(), date, game.gethTeam().getName(), game.getgTeam().getName()));
        }
    }

    @Override
    public void updateSet(long date, int setIndex, Set set) {
        Game savedGame = gameRepository.findByDate(date);

        if (setIndex < savedGame.getSets().size()) {
            savedGame.getSets().set(setIndex, set);
            gameRepository.save(savedGame);
            LOGGER.debug(String.format("Updated set #%d of %s game with date %d (%s vs %s)", setIndex, savedGame.getKind(), date, savedGame.gethTeam().getName(), savedGame.getgTeam().getName()));
        } else {
            LOGGER.error(String.format("Could not update %s game with date %d (%s vs %s) because set #%d does not exist", savedGame.getKind(), date, savedGame.gethTeam().getName(), savedGame.getgTeam().getName(), setIndex));
        }
    }

    @Override
    public void deleteGame(long date, UserId userId) {
        gameDescriptionRepository.deleteByDateAndUserId_SocialIdAndUserId_Provider(date, userId.getSocialId(), userId.getProvider());
        gameRepository.deleteByDateAndUserId_SocialIdAndUserId_Provider(date, userId.getSocialId(), userId.getProvider());
        codeRepository.deleteByDate(date);
    }

    @Override
    public void deleteLiveGame(long date) {
        gameDescriptionRepository.deleteByDateAndUserId_SocialIdAndUserId_ProviderAndStatus(
                date, UserId.VBR_USER_ID.getSocialId(), UserId.VBR_USER_ID.getProvider(), GameStatus.LIVE.toString());
        gameRepository.deleteByDateAndUserId_SocialIdAndUserId_ProviderAndStatus(
                date, UserId.VBR_USER_ID.getSocialId(), UserId.VBR_USER_ID.getProvider(), GameStatus.LIVE.toString());
    }

    @Override
    public void deleteOldGames(int daysAgo) {
        long dateNDaysAgo = System.currentTimeMillis() - (daysAgo * 86400000L);

        long count = gameDescriptionRepository.deleteByDateLessThanAndUserId_SocialIdAndUserId_Provider(
                dateNDaysAgo, UserId.VBR_USER_ID.getSocialId(), UserId.VBR_USER_ID.getProvider());
        LOGGER.debug(String.format("Deleted %d game descriptions older than date %d", count, dateNDaysAgo));

        count = gameRepository.deleteByDateLessThanAndUserId_SocialIdAndUserId_Provider(
                dateNDaysAgo, UserId.VBR_USER_ID.getSocialId(), UserId.VBR_USER_ID.getProvider());
        LOGGER.debug(String.format("Deleted %d games older than date %d", count, dateNDaysAgo));
    }

    @Override
    public void deleteOldLiveGames(int daysAgo) {
        long dateNDaysAgo = System.currentTimeMillis() - (daysAgo * 86400000L);

        long count = gameDescriptionRepository.deleteByDateLessThanAndUserId_SocialIdAndUserId_ProviderAndStatus(
                dateNDaysAgo, UserId.VBR_USER_ID.getSocialId(), UserId.VBR_USER_ID.getProvider(), GameStatus.LIVE.toString());
        LOGGER.debug(String.format("Deleted %d live game descriptions older than date %d", count, dateNDaysAgo));

        count = gameRepository.deleteByDateLessThanAndUserId_SocialIdAndUserId_ProviderAndStatus(
                dateNDaysAgo, UserId.VBR_USER_ID.getSocialId(), UserId.VBR_USER_ID.getProvider(), GameStatus.LIVE.toString());
        LOGGER.debug(String.format("Deleted %d live games older than date %d", count, dateNDaysAgo));
    }

    @Override
    public void deleteOldCodes(int daysAgo) {
        long timeNDaysAgo = System.currentTimeMillis() - (daysAgo * 86400000L);

        long count = codeRepository.deleteByDateLessThan(timeNDaysAgo);
        LOGGER.debug(String.format("Deleted %d codes older than date %d", count, timeNDaysAgo));
    }

    @Override
    public void deleteTestGames(int setDurationMinutesUnder) {
        long setDurationMillisUnder = setDurationMinutesUnder * 60000L;

        List<Game> games = gameRepository.findByUserId_SocialIdAndUserId_ProviderAndStatusAndSets_DurationLessThan(
                UserId.VBR_USER_ID.getSocialId(), UserId.VBR_USER_ID.getProvider(), GameStatus.COMPLETED.toString(), setDurationMillisUnder);

        for (Game game : games) {
            boolean delete = true;

            for (Set set : game.getSets()) {
                delete = delete && (set.getDuration() < setDurationMillisUnder);
            }

            if (delete) {
                deleteGame(game.getDate(), UserId.VBR_USER_ID);
                LOGGER.debug(String.format("Deleted game at date %d with set shorter than %d minutes", game.getDate(), setDurationMinutesUnder));
            }
        }
    }

    @Override
    public boolean hasGameUsingRules(String rulesName, UserId userId) {
        return gameDescriptionRepository.existsByUserId_SocialIdAndUserId_ProviderAndStatusAndRules(
                userId.getSocialId(), userId.getProvider(), GameStatus.SCHEDULED.toString(), rulesName);
    }

    @Override
    public List<GameDescription> listGameDescriptionsUsingRules(String rulesName, UserId userId) {
        return gameDescriptionRepository.findByUserId_SocialIdAndUserId_ProviderAndStatusAndRules(
                userId.getSocialId(), userId.getProvider(), GameStatus.SCHEDULED.toString(), rulesName);
    }

    @Override
    public boolean hasGameUsingTeam(String teamName, UserId userId) {
        return gameDescriptionRepository.existsByUserId_SocialIdAndUserId_ProviderAndStatusAndHName(
                userId.getSocialId(), userId.getProvider(), GameStatus.SCHEDULED.toString(), teamName)
                || gameDescriptionRepository.existsByUserId_SocialIdAndUserId_ProviderAndStatusAndGName(
                userId.getSocialId(), userId.getProvider(), GameStatus.SCHEDULED.toString(), teamName);
    }

    @Override
    public List<GameDescription> listGameDescriptionsUsingTeam(String teamName, UserId userId) {
        return gameDescriptionRepository.findByUserId_SocialIdAndUserId_ProviderAndStatusAndTeamName(
                userId.getSocialId(), userId.getProvider(), GameStatus.SCHEDULED.toString(), teamName);
    }

}
