package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.League;
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
@RequestMapping("/api/user/league")
@CrossOrigin("*")
public class UserLeagueController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLeagueController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<List<League>> getLeagues(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        UserId userId = new UserId(socialId, provider);
        List<League> leagues = userService.getUserLeagues(userId);
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "kind" }, method = RequestMethod.GET)
    public ResponseEntity<List<League>> getLeagues(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("kind") String kind) {
        UserId userId = new UserId(socialId, provider);
        List<League> leagues = userService.getUserLeagues(userId, kind);
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "name" }, method = RequestMethod.GET)
    public ResponseEntity<League> getLeague(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("name") String name) {
        name = ControllerUtils.decodeUrlParameters(name);
        UserId userId = new UserId(socialId, provider);
        League league = userService.getUserLeague(userId, name);

        if (league == null) {
            LOGGER.error(String.format("No league %s found for user %s", name, userId));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(league, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/count", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<Long> getNumberOfLeagues(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        UserId userId = new UserId(socialId, provider);
        long numberOfLeagues = userService.getNumberOfUserLeagues(userId);
        return new ResponseEntity<>(numberOfLeagues, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<League> createLeague(@Valid @RequestBody League league) {
        boolean result = userService.createUserLeague(league);

        if (result) {
            return new ResponseEntity<>(league, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("League %s already exists for user %s", league.getName(), league.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @PutMapping("")
    public ResponseEntity<League> updateLeague(@Valid @RequestBody League league) {
        boolean result = userService.updateUserLeague(league);

        if (result) {
            return new ResponseEntity<>(league, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to update league %s for user %s", league.getName(), league.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "name" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteLeague(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("name") String name) {
        name = ControllerUtils.decodeUrlParameters(name);
        UserId userId = new UserId(socialId, provider);
        boolean result = userService.deleteUserLeague(userId, name);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete league %s for user %s", name, userId));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

}
