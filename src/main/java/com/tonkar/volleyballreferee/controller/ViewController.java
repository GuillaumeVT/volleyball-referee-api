package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.League;
import com.tonkar.volleyballreferee.model.Team;
import com.tonkar.volleyballreferee.scoresheet.ScoreSheet;
import com.tonkar.volleyballreferee.scoresheet.ScoreSheetWriter;
import com.tonkar.volleyballreferee.service.GameService;
import com.tonkar.volleyballreferee.model.Game;
import com.tonkar.volleyballreferee.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/view")
@CrossOrigin("*")
public class ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewController.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @GetMapping("/game/{id}")
    public ResponseEntity<Game> getGame(@PathVariable("id") long id) {
        LOGGER.debug(String.format("Request get game with date %d", id));

        Game game = gameService.getGame(id);

        if (game == null) {
            LOGGER.error(String.format("No game with date %d for viewing", id));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            game.setUserId(null);
            game.gethTeam().setUserId(null);
            game.getgTeam().setUserId(null);
            game.getRules().setUserId(null);
            return new ResponseEntity<>(game, HttpStatus.OK);
        }
    }

    @GetMapping("/game/score-sheet/{id}")
    public ResponseEntity<?> getScoreSheet(@PathVariable("id") long id) {
        LOGGER.debug(String.format("Request download score sheet with date %d", id));

        Game game = gameService.getGame(id);

        if (game == null) {
            LOGGER.error(String.format("No game with date %d for downloading score sheet", id));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            ScoreSheet scoreSheet = ScoreSheetWriter.writeGame(game);
            ByteArrayResource resource = new ByteArrayResource(scoreSheet.getData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + scoreSheet.getFilename())
                    .contentType(MediaType.TEXT_HTML)
                    .contentLength(scoreSheet.getData().length)
                    .body(resource);
        }
    }

    @GetMapping("/game/code/{code}")
    public ResponseEntity<Game> getGameFromCode(@PathVariable("code") int code) {
        LOGGER.debug(String.format("Request get game with code %d", code));

        Game game = gameService.getGameFromCode(code);

        if (game == null) {
            LOGGER.error(String.format("No game with code %d", code));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(game, HttpStatus.OK);
        }
    }

    @GetMapping("/league/{date}")
    public ResponseEntity<League> getLeague(@PathVariable("date") long date) {
        League league = userService.getUserLeague(date);

        if (league == null) {
            LOGGER.error(String.format("No league with date %d found", date));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            // This is called in a public context
            league.setUserId(null);
            return new ResponseEntity<>(league, HttpStatus.OK);
        }
    }

    @GetMapping("/team/{date}")
    public ResponseEntity<List<Team>> listTeamsInLeague(@PathVariable("date") long date) {
        List<Team> teams = userService.listUserTeamsInLeague(date);
        // This is called in a public context
        hideUserId(teams);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    private void hideUserId(List<Team> teams) {
        teams.forEach(team -> team.setUserId(null));
    }

}
