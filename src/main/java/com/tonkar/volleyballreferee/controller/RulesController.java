package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesDescription;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.service.RulesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/rules")
@CrossOrigin("*")
@Slf4j
public class RulesController {

    @Autowired
    private RulesService rulesService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<List<RulesDescription>> listRules(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(rulesService.listRules(user.getId()), HttpStatus.OK);
    }

    @GetMapping(value = "/kind/{kind}", produces = {"application/json"})
    public ResponseEntity<List<RulesDescription>> listRulesOfKind(@AuthenticationPrincipal User user, @PathVariable("kind") GameType kind) {
        return new ResponseEntity<>(rulesService.listRulesOfKind(user.getId(), kind), HttpStatus.OK);
    }

    @GetMapping(value = "/{rulesId}", produces = {"application/json"})
    public ResponseEntity<Rules> getRules(@AuthenticationPrincipal User user, @PathVariable("rulesId") UUID rulesId) {
        try {
            return new ResponseEntity<>(rulesService.getRules(user.getId(), rulesId), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/default/kind/{kind}", produces = {"application/json"})
    public ResponseEntity<RulesDescription> getDefaultRules(@AuthenticationPrincipal User user, @PathVariable("kind") GameType kind) {
        return new ResponseEntity<>(rulesService.getDefaultRules(kind), HttpStatus.OK);
    }

    @GetMapping(value = "/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfRules(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(rulesService.getNumberOfRules(user.getId()), HttpStatus.OK);
    }

    @PostMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> createRules(@AuthenticationPrincipal User user, @Valid @RequestBody Rules rules) {
        try {
            rulesService.createRules(user.getId(), rules);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> updateRules(@AuthenticationPrincipal User user, @Valid @RequestBody Rules rules) {
        try {
            rulesService.updateRules(user.getId(), rules);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/{rulesId}", produces = {"application/json"})
    public ResponseEntity<String> deleteRules(@AuthenticationPrincipal User user, @PathVariable("rulesId") UUID rulesId) {
        try {
            rulesService.deleteRules(user.getId(), rulesId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> deleteAllRules(@AuthenticationPrincipal User user) {
        rulesService.deleteAllRules(user.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
