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
    public ResponseEntity<List<League>> listUserLeagues(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        UserId userId = new UserId(socialId, provider);
        List<League> leagues = userService.listUserLeagues(userId);
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "kind" }, method = RequestMethod.GET)
    public ResponseEntity<List<League>> listUserLeaguesOfKind(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("kind") String kind) {
        UserId userId = new UserId(socialId, provider);
        List<League> leagues = userService.listUserLeaguesOfKind(userId, kind);
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "date" }, method = RequestMethod.GET)
    public ResponseEntity<League> getUserLeague(@RequestParam("date") long date) {
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

    @RequestMapping(value = "", params = { "socialId", "provider", "date" }, method = RequestMethod.GET)
    public ResponseEntity<League> getUserLeague(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("date") long date) {
        UserId userId = new UserId(socialId, provider);
        League league = userService.getUserLeague(userId, date);

        if (league == null) {
            LOGGER.error(String.format("No league with date %d found for user %s", date, userId));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(league, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/count", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<Long> getNumberOfUserLeagues(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        UserId userId = new UserId(socialId, provider);
        long numberOfLeagues = userService.getNumberOfUserLeagues(userId);
        return new ResponseEntity<>(numberOfLeagues, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<League> createUserLeague(@Valid @RequestBody League league) {
        boolean result = userService.createUserLeague(league);

        if (result) {
            return new ResponseEntity<>(league, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("League %s already exists for user %s", league.getName(), league.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "date" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserLeague(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("date") long date) {
        UserId userId = new UserId(socialId, provider);
        boolean result = userService.deleteUserLeague(userId, date);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete league with date %d for user %s", date, userId));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

}