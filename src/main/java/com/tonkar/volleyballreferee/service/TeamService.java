package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.*;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamDao teamDao;
    private final GameDao gameDao;

    public List<TeamSummaryDto> listTeamsOfLeague(UUID leagueId) {
        Set<UUID> teamIds = new TreeSet<>();

        gameDao.listGamesInLeague(leagueId).forEach(game -> {
            teamIds.add(game.getHomeTeamId());
            teamIds.add(game.getGuestTeamId());
        });

        return teamDao.listTeamsWithIds(teamIds);
    }

    public List<TeamSummaryDto> listTeamsOfDivision(UUID leagueId, String divisionName) {
        Set<UUID> teamIds = new TreeSet<>();

        gameDao.listGamesInDivision(leagueId, divisionName).forEach(game -> {
            teamIds.add(game.getHomeTeamId());
            teamIds.add(game.getGuestTeamId());
        });

        return teamDao.listTeamsWithIds(teamIds);
    }

    public Page<TeamSummaryDto> listTeams(User user, java.util.Set<GameType> kinds, java.util.Set<GenderType> genders, Pageable pageable) {
        return teamDao.listTeams(user.getId(), kinds, genders, pageable);
    }

    public List<TeamSummaryDto> listTeamsOfKind(User user, GameType kind) {
        return teamDao.listTeamsOfKind(user.getId(), kind);
    }

    public Team getTeam(User user, UUID teamId) {
        return teamDao
                .findByIdAndCreatedBy(teamId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find team %s for user %s", teamId, user.getId())));
    }

    public CountDto getNumberOfTeams(User user) {
        return new CountDto(teamDao.countByCreatedBy(user.getId()));
    }

    public void createTeam(User user, Team team) {
        if (teamDao.existsById(team.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not create team %s for user %s because it already exists", team.getId(),
                                                            user.getId()));
        } else if (teamDao.existsByCreatedByAndNameAndKindAndGender(user.getId(), team.getName(), team.getKind(), team.getGender())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not create team %s %s %s for user %s because it already exists",
                                                            team.getName(), team.getKind(), team.getGender(), user.getId()));
        } else {
            team.setCreatedBy(user.getId());
            team.setUpdatedAt(Instant.now().toEpochMilli());
            teamDao.save(team);
        }
    }

    public void updateTeam(User user, Team team) {
        Team savedTeam = teamDao
                .findByIdAndCreatedBy(team.getId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find team %s %s %s for user %s", team.getId(),
                                                                             team.getKind(), team.getGender(), user.getId())));

        savedTeam.setUpdatedAt(Instant.now().toEpochMilli());
        savedTeam.setName(team.getName());
        savedTeam.setGender(team.getGender());
        savedTeam.setColor(team.getColor());
        savedTeam.setLiberoColor(team.getLiberoColor());
        savedTeam.setPlayers(team.getPlayers());
        savedTeam.setLiberos(team.getLiberos());
        savedTeam.setCaptain(team.getCaptain());
        savedTeam.setCoach(team.getCoach());
        teamDao.save(savedTeam);

        updateScheduledGamesWithTeam(user, savedTeam);
    }

    private void updateScheduledGamesWithTeam(User user, Team team) {
        teamDao.updateScheduledGamesWithHomeTeam(user.getId(), team);
        teamDao.updateScheduledGamesWithGuestTeam(user.getId(), team);
    }

    public void deleteTeam(User user, UUID teamId) {
        if (gameDao.existsByCreatedByAndTeamAndStatus(user.getId(), teamId, GameStatus.SCHEDULED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Could not delete team %s for user %s because it is used in a game", teamId,
                                                            user.getId()));
        } else {
            teamDao.deleteByIdAndCreatedBy(teamId, user.getId());
        }
    }

    public void deleteAllTeams(User user) {
        teamDao.findByCreatedByOrderByNameAsc(user.getId()).forEach(team -> {
            if (!gameDao.existsByCreatedByAndRules_IdAndStatus(user.getId(), team.id(), GameStatus.SCHEDULED)) {
                teamDao.deleteByIdAndCreatedBy(team.id(), user.getId());
            }
        });
    }
}
