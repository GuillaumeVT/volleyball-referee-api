package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameIngredients;
import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.service.GameService;
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
import javax.validation.constraints.*;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v3.2/games")
@CrossOrigin("*")
@Slf4j
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<Page<GameSummary>> listGames(@AuthenticationPrincipal User user,
                                                       @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                       @RequestParam(value = "kind", required = false) List<GameType> kinds,
                                                       @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                       @RequestParam("page") @Min(0) int page,
                                                       @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGames(user, statuses, kinds, genders, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/available", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listAvailableGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.listAvailableGames(user), HttpStatus.OK);
    }

    @GetMapping(value = "/completed", produces = {"application/json"})
    public ResponseEntity<Page<GameSummary>> listCompletedGames(@AuthenticationPrincipal User user,
                                                                @RequestParam("page") @Min(0) int page,
                                                                @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listCompletedGames(user, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/league/{leagueId}", produces = {"application/json"})
    public ResponseEntity<Page<GameSummary>> listGamesInLeague(@AuthenticationPrincipal User user,
                                                               @PathVariable("leagueId") UUID leagueId,
                                                               @RequestParam(value = "status", required = false) List<GameStatus> statuses,
                                                               @RequestParam(value = "gender", required = false) List<GenderType> genders,
                                                               @RequestParam("page") @Min(0) int page,
                                                               @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(gameService.listGamesInLeague(user, leagueId, statuses, genders, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @GetMapping(value = "/{gameId}", produces = {"application/json"})
    public ResponseEntity<Game> getGame(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId) {
        try {
            return new ResponseEntity<>(gameService.getGame(user, gameId), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/ingredients/{kind}", produces = {"application/json"})
    public ResponseEntity<GameIngredients> getGameIngredientsOfKind(@AuthenticationPrincipal User user, @PathVariable("kind") GameType kind) {
        return new ResponseEntity<>(gameService.getGameIngredientsOfKind(user, kind), HttpStatus.OK);
    }

    @GetMapping(value = "/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.getNumberOfGames(user), HttpStatus.OK);
    }

    @GetMapping(value = "/league/{leagueId}/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfGamesInLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.getNumberOfGamesInLeague(user, leagueId), HttpStatus.OK);
    }

    @GetMapping(value = "/available/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfAvailableGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.getNumberOfAvailableGames(user), HttpStatus.OK);
    }

    @PostMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> createGame(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody GameSummary gameSummary) {
        try {
            gameService.createGame(user, gameSummary);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/full", produces = {"application/json"})
    public ResponseEntity<String> createGame(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Game game) {
        try {
            gameService.createGame(user, game);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> updateGame(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody GameSummary gameSummary) {
        try {
            gameService.updateGame(user, gameSummary);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/full", produces = {"application/json"})
    public ResponseEntity<String> updateGame(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Game game) {
        try {
            gameService.updateGame(user, game);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value = "/{gameId}/set/{setIndex}", produces = {"application/json"})
    public ResponseEntity<String> updateSet(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId, @PathVariable("setIndex") @Positive int setIndex, @Valid @NotNull @RequestBody Set set) {
        try {
            gameService.updateSet(user, gameId, setIndex, set);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value = "/{gameId}/indexed/{indexed}", produces = {"application/json"})
    public ResponseEntity<String> setIndexed(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId, @PathVariable("indexed") boolean indexed) {
        try {
            gameService.setIndexed(user, gameId, indexed);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value = "/{gameId}/referee/{refereeUserId}", produces = {"application/json"})
    public ResponseEntity<String> setReferee(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId, @PathVariable("refereeUserId") @NotBlank String refereeUserId) {
        try {
            gameService.setReferee(user, gameId, refereeUserId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/{gameId}", produces = {"application/json"})
    public ResponseEntity<String> deleteGame(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId) {
        gameService.deleteGame(user, gameId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> deleteAllGames(@AuthenticationPrincipal User user) {
        gameService.deleteAllGames(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
