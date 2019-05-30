package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.GameDao;
import com.tonkar.volleyballreferee.dao.TeamDao;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamDescription;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.entity.User;
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
    public List<TeamDescription> listTeams(User user) {
        return teamDao.listTeams(user.getId());
    }

    @Override
    public List<TeamDescription> listTeamsOfKind(User user, GameType kind) {
        return teamDao.listTeamsOfKind(user.getId(), kind);
    }

    @Override
    public Team getTeam(User user, UUID teamId) throws NotFoundException {
        return teamRepository
                .findByIdAndCreatedBy(teamId, user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find team %s for user %s", teamId, user.getId())));
    }

    @Override
    public Count getNumberOfTeams(User user) {
        return new Count(teamRepository.countByCreatedBy(user.getId()));
    }

    @Override
    public void createTeam(User user, Team team) throws ConflictException {
        if (teamRepository.existsById(team.getId())) {
            throw new ConflictException(String.format("Could not create team %s for user %s because it already exists", team.getId(), user.getId()));
        } else if (teamRepository.existsByCreatedByAndNameAndKindAndGender(user.getId(), team.getName(), team.getKind(), team.getGender())) {
            throw new ConflictException(String.format("Could not create team %s %s %s for user %s because it already exists", team.getName(), team.getKind(), team.getGender(), user.getId()));
        } else {
            team.setCreatedBy(user.getId());
            team.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            teamRepository.save(team);
        }
    }

    @Override
    public void updateTeam(User user, Team team) throws NotFoundException {
        Team savedTeam = teamRepository
                .findByIdAndCreatedBy(team.getId(), user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find team %s %s %s for user %s", team.getName(), team.getKind(), team.getGender(), user.getId())));

        savedTeam.setUpdatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        savedTeam.setColor(team.getColor());
        savedTeam.setLiberoColor(team.getLiberoColor());
        savedTeam.setPlayers(team.getPlayers());
        savedTeam.setLiberos(team.getLiberos());
        savedTeam.setCaptain(team.getCaptain());
        teamRepository.save(savedTeam);

        updateScheduledGamesWithTeam(user, savedTeam);
    }

    private void updateScheduledGamesWithTeam(User user, Team team) {
        teamDao.updateScheduledGamesWithHomeTeam(user.getId(), team);
        teamDao.updateScheduledGamesWithGuestTeam(user.getId(), team);
    }

    @Override
    public void deleteTeam(User user, UUID teamId) throws ConflictException {
        if (gameRepository.existsByCreatedByAndTeamAndStatus(user.getId(), teamId, GameStatus.SCHEDULED)) {
            throw new ConflictException(String.format("Could not delete team %s for user %s because it is used in a game", teamId, user.getId()));
        } else {
            teamRepository.deleteByIdAndCreatedBy(teamId, user.getId());
        }
    }

    @Override
    public void deleteAllTeams(User user) {
        teamRepository.findByCreatedByOrderByNameAsc(user.getId()).forEach(team -> {
            if (!gameRepository.existsByCreatedByAndTeamAndStatus(user.getId(), team.getId(), GameStatus.SCHEDULED)) {
                teamRepository.delete(team);
            }
        });
    }
}
