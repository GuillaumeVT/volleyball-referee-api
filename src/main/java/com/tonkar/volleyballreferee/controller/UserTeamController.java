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

    @RequestMapping(value = "", params = { "userId" }, method = RequestMethod.GET)
    public ResponseEntity<List<Team>> listUserTeams(@RequestParam("userId") String userId) {
        if (UserId.VBR_USER_ID.equals(userId)) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }

        List<Team> teams = userService.listUserTeams(userId);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "userId", "kind" }, method = RequestMethod.GET)
    public ResponseEntity<List<Team>> listUserTeamsOfKind(@RequestParam("userId") String userId, @RequestParam("kind") String kind) {
        if (UserId.VBR_USER_ID.equals(userId)) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }

        List<Team> teams = userService.listUserTeamsOfKind(userId, kind);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "date" }, method = RequestMethod.GET)
    public ResponseEntity<List<Team>> listUserTeamsInLeague(@RequestParam("date") long date) {
        List<Team> teams = userService.listUserTeamsInLeague(date);
        // This is called in a public context
        hideUserId(teams);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "userId", "name", "gender", "kind" }, method = RequestMethod.GET)
    public ResponseEntity<Team> getUserTeam(@RequestParam("userId") String userId, @RequestParam("name") String name, @RequestParam("gender") String gender, @RequestParam("kind") String kind) {
        if (UserId.VBR_USER_ID.equals(userId)) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }

        name = ControllerUtils.decodeUrlParameter(name);
        Team team = userService.getUserTeam(userId, name, gender, kind);

        if (team == null) {
            LOGGER.error(String.format("No team %s found for user %s", name, userId));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(team, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/count", params = { "userId" }, method = RequestMethod.GET)
    public ResponseEntity<Long> getNumberOfUserTeams(@RequestParam("userId") String userId) {
        if (UserId.VBR_USER_ID.equals(userId)) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }

        long numberOfTeams = userService.getNumberOfUserTeams(userId);
        return new ResponseEntity<>(numberOfTeams, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Team> createUserTeam(@Valid @RequestBody Team team) {
        if (UserId.VBR_USER_ID.equals(team.getUserId())) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }

        boolean result = userService.createUserTeam(team);

        if (result) {
            return new ResponseEntity<>(team, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("Team %s already exists for user %s", team.getName(), team.getUserId()));
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("")
    public ResponseEntity<Team> updateUserTeam(@Valid @RequestBody Team team) {
        if (UserId.VBR_USER_ID.equals(team.getUserId())) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }

        boolean result = userService.updateUserTeam(team);

        if (result) {
            return new ResponseEntity<>(team, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to update team %s for user %s", team.getName(), team.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "", params = { "userId", "name", "gender", "kind" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserTeam(@RequestParam("userId") String userId, @RequestParam("name") String name, @RequestParam("gender") String gender, @RequestParam("kind") String kind) {
        if (UserId.VBR_USER_ID.equals(userId)) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }

        name = ControllerUtils.decodeUrlParameter(name);
        boolean result = userService.deleteUserTeam(userId, name, gender, kind);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete team %s for user %s", name, userId));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "", params = { "userId" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteAllUserTeams(@RequestParam("userId") String userId) {
        if (UserId.VBR_USER_ID.equals(userId)) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }

        boolean result = userService.deleteAllUserTeams(userId);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete all teams for user %s", userId));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private void hideUserId(List<Team> teams) {
        for (Team team : teams) {
            team.setUserId(null);
        }
    }

}
