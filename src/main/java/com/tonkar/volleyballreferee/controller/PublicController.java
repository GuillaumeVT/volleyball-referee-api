package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.exception.*;
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
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3.1/public")
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

    @Autowired
    private UserService userService;

    @GetMapping(value = "/users/{purchaseToken}", produces = {"application/json"})
    public ResponseEntity<UserSummary> getUserFromPurchaseToken(@PathVariable("purchaseToken") @NotBlank String purchaseToken) {
        try {
            return new ResponseEntity<>(userService.getUserFromPurchaseToken(purchaseToken), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/users", produces = {"application/json"})
    public ResponseEntity<UserToken> createUser(@Valid @RequestBody User user) {
        try {
            return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
        } catch (UnauthorizedException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (ForbiddenException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/users/token", produces = {"application/json"})
    public ResponseEntity<UserToken> signInUser(@Valid @RequestBody EmailCredentials emailCredentials) {
        try {
            return new ResponseEntity<>(userService.signInUser(emailCredentials.getUserEmail(), emailCredentials.getUserPassword()), HttpStatus.OK);
        } catch (UnauthorizedException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (ForbiddenException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/users/password/recover/{userEmail}", produces = {"application/json"})
    public ResponseEntity<String> initiatePasswordReset(@PathVariable("userEmail") @Email @NotBlank String userEmail) {
        try {
            userService.initiatePasswordReset(userEmail);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/users/password/follow/{passwordResetId}", produces = {"application/json"})
    public ResponseEntity<String> followPasswordReset(@PathVariable("passwordResetId") @NotNull UUID passwordResetId) {
        try {
            String redirectUrl = userService.followPasswordReset(passwordResetId);

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(redirectUrl)
                    .queryParam("passwordResetId", passwordResetId);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", builder.toUriString());
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/users/password/reset/{passwordResetId}", produces = {"application/json"})
    public ResponseEntity<UserToken> resetPassword(@PathVariable("passwordResetId") @NotNull UUID passwordResetId, @Valid @RequestBody UserPassword userPassword) {
        try {
            return new ResponseEntity<>(userService.resetPassword(passwordResetId, userPassword.getUserPassword()), HttpStatus.OK);
        } catch (BadRequestException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (ForbiddenException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

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

    @GetMapping(value = "/games/{gameId}/score-sheet")
    public ResponseEntity<?> getScoreSheet(@PathVariable("gameId") UUID gameId) {
        try {
            FileWrapper scoreSheet = gameService.getScoreSheet(gameId);
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
    public ResponseEntity<List<GameSummary>> listLiveGames() {
        return new ResponseEntity<>(gameService.listLiveGames(), HttpStatus.OK);
    }

    @GetMapping(value = "/games/token/{token}", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGamesMatchingToken(@PathVariable("token") @NotBlank @Size(min = 3) String token) {
        return new ResponseEntity<>(gameService.listGamesMatchingToken(token), HttpStatus.OK);
    }

    @GetMapping(value = "/games/date/{date}", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGamesWithScheduleDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return new ResponseEntity<>(gameService.listGamesWithScheduleDate(date), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listGamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/live", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listLiveGamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listLiveGamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/next-10", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listNext10GamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listNext10GamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/last-10", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listLast10GamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listLast10GamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/team/{teamId}", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGamesOfTeamInLeague(@PathVariable("leagueId") UUID leagueId, @PathVariable("teamId") UUID teamId) {
        return new ResponseEntity<>(gameService.listGamesOfTeamInLeague(leagueId, teamId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/division/{divisionName}", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGamesInDivision(@PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listGamesInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/division/{divisionName}/live", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listLiveGamesInDivision(@PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listLiveGamesInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/division/{divisionName}/next-10", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listNext10GamesInDivision(@PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listNext10GamesInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/division/{divisionName}/last-10", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listLast10GamesInDivision(@PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listLast10GamesInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/division/{divisionName}/team/{teamId}", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGamesOfTeamInDivision(@PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName, @PathVariable("teamId") UUID teamId) {
        return new ResponseEntity<>(gameService.listGamesOfTeamInDivision(leagueId, divisionName, teamId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/division/{divisionName}/rankings", produces = {"application/json"})
    public ResponseEntity<List<Ranking>> listRankingsInDivision(@PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listRankingsInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping("/games/league/{leagueId}/division/{divisionName}/excel")
    public ResponseEntity<?> listGamesInDivisionExcel(@PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName) {
        try {
            FileWrapper excelDivision = gameService.listGamesInDivisionExcel(leagueId, divisionName);
            ByteArrayResource resource = new ByteArrayResource(excelDivision.getData());
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + excelDivision.getFilename())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelDivision.getData().length)
                    .body(resource);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/teams/league/{leagueId}", produces = {"application/json"})
    public ResponseEntity<List<TeamSummary>> listTeamsOfLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(teamService.listTeamsOfLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/teams/league/{leagueId}/division/{divisionName}", produces = {"application/json"})
    public ResponseEntity<List<TeamSummary>> listTeamsOfDivision(@PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(teamService.listTeamsOfDivision(leagueId, divisionName), HttpStatus.OK);
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
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
