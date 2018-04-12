package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.Game;
import com.tonkar.volleyballreferee.model.Set;
import com.tonkar.volleyballreferee.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/manage/game")
@CrossOrigin("*")
public class ManageGameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageGameController.class);

    @Autowired
    private GameService gameService;

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateGame(@PathVariable("id") long id, @Valid @RequestBody Game game) {
        LOGGER.debug(String.format("Request update %s game with date %d (%s vs %s)", game.getKind(), id, game.gethTeam().getName(), game.getgTeam().getName()));

        if (gameService.hasGame(id)) {
            gameService.updateGame(id, game);
            return new ResponseEntity<>(id, HttpStatus.OK);
        } else {
            gameService.createGame(game);
            return new ResponseEntity<>(id, HttpStatus.CREATED);
        }
    }

    @PutMapping("/{id}/set/{index}")
    public ResponseEntity<Long> updateSet(@PathVariable("id") long id, @PathVariable("index") int index, @Valid @RequestBody Set set) {
        LOGGER.debug(String.format("Request update set index %d for game with date %d", index, id));

        if (gameService.hasGame(id)) {
            gameService.updateSet(id, index, set);
            return new ResponseEntity<>(id, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("No game with date %d when updating set %d", id, index));
            return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Boolean> gameIsSynched(@PathVariable("id") long id) {
        LOGGER.debug(String.format("Request game exists with date %d", id));
        return new ResponseEntity<>(gameService.hasGameSynched(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLiveGame(@PathVariable("id") long id) {
        LOGGER.debug(String.format("Request delete live game with date %d", id));

        if (gameService.hasGame(id)) {
            gameService.deleteLiveGame(id);
            return new ResponseEntity<>(id, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("No game with date %d when deleting", id));
            return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
        }
    }

}