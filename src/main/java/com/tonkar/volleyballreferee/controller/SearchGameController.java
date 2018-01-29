package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.GameDescription;
import com.tonkar.volleyballreferee.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@RequestMapping("/api/search/game")
@CrossOrigin("*")
public class SearchGameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchGameController.class);

    @Autowired
    private GameService gameService;

    @GetMapping("/{token}")
    public ResponseEntity<List<GameDescription>> searchGames(@PathVariable("token") String token) {
        LOGGER.debug(String.format("Request list games with token %s", token));

        if (token.length() > 2) {
            List<GameDescription> gameDescriptions = gameService.listGameDescriptions(token);

            if (gameDescriptions.isEmpty()) {
                LOGGER.debug(String.format("No game with token %s", token));
                return new ResponseEntity<>(gameDescriptions, HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(gameDescriptions, HttpStatus.OK);
            }
        } else {
            LOGGER.debug(String.format("Request list games rejected because token %s is too short", token));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<GameDescription>> searchGamesByDate(@PathVariable("date") String date) {
        LOGGER.debug(String.format("Request list games with date %s", date));

        long fromDate = parseDate(date);

        if (fromDate > 0L) {
            long toDate = fromDate + 86400000L;
            List<GameDescription> gameDescriptions = gameService.listGameDescriptionsBetween(fromDate, toDate);

            if (gameDescriptions.isEmpty()) {
                LOGGER.debug(String.format("No game on date %s", date));
                return new ResponseEntity<>(gameDescriptions, HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(gameDescriptions, HttpStatus.OK);
            }
        } else {
            LOGGER.debug(String.format("Request list games rejected because date %s has wrong format", date));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/live")
    public ResponseEntity<List<GameDescription>> searchLiveGames() {
        LOGGER.debug("Request list live games");

        List<GameDescription> gameDescriptions = gameService.listLiveGameDescriptions();

        if (gameDescriptions.isEmpty()) {
            LOGGER.debug("No live game");
            return new ResponseEntity<>(gameDescriptions, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(gameDescriptions, HttpStatus.OK);
        }
    }

    private long parseDate(String date) {
        long dateMillis = 0L;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        try {
            dateMillis = formatter.parse(date).getTime();
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return dateMillis;
    }

}