package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.Rules;
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
@RequestMapping("/api/user/rules")
@CrossOrigin("*")
public class UserRulesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRulesController.class);

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<List<Rules>> listUserRules(@AuthenticationPrincipal User user) {
        List<Rules> rules = userService.listUserRules(user.getUserId());
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }

    @GetMapping("/default")
    public ResponseEntity<List<Rules>> listDefaultRules() {
        List<Rules> rules = userService.listDefaultRules();
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "name" }, method = RequestMethod.GET)
    public ResponseEntity<Rules> getUserRules(@AuthenticationPrincipal User user, @RequestParam("name") String name) {
        name = ControllerUtils.decodeUrlParameter(name);
        Rules rules = userService.getUserRules(user.getUserId(), name);

        if (rules == null) {
            LOGGER.error(String.format("No rules %s found for user %s", name, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(rules, HttpStatus.OK);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getNumberOfUserRules(@AuthenticationPrincipal User user) {
        long numberOfRules = userService.getNumberOfUserRules(user.getUserId());
        return new ResponseEntity<>(numberOfRules, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Rules> createUserRules(@AuthenticationPrincipal User user, @Valid @RequestBody Rules rules) {
        boolean result = userService.createUserRules(rules);

        if (result) {
            return new ResponseEntity<>(rules, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("Rules %s already exist for user %s", rules.getName(), rules.getUserId()));
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("")
    public ResponseEntity<Rules> updateUserRules(@AuthenticationPrincipal User user, @Valid @RequestBody Rules rules) {
        boolean result = userService.updateUserRules(rules);

        if (result) {
            return new ResponseEntity<>(rules, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to update rules %s for user %s", rules.getName(), rules.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "", params = { "name" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserRules(@AuthenticationPrincipal User user, @RequestParam("name") String name) {
        name = ControllerUtils.decodeUrlParameter(name);
        boolean result = userService.deleteUserRules(user.getUserId(), name);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete rules %s for user %s", name, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteAllUserRules(@AuthenticationPrincipal User user) {
        boolean result = userService.deleteAllUserRules(user.getUserId());

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete all rules for user %s", user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
