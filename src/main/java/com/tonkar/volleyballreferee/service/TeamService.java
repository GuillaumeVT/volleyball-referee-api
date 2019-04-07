package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamDescription;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    // Public

    List<TeamDescription> listTeamsOfLeague(UUID leagueId);

    // User only

    List<Team> listTeams(String userId);

    List<TeamDescription> listTeamsOfKind(String userId, GameType kind);

    Team getTeam(String userId, UUID teamId) throws NotFoundException;

    Count getNumberOfTeams(String userId);

    void createTeam(String userId, Team team) throws ConflictException;

    void updateTeam(String userId, Team team) throws NotFoundException;

    void deleteTeam(String userId, UUID teamId) throws ConflictException;

    void deleteAllTeams(String userId);
}
