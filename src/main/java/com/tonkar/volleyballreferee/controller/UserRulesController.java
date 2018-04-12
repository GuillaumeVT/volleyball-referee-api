package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.model.Rules;
import com.tonkar.volleyballreferee.model.User;
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
@RequestMapping("/api/user/rules")
@CrossOrigin("*")
public class UserRulesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRulesController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<List<Rules>> getRules(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        User user = userService.getUser(new UserId(socialId, provider));
        List<Rules> rules = userService.getUserRules(user);
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }

    @GetMapping("/default")
    public ResponseEntity<List<Rules>> getDefaultRules() {
        List<Rules> rules = userService.getDefaultRules();
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "name" }, method = RequestMethod.GET)
    public ResponseEntity<Rules> getRules(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("name") String name) {
        name = ControllerUtils.decodeUrlParameters(name);
        User user = userService.getUser(new UserId(socialId, provider));
        Rules rules = userService.getUserRules(user, name);

        if (rules == null) {
            LOGGER.error(String.format("No rules %s found for user %s", name, user.getUserId().getSocialId()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(rules, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/count", params = { "socialId", "provider" }, method = RequestMethod.GET)
    public ResponseEntity<Long> getNumberOfRules(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider) {
        User user = userService.getUser(new UserId(socialId, provider));
        long numberOfRules = userService.getNumberOfUserRules(user);
        return new ResponseEntity<>(numberOfRules, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Rules> createRules(@Valid @RequestBody Rules rules) {
        User user = userService.getUser(rules.getUserId());
        boolean result = userService.createUserRules(user, rules);

        if (result) {
            return new ResponseEntity<>(rules, HttpStatus.CREATED);
        } else {
            LOGGER.error(String.format("Rules %s already exist for user %s", rules.getName(), user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @PutMapping("")
    public ResponseEntity<Rules> updateRules(@Valid @RequestBody Rules rules) {
        User user = userService.getUser(rules.getUserId());
        boolean result = userService.updateUserRules(user, rules);

        if (result) {
            return new ResponseEntity<>(rules, HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to update rules %s for user %s", rules.getName(), user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    @RequestMapping(value = "", params = { "socialId", "provider", "name" }, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteRules(@RequestParam("socialId") String socialId, @RequestParam("provider") String provider, @RequestParam("name") String name) {
        name = ControllerUtils.decodeUrlParameters(name);
        User user = userService.getUser(new UserId(socialId, provider));
        boolean result = userService.deleteUserRules(user, name);

        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error(String.format("Failed to delete rules %s for user %s", name, user.getUserId()));
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

}
