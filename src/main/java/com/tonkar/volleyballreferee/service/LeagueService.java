package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface LeagueService {

    // Public

    League getLeague(UUID leagueId) throws NotFoundException;

    // User only

    List<League> listLeagues(String userId);

    List<League> listLeaguesOfKind(String userId, GameType kind);

    League getLeague(String userId, UUID leagueId) throws NotFoundException;

    Count getNumberOfLeagues(String userId);

    void createLeague(String userId, League league) throws ConflictException;

    void updateDivisions(String userId, UUID leagueId) throws NotFoundException;

    void deleteLeague(String userId, UUID leagueId) throws ConflictException;

}
