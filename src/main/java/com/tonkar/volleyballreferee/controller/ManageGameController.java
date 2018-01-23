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

    private static final Logger logger = LoggerFactory.getLogger(ManageGameController.class);

    @Autowired
    private GameService gameService;

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGame(@PathVariable("id") long id, @Valid @RequestBody Game game) {
        logger.info(String.format("Request update %s game with date %d (%s vs %s)", game.getKind(), id, game.gethTeam().getName(), game.getgTeam().getName()));

        if (gameService.hasGame(id)) {
            gameService.updateGame(id, game);
            return new ResponseEntity<>(id, HttpStatus.OK);
        } else {
            gameService.createGame(game);
            return new ResponseEntity<>(id, HttpStatus.CREATED);
        }
    }

    @PutMapping("/{id}/set/{index}")
    public ResponseEntity<?> updateSet(@PathVariable("id") long id, @PathVariable("index") int index, @Valid @RequestBody Set set) {
        logger.info(String.format("Request update set index %d for game with date %d", index, id));

        if (gameService.hasGame(id)) {
            gameService.updateSet(id, index, set);
            return new ResponseEntity<>(id, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> gameExists(@PathVariable("id") long id) {
        logger.info(String.format("Request game exists with date %d", id));
        return new ResponseEntity<>(gameService.hasGame(id), HttpStatus.OK);
    }

    @DeleteMapping("live/{id}")
    public ResponseEntity<?> deleteLiveGame(@PathVariable("id") long id) {
        logger.info(String.format("Request delete live game with date %d", id));

        if (gameService.hasGame(id)) {
            gameService.deleteLiveGame(id);
            return new ResponseEntity<>(id, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
        }
    }

}