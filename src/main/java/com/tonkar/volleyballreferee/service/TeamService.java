package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamDescription;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    // Public

    List<TeamDescription> listTeamsOfLeague(UUID leagueId);

    List<TeamDescription> listTeamsOfDivision(UUID leagueId, String divisionName);

    // User only

    List<TeamDescription> listTeams(User user);

    List<TeamDescription> listTeamsOfKind(User user, GameType kind);

    Team getTeam(User user, UUID teamId) throws NotFoundException;

    Count getNumberOfTeams(User user);

    void createTeam(User user, Team team) throws ConflictException;

    void updateTeam(User user, Team team) throws NotFoundException;

    void deleteTeam(User user, UUID teamId) throws ConflictException;

    void deleteAllTeams(User user);
}
