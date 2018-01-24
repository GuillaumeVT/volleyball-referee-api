package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.Game;
import com.tonkar.volleyballreferee.model.GameDescription;
import com.tonkar.volleyballreferee.model.GameStatistics;
import com.tonkar.volleyballreferee.model.Set;

import java.util.List;

public interface GameService {

    boolean hasGame(long date);

    List<GameDescription> listGameDescriptions(String token);

    List<GameDescription> listGameDescriptionsBetween(long fromDate, long toDate);

    List<GameDescription> listLiveGameDescriptions();

    GameStatistics getGameStatistics();

    Game getGame(long date);

    void createGame(Game game);

    void updateGame(long date, Game game);

    void updateSet(long date, int setIndex, Set set);

    void deleteGame(long date);

    void deleteLiveGame(long date);

    void deleteOldGames(int daysAgo);

    void deleteOldLiveGames(int daysAgo);

}
