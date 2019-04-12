package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.GameDescription;
import com.tonkar.volleyballreferee.dto.Statistics;
import com.tonkar.volleyballreferee.dto.TeamDescription;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.entity.Message;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.scoresheet.ScoreSheet;
import com.tonkar.volleyballreferee.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/public")
@CrossOrigin("*")
@Slf4j
public class PublicController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private GameService gameService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private MessageService messageService;

    @GetMapping(value = "/statistics", produces = {"application/json"})
    public ResponseEntity<Statistics> getStatistics() {
        return new ResponseEntity<>(statisticsService.getStatistics(), HttpStatus.OK);
    }

    @GetMapping(value = "/games/{gameId}", produces = {"application/json"})
    public ResponseEntity<Game> getGame(@PathVariable("gameId") UUID gameId) {
        try {
            return new ResponseEntity<>(gameService.getGame(gameId), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/games/{gameId}/score-sheet", produces = {"application/json"})
    public ResponseEntity<?> getScoreSheet(@PathVariable("gameId") UUID gameId) {
        try {
            ScoreSheet scoreSheet = gameService.getScoreSheet(gameId);
            ByteArrayResource resource = new ByteArrayResource(scoreSheet.getData());
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + scoreSheet.getFilename())
                    .contentType(MediaType.TEXT_HTML)
                    .contentLength(scoreSheet.getData().length)
                    .body(resource);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/games/live", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listLiveGames() {
        return new ResponseEntity<>(gameService.listLiveGames(), HttpStatus.OK);
    }

    @GetMapping(value = "/games/token/{token}", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listGamesMatchingToken(@PathVariable("token") @NotBlank @Size(min = 3) String token) {
        return new ResponseEntity<>(gameService.listGamesMatchingToken(token), HttpStatus.OK);
    }

    @GetMapping(value = "/games/date/{date}", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listGamesWithScheduleDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return new ResponseEntity<>(gameService.listGamesWithScheduleDate(date), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listGamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listGamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/live", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listLiveGamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listLiveGamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/next-10", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listNext10GamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listNext10GamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/last-10", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listLast10GamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listLast10GamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/team/{teamId}", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listGamesOfTeamInLeague(@PathVariable("leagueId") UUID leagueId, @PathVariable("teamId") UUID teamId) {
        return new ResponseEntity<>(gameService.listGamesOfTeamInLeague(leagueId, teamId), HttpStatus.OK);
    }

    @GetMapping(value = "/teams/league/{leagueId}", produces = {"application/json"})
    public ResponseEntity<List<TeamDescription>> listTeamsOfLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(teamService.listTeamsOfLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/leagues/{leagueId}", produces = {"application/json"})
    public ResponseEntity<League> getLeague(@PathVariable("leagueId") UUID leagueId) {
        try {
            return new ResponseEntity<>(leagueService.getLeague(leagueId), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/messages", produces = {"application/json"})
    public ResponseEntity<Message> getMessage() {
        try {
            return new ResponseEntity<>(messageService.getMessage(), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}