package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.Team;
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
@RequestMapping("/api/user/team")
@CrossOrigin("*")
public class UserTeamController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTeamController.class);

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<List<Team>> listUserTeams(@AuthenticationPrincipal User user) {
        List<Team> teams = userService.listUserTeams(user.getUserId());
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "kind" }, method = RequestMethod.GET)
    public ResponseEntity<List<Team>> listUserTeamsOfKind(@AuthenticationPrincipal User user, @RequestParam("kind") String kind) {
        List<Team> teams = userService.listUserTeamsOfKind(user.getUserId(), kind);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "name", "gender", "kind" }, method = RequestMethod.GET)
    public ResponseEntity<Team> getUserTeam(@AuthenticationPrincipal User user, @RequestParam("name") String name, @RequestParam("gender") String gender, @RequestParam("kind") String kind) {
        name = ControllerUtils.decodeUrlParameter(name);
        Team team = userService.getUserTeam(user.getUserId(), name, gender, kind);

        if (team == null) {
            LOGGER.error(String.format("No team %s found for user %s", name, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(team, HttpStatus.OK);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getNumberOfUserTeams(@AuthenticationPrincipal User user) {
        long numberOfTeams = userService.getNumberOfUserTeams(user.getUserId());
        return new ResponseEntity<>(numberOfTeams, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Team> createUserTeam(@AuthenticationPrincipal User user, @Valid @RequestBody Team team) {
        boolean result = userService.createUserTeam(team);

        if (result) {
            return new ResponseEntity<>(team, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("Team %s already exists for user %s", team.getName(), team.getUserId()));
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("")
    public ResponseEntity<Team> updateUserTeam(@AuthenticationPrincipal User user, @Valid @RequestBody Team team) {
        boolean result = userService.updateUserTeam(team);

        if (result) {
            return new ResponseEntity<>(team, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to update team %s for user %s", team.getName(), team.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "", params = { "name", "gender", "kind" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserTeam(@AuthenticationPrincipal User user, @RequestParam("name") String name, @RequestParam("gender") String gender, @RequestParam("kind") String kind) {
        name = ControllerUtils.decodeUrlParameter(name);
        boolean result = userService.deleteUserTeam(user.getUserId(), name, gender, kind);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete team %s for user %s", name, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteAllUserTeams(@AuthenticationPrincipal User user) {
        boolean result = userService.deleteAllUserTeams(user.getUserId());

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete all teams for user %s", user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }



}
