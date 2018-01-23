package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.GameDescription;
import com.tonkar.volleyballreferee.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search/game")
@CrossOrigin("*")
public class SearchGameController {

    private static final Logger logger = LoggerFactory.getLogger(SearchGameController.class);

    @Autowired
    private GameService gameService;

    @GetMapping("/{token}")
    public ResponseEntity<List<GameDescription>> searchGames(@PathVariable("token") String token) {
        logger.info(String.format("Request list games with token %s", token));

        if (token.length() > 2) {
            List<GameDescription> gameDescriptions = gameService.listGameDescriptions(token);

            if (gameDescriptions.isEmpty()) {
                logger.info(String.format("No game with token %s", token));
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(gameDescriptions, HttpStatus.OK);
            }
        } else {
            logger.info(String.format("Request list games rejected because token %s is too short", token));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/live")
    public ResponseEntity<List<GameDescription>> searchLiveGames() {
        logger.info("Request list live games");

        List<GameDescription> gameDescriptions = gameService.listLiveGameDescriptions();

        if (gameDescriptions.isEmpty()) {
            logger.info("No live game");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(gameDescriptions, HttpStatus.OK);
        }
    }

}