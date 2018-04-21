package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.*;

import java.util.List;

public interface GameService {

    boolean hasGame(long date);

    boolean hasGameSynched(long date);

    List<GameDescription> listGameDescriptions(String token);

    List<GameDescription> listGameDescriptionsBetween(long fromDate, long toDate);

    List<GameDescription> listLiveGameDescriptions();

    GameStatistics getGameStatistics();

    Game getGame(long date);

    Game getGame(int code);

    void createGame(Game game);

    void updateGame(long date, Game game);

    void updateSet(long date, int setIndex, Set set);

    void deleteGame(long date, UserId userId);

    void deleteLiveGame(long date);

    void deleteOldGames(int daysAgo);

    void deleteOldLiveGames(int daysAgo);

    void deleteOldCodes(int daysAgo);

    void deleteTestGames(int setDurationMinutesUnder);

    boolean hasGameUsingRules(String rulesName, UserId userId);

    List<GameDescription> listGameDescriptionsUsingRules(String rulesName, UserId userId);

    boolean hasGameUsingTeam(String teamName, UserId userId);

    List<GameDescription> listGameDescriptionsUsingTeam(String teamName, UserId userId);

}
