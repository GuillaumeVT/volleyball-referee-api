package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
@CrossOrigin("*")
@Slf4j
public class TeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping(value = "/teams", produces = {"application/json"})
    public ResponseEntity<Page<TeamSummary>> listTeams(@AuthenticationPrincipal User user,
                                                       @RequestParam(value = "kind", required = false) List<GameType> kinds,
                                                       @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                       @RequestParam("page") @Min(0) int page,
                                                       @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(teamService.listTeams(user, kinds, genders, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/teams/{teamId}", produces = {"application/json"})
    public ResponseEntity<Team> getTeam(@AuthenticationPrincipal User user, @PathVariable("teamId") UUID teamId) throws NotFoundException {
        return new ResponseEntity<>(teamService.getTeam(user, teamId), HttpStatus.OK);
    }

    @GetMapping(value = "/teams/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfTeams(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(teamService.getNumberOfTeams(user), HttpStatus.OK);
    }

    @PostMapping(value = "/teams", produces = {"application/json"})
    public ResponseEntity<Void> createTeam(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Team team) throws ConflictException {
        teamService.createTeam(user, team);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/teams", produces = {"application/json"})
    public ResponseEntity<Void> updateTeam(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Team team) throws NotFoundException {
        teamService.updateTeam(user, team);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/teams/{teamId}", produces = {"application/json"})
    public ResponseEntity<Void> deleteTeam(@AuthenticationPrincipal User user, @PathVariable("teamId") UUID teamId) throws ConflictException {
        teamService.deleteTeam(user, teamId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/teams", produces = {"application/json"})
    public ResponseEntity<Void> deleteAllTeams(@AuthenticationPrincipal User user) {
        teamService.deleteAllTeams(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
