package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.GameDescription;
import com.tonkar.volleyballreferee.model.UserId;
import com.tonkar.volleyballreferee.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(value = "", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<List<GameDescription>> listUserGames(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        UserId userId = new UserId(socialId, provider);
        List<GameDescription> games = userService.listUserGames(userId);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "kind", "league" }, method = RequestMethod.GET)
    public ResponseEntity<List<GameDescription>> listUserGamesInLeague(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("kind") String kind, @RequestParam("league") String league) {
        league = ControllerUtils.decodeUrlParameter(league);
        UserId userId = new UserId(socialId, provider);
        List<GameDescription> games = userService.listUserGamesInLeague(userId, kind, league);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "id" }, method = RequestMethod.GET)
    public ResponseEntity<GameDescription> getUserGame(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("id") long id) {
        UserId userId = new UserId(socialId, provider);
        GameDescription game = userService.getUserGame(userId, id);

        if (game == null) {
            LOGGER.error(String.format("No game %d found for user %s", id, userId));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(game, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/code", params = { "socialId", "provider", "id" }, method = RequestMethod.GET)
    public ResponseEntity<Integer> getUserGameCode(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("id") long id) {
        UserId userId = new UserId(socialId, provider);
        int code = userService.getUserGameCode(userId, id);

        if (code == -1) {
            LOGGER.error(String.format("No game %d found for code for user %s", id, userId));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(code, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/count", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<Long> getNumberOfUserGames(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        UserId userId = new UserId(socialId, provider);
        long numberOfUserGames = userService.getNumberOfUserGames(userId);
        return new ResponseEntity<>(numberOfUserGames, HttpStatus.OK);
    }

    @RequestMapping(value = "/count", params = { "socialId", "provider", "kind", "league" }, method = RequestMethod.GET)
    public ResponseEntity<Long> getNumberOfUserGames(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("kind") String kind, @RequestParam("league") String league) {
        league = ControllerUtils.decodeUrlParameter(league);
        UserId userId = new UserId(socialId, provider);
        long numberOfUserGames = userService.getNumberOfUserGames(userId, kind, league);
        return new ResponseEntity<>(numberOfUserGames, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<GameDescription> createUserGame(@Valid @RequestBody GameDescription game) {
        boolean result = userService.createUserGame(game);

        if (result) {
            return new ResponseEntity<>(game, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("Game with date %d (%s vs %s) not created for user %s",
                    game.getDate(), game.gethName(), game.getgName(), game.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @PutMapping("")
    public ResponseEntity<GameDescription> updateUserGame(@Valid @RequestBody GameDescription game) {
        boolean result = userService.updateUserGame(game);

        if (result) {
            return new ResponseEntity<>(game, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to update game with date %d (%s vs %s) for user %s",
                    game.getDate(), game.gethName(), game.getgName(), game.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "id" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserGame(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("id") long id) {
        UserId userId = new UserId(socialId, provider);
        boolean result = userService.deleteUserGame(userId, id);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete game %d for user %s", id, userId));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

}
