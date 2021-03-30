package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.LeagueSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.entity.User;

import java.util.List;
import java.util.UUID;

public interface LeagueService {

    // Public

    League getLeague(UUID leagueId);

    // User only

    List<LeagueSummary> listLeagues(User user, List<GameType> kinds);

    List<LeagueSummary> listLeaguesOfKind(User user, GameType kind);

    League getLeague(User user, UUID leagueId);

    Count getNumberOfLeagues(User user);

    void createLeague(User user, League league);

    void updateDivisions(User user, UUID leagueId);

    void deleteLeague(User user, UUID leagueId);

    void deleteAllLeagues(User user);
}
