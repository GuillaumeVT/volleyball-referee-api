package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesSummary;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
@CrossOrigin("*")
@Slf4j
public class RulesController {

    @Autowired
    private RulesService rulesService;

    @GetMapping(value = "/rules", produces = {"application/json"})
    public ResponseEntity<List<RulesSummary>> listRules(@AuthenticationPrincipal User user,
                                                        @RequestParam(value = "kind", required = false) List<GameType> kinds) {
        return new ResponseEntity<>(rulesService.listRules(user, kinds), HttpStatus.OK);
    }

    @GetMapping(value = "/rules/{rulesId}", produces = {"application/json"})
    public ResponseEntity<Rules> getRules(@AuthenticationPrincipal User user, @PathVariable("rulesId") UUID rulesId) throws NotFoundException {
        return new ResponseEntity<>(rulesService.getRules(user, rulesId), HttpStatus.OK);
    }

    @GetMapping(value = "/rules/default/kind/{kind}", produces = {"application/json"})
    public ResponseEntity<RulesSummary> getDefaultRules(@AuthenticationPrincipal User user, @PathVariable("kind") GameType kind) {
        return new ResponseEntity<>(rulesService.getDefaultRules(kind), HttpStatus.OK);
    }

    @GetMapping(value = "/rules/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfRules(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(rulesService.getNumberOfRules(user), HttpStatus.OK);
    }

    @PostMapping(value = "/rules", produces = {"application/json"})
    public ResponseEntity<Void> createRules(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Rules rules) throws ConflictException {
        rulesService.createRules(user, rules);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/rules", produces = {"application/json"})
    public ResponseEntity<Void> updateRules(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Rules rules) throws NotFoundException {
        rulesService.updateRules(user, rules);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/rules/{rulesId}", produces = {"application/json"})
    public ResponseEntity<Void> deleteRules(@AuthenticationPrincipal User user, @PathVariable("rulesId") UUID rulesId) throws ConflictException {
        rulesService.deleteRules(user, rulesId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/rules", produces = {"application/json"})
    public ResponseEntity<Void> deleteAllRules(@AuthenticationPrincipal User user) {
        rulesService.deleteAllRules(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
