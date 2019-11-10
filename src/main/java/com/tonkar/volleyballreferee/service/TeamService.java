package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    // Public

    List<TeamSummary> listTeamsOfLeague(UUID leagueId);

    List<TeamSummary> listTeamsOfDivision(UUID leagueId, String divisionName);

    // User only

    Page<TeamSummary> listTeams(User user, List<GameType> kinds, List<GenderType> genders, Pageable pageable);

    List<TeamSummary> listTeamsOfKind(User user, GameType kind);

    Team getTeam(User user, UUID teamId) throws NotFoundException;

    Count getNumberOfTeams(User user);

    void createTeam(User user, Team team) throws ConflictException;

    void updateTeam(User user, Team team) throws NotFoundException;

    void deleteTeam(User user, UUID teamId) throws ConflictException;

    void deleteAllTeams(User user);
}
