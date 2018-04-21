package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.Team;
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
@RequestMapping("/api/user/team")
@CrossOrigin("*")
public class UserTeamController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTeamController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<List<Team>> getTeams(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        UserId userId = new UserId(socialId, provider);
        List<Team> teams = userService.getUserTeams(userId);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "name" }, method = RequestMethod.GET)
    public ResponseEntity<Team> getTeam(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("name") String name) {
        name = ControllerUtils.decodeUrlParameters(name);
        UserId userId = new UserId(socialId, provider);
        Team team = userService.getUserTeam(userId, name);

        if (team == null) {
            LOGGER.error(String.format("No team %s found for user %s", name, userId));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(team, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/count", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<Long> getNumberOfTeams(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        UserId userId = new UserId(socialId, provider);
        long numberOfTeams = userService.getNumberOfUserTeams(userId);
        return new ResponseEntity<>(numberOfTeams, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Team> createTeam(@Valid @RequestBody Team team) {
        boolean result = userService.createUserTeam(team);

        if (result) {
            return new ResponseEntity<>(team, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("Team %s already exists for user %s", team.getName(), team.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @PutMapping("")
    public ResponseEntity<Team> updateTeam(@Valid @RequestBody Team team) {
        boolean result = userService.updateUserTeam(team);

        if (result) {
            return new ResponseEntity<>(team, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to update team %s for user %s", team.getName(), team.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "name" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteTeam(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("name") String name) {
        name = ControllerUtils.decodeUrlParameters(name);
        UserId userId = new UserId(socialId, provider);
        boolean result = userService.deleteUserTeam(userId, name);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete team %s for user %s", name, userId));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

}
