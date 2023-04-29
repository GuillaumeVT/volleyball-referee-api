package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameIngredients;
import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping(value = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listGames(@AuthenticationPrincipal User user,
                                                       @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                       @RequestParam(value = "kind", required = false) List<GameType> kinds,
                                                       @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                       @RequestParam("page") @Min(0) int page,
                                                       @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGames(user, statuses, kinds, genders, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/games/available", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GameSummary>> listAvailableGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.listAvailableGames(user), HttpStatus.OK);
    }

    @GetMapping(value = "/games/completed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listCompletedGames(@AuthenticationPrincipal User user,
                                                                @RequestParam("page") @Min(0) int page,
                                                                @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listCompletedGames(user, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GameSummary>> listGamesInLeague(@AuthenticationPrincipal User user,
                                                               @PathVariable("leagueId") UUID leagueId,
                                                               @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                               @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                               @RequestParam("page") @Min(0) int page,
                                                               @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGamesInLeague(user, leagueId, statuses, genders, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/games/{gameId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Game> getGame(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId) {
        return new ResponseEntity<>(gameService.getGame(user, gameId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/ingredients/{kind}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GameIngredients> getGameIngredientsOfKind(@AuthenticationPrincipal User user, @PathVariable("kind") GameType kind) {
        return new ResponseEntity<>(gameService.getGameIngredientsOfKind(user, kind), HttpStatus.OK);
    }

    @GetMapping(value = "/games/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Count> getNumberOfGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.getNumberOfGames(user), HttpStatus.OK);
    }

    @GetMapping(value = "/games/league/{leagueId}/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Count> getNumberOfGamesInLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.getNumberOfGamesInLeague(user, leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/games/available/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Count> getNumberOfAvailableGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.getNumberOfAvailableGames(user), HttpStatus.OK);
    }

    @PostMapping(value = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createGame(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody GameSummary gameSummary) {
        gameService.createGame(user, gameSummary);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/games/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createGame(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Game game) {
        gameService.createGame(user, game);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateGame(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody GameSummary gameSummary) {
        gameService.updateGame(user, gameSummary);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = "/games/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateGame(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Game game) {
        gameService.updateGame(user, game);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(value = "/games/{gameId}/set/{setIndex}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateSet(@AuthenticationPrincipal User user,
                                          @PathVariable("gameId") UUID gameId,
                                          @PathVariable("setIndex") @Positive int setIndex,
                                          @Valid @NotNull @RequestBody Set set) {
        gameService.updateSet(user, gameId, setIndex, set);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(value = "/games/{gameId}/indexed/{indexed}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> setIndexed(@AuthenticationPrincipal User user,
                                           @PathVariable("gameId") UUID gameId,
                                           @PathVariable("indexed") boolean indexed)  {
        gameService.setIndexed(user, gameId, indexed);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(value = "/games/{gameId}/referee/{refereeUserId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> setReferee(@AuthenticationPrincipal User user,
                                           @PathVariable("gameId") UUID gameId,
                                           @PathVariable("refereeUserId") @NotBlank String refereeUserId) {
        gameService.setReferee(user, gameId, refereeUserId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/games/{gameId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteGame(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId) {
        gameService.deleteGame(user, gameId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAllGames(@AuthenticationPrincipal User user) {
        gameService.deleteAllGames(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/games/league/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAllGamesInLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        gameService.deleteAllGamesInLeague(user, leagueId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
