package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.GameStatistics;
import com.tonkar.volleyballreferee.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin("*")
public class StatisticsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private GameService gameService;

    @GetMapping("/game")
    public ResponseEntity<GameStatistics> getGameStatistics() {
        LOGGER.debug("Request get game statistics");
        return new ResponseEntity<>(gameService.getGameStatistics(), HttpStatus.OK);
    }

}