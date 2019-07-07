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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3.1/games")
@CrossOrigin("*")
@Slf4j
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.listGames(user), HttpStatus.OK);
    }

    @GetMapping(value = "/status/{status}", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGamesWithStatus(@AuthenticationPrincipal User user, @PathVariable("status") GameStatus status) {
        return new ResponseEntity<>(gameService.listGamesWithStatus(user, status), HttpStatus.OK);
    }

    @GetMapping(value = "/available", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listAvailableGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.listAvailableGames(user), HttpStatus.OK);
    }

    @GetMapping(value = "/completed", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listCompletedGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.listCompletedGames(user), HttpStatus.OK);
    }

    @GetMapping(value = "/league/{leagueId}", produces = {"application/json"})
    public ResponseEntity<List<GameSummary>> listGamesInLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listGamesInLeague(user, leagueId), HttpStatus.OK);
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
    public ResponseEntity<String> createGame(@AuthenticationPrincipal User user, @Valid @RequestBody GameSummary gameSummary) {
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
    public ResponseEntity<String> createGame(@AuthenticationPrincipal User user, @Valid @RequestBody Game game) {
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
    public ResponseEntity<String> updateGame(@AuthenticationPrincipal User user, @Valid @RequestBody GameSummary gameSummary) {
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
    public ResponseEntity<String> updateGame(@AuthenticationPrincipal User user, @Valid @RequestBody Game game) {
        try {
            gameService.updateGame(user, game);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value = "/{gameId}/set/{setIndex}", produces = {"application/json"})
    public ResponseEntity<String> updateSet(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId, @PathVariable("setIndex") int setIndex, @Valid @RequestBody Set set) {
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
    public ResponseEntity<String> setReferee(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId, @PathVariable("refereeUserId") String refereeUserId) {
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
