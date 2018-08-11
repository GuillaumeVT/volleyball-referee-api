package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.League;
import com.tonkar.volleyballreferee.security.User;
import com.tonkar.volleyballreferee.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("")
    public ResponseEntity<List<League>> listUserLeagues(@AuthenticationPrincipal User user) {
        List<League> leagues = userService.listUserLeagues(user.getUserId());
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "kind" }, method = RequestMethod.GET)
    public ResponseEntity<List<League>> listUserLeaguesOfKind(@AuthenticationPrincipal User user, @RequestParam("kind") String kind) {
        List<League> leagues = userService.listUserLeaguesOfKind(user.getUserId(), kind);
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @GetMapping("/division")
    public ResponseEntity<List<String>> listUserDivisions(@AuthenticationPrincipal User user, @RequestParam("kind") String kind) {
        List<String> divisions = userService.listUserDivisionsOfKind(user.getUserId(), kind);
        return new ResponseEntity<>(divisions, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "date" }, method = RequestMethod.GET)
    public ResponseEntity<League> getUserLeague(@AuthenticationPrincipal User user, @RequestParam("date") long date) {
        League league = userService.getUserLeague(user.getUserId(), date);

        if (league == null) {
            LOGGER.error(String.format("No league with date %d found for user %s", date, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(league, HttpStatus.OK);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getNumberOfUserLeagues(@AuthenticationPrincipal User user) {
        long numberOfLeagues = userService.getNumberOfUserLeagues(user.getUserId());
        return new ResponseEntity<>(numberOfLeagues, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<League> createUserLeague(@AuthenticationPrincipal User user, @Valid @RequestBody League league) {
        league.setUserId(user.getUserId());
        boolean result = userService.createUserLeague(league);

        if (result) {
            return new ResponseEntity<>(league, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("League %s already exists for user %s", league.getName(), league.getUserId()));
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteUserLeague(@AuthenticationPrincipal User user, @RequestParam("date") long date) {
        boolean result = userService.deleteUserLeague(user.getUserId(), date);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete league with date %d for user %s", date, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/csv")
    public ResponseEntity<?> getCsvLeague(@AuthenticationPrincipal User user, @RequestParam("league") String league, @RequestParam("division") String division) {
        LOGGER.debug(String.format("Request download csv with league %s and division %s for user %s", league, division, user.getUserId()));

        byte[] data = userService.getCsvLeague(user.getUserId(), league, division);

        String fileName = league + "_" + division + ".csv";
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }
}
