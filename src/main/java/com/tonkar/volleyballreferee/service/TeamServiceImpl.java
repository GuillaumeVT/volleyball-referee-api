package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dao.TeamDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamDescription;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.GameRepository;
import com.tonkar.volleyballreferee.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamDao teamDao;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameDao gameDao;

    @Override
    public List<TeamDescription> listTeamsOfLeague(UUID leagueId) {
        Set<UUID> teamIds = new TreeSet<>();

        gameDao.listGamesInLeague(leagueId).forEach(gameDescription -> {
            teamIds.add(gameDescription.getHomeTeamId());
            teamIds.add(gameDescription.getGuestTeamId());
        });

        return teamDao.listTeamsWithIds(teamIds);
    }

    @Override
    public List<TeamDescription> listTeamsOfDivision(UUID leagueId, String divisionName) {
        Set<UUID> teamIds = new TreeSet<>();

        gameDao.listGamesInDivision(leagueId, divisionName).forEach(gameDescription -> {
            teamIds.add(gameDescription.getHomeTeamId());
            teamIds.add(gameDescription.getGuestTeamId());
        });

        return teamDao.listTeamsWithIds(teamIds);
    }

    @Override
    public List<TeamDescription> listTeams(String userId) {
        return teamDao.listTeams(userId);
    }

    @Override
    public List<TeamDescription> listTeamsOfKind(String userId, GameType kind) {
        return teamDao.listTeamsOfKind(userId, kind);
    }

    @Override
    public Team getTeam(String userId, UUID teamId) throws NotFoundException {
        return teamRepository
                .findByIdAndCreatedBy(teamId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find team %s for user %s", teamId, userId)));
    }

    @Override
    public Count getNumberOfTeams(String userId) {
        return new Count(teamRepository.countByCreatedBy(userId));
    }

    @Override
    public void createTeam(String userId, Team team) throws ConflictException {
        if (teamRepository.existsById(team.getId())) {
            throw new ConflictException(String.format("Could not create team %s for user %s because it already exists", team.getId(), userId));
        } else if (teamRepository.existsByCreatedByAndNameAndKindAndGender(userId, team.getName(), team.getKind(), team.getGender())) {
            throw new ConflictException(String.format("Could not create team %s %s %s for user %s because it already exists", team.getName(), team.getKind(), team.getGender(), userId));
        } else {
            team.setCreatedBy(userId);
            team.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            teamRepository.save(team);
        }
    }

    @Override
    public void updateTeam(String userId, Team team) throws NotFoundException {
        Team savedTeam = teamRepository
                .findByIdAndCreatedBy(team.getId(), userId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find team %s %s %s for user %s", team.getName(), team.getKind(), team.getGender(), userId)));

        savedTeam.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        savedTeam.setColor(team.getColor());
        savedTeam.setLiberoColor(team.getLiberoColor());
        savedTeam.setPlayers(team.getPlayers());
        savedTeam.setLiberos(team.getLiberos());
        savedTeam.setCaptain(team.getCaptain());
        teamRepository.save(savedTeam);

        updateScheduledGamesWithTeam(userId, savedTeam);
    }

    private void updateScheduledGamesWithTeam(String userId, Team team) {
        teamDao.updateScheduledGamesWithHomeTeam(userId, team);
        teamDao.updateScheduledGamesWithGuestTeam(userId, team);
    }

    @Override
    public void deleteTeam(String userId, UUID teamId) throws ConflictException {
        if (gameRepository.existsByCreatedByAndTeamAndStatus(userId, teamId, GameStatus.SCHEDULED)) {
            throw new ConflictException(String.format("Could not delete team %s for user %s because it is used in a game", teamId, userId));
        } else {
            teamRepository.deleteByIdAndCreatedBy(teamId, userId);
        }
    }

    @Override
    public void deleteAllTeams(String userId) {
        teamRepository.findByCreatedByOrderByNameAsc(userId).forEach(team -> {
            if (!gameRepository.existsByCreatedByAndTeamAndStatus(userId, team.getId(), GameStatus.SCHEDULED)) {
                teamRepository.delete(team);
            }
        });
    }
}
