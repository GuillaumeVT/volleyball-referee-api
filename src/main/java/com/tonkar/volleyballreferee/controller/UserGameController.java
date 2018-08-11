package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.Game;
import com.tonkar.volleyballreferee.model.GameDescription;
import com.tonkar.volleyballreferee.model.GameStatus;
import com.tonkar.volleyballreferee.security.User;
import com.tonkar.volleyballreferee.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user/game")
@CrossOrigin("*")
public class UserGameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGameController.class);

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<List<GameDescription>> listUserGames(@AuthenticationPrincipal User user) {
        List<GameDescription> games = userService.listUserGames(user.getUserId());
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<GameDescription>> listCompletedUserGames(@AuthenticationPrincipal User user) {
        List<GameDescription> games = userService.listUserGames(user.getUserId(), GameStatus.COMPLETED);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @GetMapping("/available")
    public ResponseEntity<List<GameDescription>> listAvailableUserGames(@AuthenticationPrincipal User user) {
        List<GameDescription> games = userService.listAvailableUserGames(user.getUserId());
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "kind", "league" }, method = RequestMethod.GET)
    public ResponseEntity<List<GameDescription>> listUserGamesInLeague(@AuthenticationPrincipal User user, @RequestParam("kind") String kind, @RequestParam("league") String league) {
        league = ControllerUtils.decodeUrlParameter(league);
        List<GameDescription> games = userService.listUserGamesInLeague(user.getUserId(), kind, league);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "id" }, method = RequestMethod.GET)
    public ResponseEntity<Game> getUserGame(@AuthenticationPrincipal User user, @RequestParam("id") long id) {
        Game game = userService.getUserGameFull(user.getUserId(), id);

        if (game == null) {
            LOGGER.error(String.format("No game %d found for user %s", id, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(game, HttpStatus.OK);
        }
    }

    @GetMapping("/code")
    public ResponseEntity<Integer> getUserGameCode(@AuthenticationPrincipal User user, @RequestParam("id") long id) {
        int code = userService.getUserGameCode(user.getUserId(), id);

        if (code == -1) {
            LOGGER.error(String.format("No game %d found for code for user %s", id, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(code, HttpStatus.OK);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getNumberOfUserGames(@AuthenticationPrincipal User user) {
        long numberOfUserGames = userService.getNumberOfUserGames(user.getUserId());
        return new ResponseEntity<>(numberOfUserGames, HttpStatus.OK);
    }

    @RequestMapping(value = "/count", params = { "kind", "league" }, method = RequestMethod.GET)
    public ResponseEntity<Long> getNumberOfUserGames(@AuthenticationPrincipal User user, @RequestParam("kind") String kind, @RequestParam("league") String league) {
        league = ControllerUtils.decodeUrlParameter(league);
        long numberOfUserGames = userService.getNumberOfUserGames(user.getUserId(), kind, league);
        return new ResponseEntity<>(numberOfUserGames, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<GameDescription> createUserGame(@AuthenticationPrincipal User user, @Valid @RequestBody GameDescription game) {
        game.setUserId(user.getUserId());
        boolean result = userService.createUserGame(game);

        if (result) {
            return new ResponseEntity<>(game, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("Game with date %d (%s vs %s) not created for user %s",
                    game.getDate(), game.gethName(), game.getgName(), game.getUserId()));
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("")
    public ResponseEntity<GameDescription> updateUserGame(@AuthenticationPrincipal User user, @Valid @RequestBody GameDescription game) {
        game.setUserId(user.getUserId());
        boolean result = userService.updateUserGame(game);

        if (result) {
            return new ResponseEntity<>(game, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to update game with date %d (%s vs %s) for user %s",
                    game.getDate(), game.gethName(), game.getgName(), game.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "", params = { "id" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserGame(@AuthenticationPrincipal User user, @RequestParam("id") long id) {
        boolean result = userService.deleteUserGame(user.getUserId(), id);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete game %d for user %s", id, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteAllUserGames(@AuthenticationPrincipal User user) {
        boolean result = userService.deleteAllUserGames(user.getUserId());

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete all games for user %s", user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
