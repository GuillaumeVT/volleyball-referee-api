package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameDescription;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/games")
@CrossOrigin("*")
@Slf4j
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.listGames(user.getId()), HttpStatus.OK);
    }

    @GetMapping(value = "/status/{status}", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listGamesWithStatus(@AuthenticationPrincipal User user, @PathVariable("status") GameStatus status) {
        return new ResponseEntity<>(gameService.listGamesWithStatus(user.getId(), status), HttpStatus.OK);
    }

    @GetMapping(value = "/available", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listAvailableGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.listAvailableGames(user.getId()), HttpStatus.OK);
    }

    @GetMapping(value = "/completed", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listCompletedGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.listCompletedGames(user.getId()), HttpStatus.OK);
    }

    @GetMapping(value = "/league/{leagueId}", produces = {"application/json"})
    public ResponseEntity<List<GameDescription>> listGamesInLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.listGamesInLeague(user.getId(), leagueId), HttpStatus.OK);
    }

    @GetMapping("/league/{leagueId}/division/{divisionName}/excel")
    public ResponseEntity<?> listGamesInDivisionExcel(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId, @PathVariable("divisionName") String divisionName) {
        try {
            FileWrapper excelDivision = gameService.listGamesInDivisionExcel(user.getId(), leagueId, divisionName);
            ByteArrayResource resource = new ByteArrayResource(excelDivision.getData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + excelDivision.getFilename())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelDivision.getData().length)
                    .body(resource);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/{gameId}", produces = {"application/json"})
    public ResponseEntity<Game> getGame(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId) {
        try {
            return new ResponseEntity<>(gameService.getGame(user.getId(), gameId), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfGames(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(gameService.getNumberOfGames(user.getId()), HttpStatus.OK);
    }

    @GetMapping(value = "/league/{leagueId}/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfGamesInLeague(@AuthenticationPrincipal User user, @PathVariable("leagueId") UUID leagueId) {
        return new ResponseEntity<>(gameService.getNumberOfGamesInLeague(user.getId(), leagueId), HttpStatus.OK);
    }

    @PostMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> createGame(@AuthenticationPrincipal User user, @Valid @RequestBody GameDescription gameDescription) {
        try {
            gameService.createGame(user.getId(), gameDescription);
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
            gameService.createGame(user.getId(), game);
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
    public ResponseEntity<String> updateGame(@AuthenticationPrincipal User user, @Valid @RequestBody GameDescription gameDescription) {
        try {
            gameService.updateGame(user.getId(), gameDescription);
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
            gameService.updateGame(user.getId(), game);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value = "/{gameId}/set/{setIndex}", produces = {"application/json"})
    public ResponseEntity<String> updateSet(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId, @PathVariable("setIndex") int setIndex, @Valid @RequestBody Set set) {
        try {
            gameService.updateSet(user.getId(), gameId, setIndex, set);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value = "/{gameId}/indexed/{indexed}", produces = {"application/json"})
    public ResponseEntity<String> setIndexed(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId, @PathVariable("indexed") boolean indexed) {
        try {
            gameService.setIndexed(user.getId(), gameId, indexed);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/{gameId}", produces = {"application/json"})
    public ResponseEntity<String> deleteGame(@AuthenticationPrincipal User user, @PathVariable("gameId") UUID gameId) {
        gameService.deleteGame(user.getId(), gameId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> deleteAllGames(@AuthenticationPrincipal User user) {
        gameService.deleteAllGames(user.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
