package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.LeagueDescription;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/leagues")
@CrossOrigin("*")
@Slf4j
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<List<LeagueDescription>> listLeagues(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(leagueService.listLeagues(user.getId()), HttpStatus.OK);
    }

    @GetMapping(value = "/kind/{kind}", produces = {"application/json"})
    public ResponseEntity<List<LeagueDescription>> listTeamsOfKind(@AuthenticationPrincipal User user, @PathVariable("kind") GameType kind) {
        return new ResponseEntity<>(leagueService.listLeaguesOfKind(user.getId(), kind), HttpStatus.OK);
    }

    @GetMapping(value = "/{leagueId}", produces = {"application/json"})
    public ResponseEntity<League> getLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        try {
            return new ResponseEntity<>(leagueService.getLeague(user.getId(), leagueId), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfLeagues(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(leagueService.getNumberOfLeagues(user.getId()), HttpStatus.OK);
    }

    @PostMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> createLeague(@AuthenticationPrincipal User user, @Valid @RequestBody League league) {
        try {
            leagueService.createLeague(user.getId(), league);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = "/{leagueId}", produces = {"application/json"})
    public ResponseEntity<String> deleteLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        try {
            leagueService.deleteLeague(user.getId(), leagueId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

}
