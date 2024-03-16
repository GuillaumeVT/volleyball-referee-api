package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class PublicController {

    private final StatisticsService statisticsService;
    private final GameService       gameService;
    private final TeamService       teamService;
    private final LeagueService     leagueService;
    private final UserService       userService;

    @GetMapping(value = "/public/users/{purchaseToken}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserSummary> getUserFromPurchaseToken(@PathVariable("purchaseToken") @NotBlank String purchaseToken) {
        return new ResponseEntity<>(userService.getUserFromPurchaseToken(purchaseToken), HttpStatus.OK);
    }

    @PostMapping(value = "/public/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserToken> createUser(@Valid @NotNull @RequestBody NewUser newUser) {
        return new ResponseEntity<>(userService.createUser(newUser), HttpStatus.CREATED);
    }

    @PostMapping(value = "/public/users/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserToken> signInUser(@Valid @NotNull @RequestBody EmailCredentials emailCredentials) {
        return new ResponseEntity<>(userService.signInUser(emailCredentials.userEmail(), emailCredentials.userPassword()), HttpStatus.OK);
    }

    @PostMapping(value = "/public/users/password/recover/{userEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> initiatePasswordReset(@PathVariable("userEmail") @Email @NotBlank String userEmail) {
        userService.initiatePasswordReset(userEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/public/users/password/follow/{passwordResetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> followPasswordReset(@PathVariable("passwordResetId") UUID passwordResetId) {
        String redirectUrl = userService.followPasswordReset(passwordResetId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(redirectUrl).queryParam("passwordResetId", passwordResetId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", builder.toUriString());
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping(value = "/public/users/password/reset/{passwordResetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserToken> resetPassword(@PathVariable("passwordResetId") UUID passwordResetId,
                                                   @Valid @NotNull @RequestBody UserPassword userPassword) {
        return new ResponseEntity<>(userService.resetPassword(passwordResetId, userPassword.userPassword()), HttpStatus.OK);
    }

    @GetMapping(value = "/public/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatisticsGroup> getGlobalStatistics() {
        return new ResponseEntity<>(statisticsService.getGlobalStatistics(), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/{gameId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Game> getGame(@PathVariable("gameId") UUID gameId) {
        return new ResponseEntity<>(gameService.getGame(gameId), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/{gameId}/score-sheet")
    public ResponseEntity<?> getScoreSheet(@PathVariable("gameId") UUID gameId) {
        FileWrapper scoreSheet = gameService.getScoreSheet(gameId);
        ByteArrayResource resource = new ByteArrayResource(scoreSheet.data());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + scoreSheet.filename())
                .contentType(MediaType.TEXT_HTML)
                .contentLength(scoreSheet.data().length)
                .body(resource);
    }

    @GetMapping(value = "/public/games/live", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listLiveGames(@RequestParam(value = "kind", required = false) List<GameType> kinds,
                                                           @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                           @RequestParam("page") @Min(0) int page,
                                                           @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listLiveGames(kinds, genders, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/token/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listGamesMatchingToken(@PathVariable("token") @NotBlank @Size(min = 3) String token,
                                                                    @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                                    @RequestParam(value = "kind", required = false) List<GameType> kinds,
                                                                    @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                                    @RequestParam("page") @Min(0) int page,
                                                                    @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGamesMatchingToken(token, statuses, kinds, genders, PageRequest.of(page, size)),
                                    HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/date/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listGamesWithScheduleDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                       @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                                       @RequestParam(value = "kind", required = false) List<GameType> kinds,
                                                                       @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                                       @RequestParam("page") @Min(0) int page,
                                                                       @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGamesWithScheduleDate(date, statuses, kinds, genders, PageRequest.of(page, size)),
                                    HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listGamesInLeague(@PathVariable("leagueId") UUID leagueId,
                                                               @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                               @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                               @RequestParam("page") @Min(0) int page,
                                                               @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGamesInLeague(leagueId, statuses, genders, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/group", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeagueDashboard> getGamesInLeagueGroupedByStatus(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.getGamesInLeagueGroupedByStatus(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/live", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GameSummary>> listLiveGamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listLiveGamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/next-10", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GameSummary>> listNext10GamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listNext10GamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/last-10", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GameSummary>> listLast10GamesInLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listLast10GamesInLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listGamesOfTeamInLeague(@PathVariable("leagueId") UUID leagueId,
                                                                     @PathVariable("teamId") UUID teamId,
                                                                     @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                                     @RequestParam("page") @Min(0) int page,
                                                                     @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGamesOfTeamInLeague(leagueId, teamId, statuses, PageRequest.of(page, size)),
                                    HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/division/{divisionName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listGamesInDivision(@PathVariable("leagueId") UUID leagueId,
                                                                 @PathVariable("divisionName") String divisionName,
                                                                 @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                                 @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                                 @RequestParam("page") @Min(0) int page,
                                                                 @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGamesInDivision(leagueId, divisionName, statuses, genders, PageRequest.of(page, size)),
                                    HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/division/{divisionName}/group", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeagueDashboard> getGamesInDivisionGroupedByStatus(@PathVariable("leagueId") UUID leagueId,
                                                                             @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.getGamesInDivisionGroupedByStatus(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/division/{divisionName}/live", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GameSummary>> listLiveGamesInDivision(@PathVariable("leagueId") UUID leagueId,
                                                                     @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listLiveGamesInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/division/{divisionName}/next-10", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GameSummary>> listNext10GamesInDivision(@PathVariable("leagueId") UUID leagueId,
                                                                       @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listNext10GamesInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/division/{divisionName}/last-10", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GameSummary>> listLast10GamesInDivision(@PathVariable("leagueId") UUID leagueId,
                                                                       @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listLast10GamesInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/division/{divisionName}/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listGamesOfTeamInDivision(@PathVariable("leagueId") UUID leagueId,
                                                                       @PathVariable("divisionName") String divisionName,
                                                                       @PathVariable("teamId") UUID teamId,
                                                                       @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                                       @RequestParam("page") @Min(0) int page,
                                                                       @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(
                gameService.listGamesOfTeamInDivision(leagueId, divisionName, teamId, statuses, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/public/games/league/{leagueId}/division/{divisionName}/rankings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Ranking>> listRankingsInDivision(@PathVariable("leagueId") UUID leagueId,
                                                                @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(gameService.listRankingsInDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping("/public/games/league/{leagueId}/division/{divisionName}/excel")
    public ResponseEntity<?> listGamesInDivisionExcel(@PathVariable("leagueId") UUID leagueId,
                                                      @PathVariable("divisionName") String divisionName) throws IOException {
        FileWrapper excelDivision = gameService.listGamesInDivisionExcel(leagueId, divisionName);
        ByteArrayResource resource = new ByteArrayResource(excelDivision.data());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + excelDivision.filename())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelDivision.data().length)
                .body(resource);
    }

    @GetMapping(value = "/public/teams/league/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TeamSummary>> listTeamsOfLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(teamService.listTeamsOfLeague(leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/public/teams/league/{leagueId}/division/{divisionName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TeamSummary>> listTeamsOfDivision(@PathVariable("leagueId") UUID leagueId,
                                                                 @PathVariable("divisionName") String divisionName) {
        return new ResponseEntity<>(teamService.listTeamsOfDivision(leagueId, divisionName), HttpStatus.OK);
    }

    @GetMapping(value = "/public/leagues/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<League> getLeague(@PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(leagueService.getLeague(leagueId), HttpStatus.OK);
    }

}
