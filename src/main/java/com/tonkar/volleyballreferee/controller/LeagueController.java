package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.LeagueSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.service.LeagueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
@CrossOrigin("*")
@Slf4j
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    @GetMapping(value = "/leagues", produces = {"application/json"})
    public ResponseEntity<List<LeagueSummary>> listLeagues(@AuthenticationPrincipal User user,
                                                           @RequestParam(value = "kind", required = false) List<GameType> kinds) {
        return new ResponseEntity<>(leagueService.listLeagues(user, kinds), HttpStatus.OK);
    }

    @GetMapping(value = "/leagues/{leagueId}", produces = {"application/json"})
    public ResponseEntity<League> getLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) throws NotFoundException {
        return new ResponseEntity<>(leagueService.getLeague(user, leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/leagues/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfLeagues(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(leagueService.getNumberOfLeagues(user), HttpStatus.OK);
    }

    @PostMapping(value = "/leagues", produces = {"application/json"})
    public ResponseEntity<Void> createLeague(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody League league) throws ConflictException {
        leagueService.createLeague(user, league);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/leagues/{leagueId}", produces = {"application/json"})
    public ResponseEntity<Void> deleteLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) throws ConflictException {
        leagueService.deleteLeague(user, leagueId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/leagues", produces = {"application/json"})
    public ResponseEntity<Void> deleteAllLeagues(@AuthenticationPrincipal User user) {
        leagueService.deleteAllLeagues(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
