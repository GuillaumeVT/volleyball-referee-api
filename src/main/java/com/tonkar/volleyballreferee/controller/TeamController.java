package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamDescription;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/teams")
@CrossOrigin("*")
@Slf4j
public class TeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<List<TeamDescription>> listTeams(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(teamService.listTeams(user.getId()), HttpStatus.OK);
    }

    @GetMapping(value = "/kind/{kind}", produces = {"application/json"})
    public ResponseEntity<List<TeamDescription>> listTeamsOfKind(@AuthenticationPrincipal User user, @PathVariable("kind") GameType kind) {
        return new ResponseEntity<>(teamService.listTeamsOfKind(user.getId(), kind), HttpStatus.OK);
    }

    @GetMapping(value = "/{teamId}", produces = {"application/json"})
    public ResponseEntity<Team> getTeam(@AuthenticationPrincipal User user, @PathVariable("teamId") UUID teamId) {
        try {
            return new ResponseEntity<>(teamService.getTeam(user.getId(), teamId), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfTeams(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(teamService.getNumberOfTeams(user.getId()), HttpStatus.OK);
    }

    @PostMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> createTeam(@AuthenticationPrincipal User user, @Valid @RequestBody Team team) {
        try {
            teamService.createTeam(user.getId(), team);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> updateTeam(@AuthenticationPrincipal User user, @Valid @RequestBody Team team) {
        try {
            teamService.updateTeam(user.getId(), team);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/{teamId}", produces = {"application/json"})
    public ResponseEntity<String> deleteTeam(@AuthenticationPrincipal User user, @PathVariable("teamId") UUID teamId) {
        try {
            teamService.deleteTeam(user.getId(), teamId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> deleteAllTeams(@AuthenticationPrincipal User user) {
        teamService.deleteAllTeams(user.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
