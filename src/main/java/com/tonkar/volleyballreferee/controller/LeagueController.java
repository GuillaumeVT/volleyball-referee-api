package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.LeagueService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.*;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    @GetMapping(value = "/leagues", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LeagueSummaryDto>> listLeagues(@AuthenticationPrincipal User user,
                                                              @RequestParam(value = "kind", required = false) Set<GameType> kinds) {
        return new ResponseEntity<>(leagueService.listLeagues(user, kinds), HttpStatus.OK);
    }

    @GetMapping(value = "/leagues/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<League> getLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(leagueService.getLeague(user, leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/leagues/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CountDto> getNumberOfLeagues(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(leagueService.getNumberOfLeagues(user), HttpStatus.OK);
    }

    @PostMapping(value = "/leagues", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createLeague(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody League league) {
        leagueService.createLeague(user, league);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/leagues/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        leagueService.deleteLeague(user, leagueId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/leagues", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAllLeagues(@AuthenticationPrincipal User user) {
        leagueService.deleteAllLeagues(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
