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

    Game getGameFromCode(int code);

    void createGame(Game game);

    void updateGame(long date, Game game);

    void updateSet(long date, int setIndex, Set set);

    void deleteGame(long date, String userId);

    void deleteLiveGame(long date);

    void deletePublicGames(int daysAgo);

    void deleteOldLiveGames(int daysAgo);

    void deletePublicLiveGames(int daysAgo);

    void deleteOldCodes(int daysAgo);

    void deletePublicTestGames(int setDurationMinutesUnder);

    boolean hasGameUsingRules(String rulesName, String userId);

    List<GameDescription> listGameDescriptionsUsingRules(String rulesName, String userId);

    boolean hasGameUsingTeam(String teamName, String userId);

    List<GameDescription> listGameDescriptionsUsingTeam(String teamName, String userId);

}
