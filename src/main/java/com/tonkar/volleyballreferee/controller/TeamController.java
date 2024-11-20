package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.TeamService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping(value = "/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<TeamSummaryDto>> listTeams(@AuthenticationPrincipal User user,
                                                          @RequestParam(value = "kind", required = false) java.util.Set<GameType> kinds,
                                                          @RequestParam(value = "gender", required = false) java.util.Set<GenderType> genders,
                                                          @RequestParam("page") @Min(0) int page,
                                                          @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(teamService.listTeams(user, kinds, genders, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/teams/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Team> getTeam(@AuthenticationPrincipal User user, @PathVariable("teamId") UUID teamId) {
        return new ResponseEntity<>(teamService.getTeam(user, teamId), HttpStatus.OK);
    }

    @GetMapping(value = "/teams/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CountDto> getNumberOfTeams(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(teamService.getNumberOfTeams(user), HttpStatus.OK);
    }

    @PostMapping(value = "/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createTeam(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Team team) {
        teamService.createTeam(user, team);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateTeam(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Team team) {
        teamService.updateTeam(user, team);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/teams/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteTeam(@AuthenticationPrincipal User user, @PathVariable("teamId") UUID teamId) {
        teamService.deleteTeam(user, teamId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAllTeams(@AuthenticationPrincipal User user) {
        teamService.deleteAllTeams(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
