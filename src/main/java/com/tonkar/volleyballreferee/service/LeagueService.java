package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.LeagueDescription;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface LeagueService {

    // Public

    League getLeague(UUID leagueId) throws NotFoundException;

    // User only

    List<LeagueDescription> listLeagues(User user);

    List<LeagueDescription> listLeaguesOfKind(User user, GameType kind);

    League getLeague(User user, UUID leagueId) throws NotFoundException;

    Count getNumberOfLeagues(User user);

    void createLeague(User user, League league) throws ConflictException;

    void updateDivisions(User user, UUID leagueId) throws NotFoundException;

    void deleteLeague(User user, UUID leagueId) throws ConflictException;

}
