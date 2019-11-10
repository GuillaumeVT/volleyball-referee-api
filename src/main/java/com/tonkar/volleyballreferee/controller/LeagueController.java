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
@RequestMapping("/api/v3.2/leagues")
@CrossOrigin("*")
@Slf4j
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<List<LeagueSummary>> listLeagues(@AuthenticationPrincipal User user,
                                                           @RequestParam(value = "kind", required = false) List<GameType> kinds) {
        return new ResponseEntity<>(leagueService.listLeagues(user, kinds), HttpStatus.OK);
    }

    @GetMapping(value = "/{leagueId}", produces = {"application/json"})
    public ResponseEntity<League> getLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        try {
            return new ResponseEntity<>(leagueService.getLeague(user, leagueId), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfLeagues(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(leagueService.getNumberOfLeagues(user), HttpStatus.OK);
    }

    @PostMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> createLeague(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody League league) {
        try {
            leagueService.createLeague(user, league);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = "/{leagueId}", produces = {"application/json"})
    public ResponseEntity<String> deleteLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        try {
            leagueService.deleteLeague(user, leagueId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

}
