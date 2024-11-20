package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.RulesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class RulesController {

    private final RulesService rulesService;

    @GetMapping(value = "/rules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RulesSummaryDto>> listRules(@AuthenticationPrincipal User user,
                                                           @RequestParam(value = "kind", required = false) java.util.Set<GameType> kinds) {
        return new ResponseEntity<>(rulesService.listRules(user, kinds), HttpStatus.OK);
    }

    @GetMapping(value = "/rules/{rulesId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Rules> getRules(@AuthenticationPrincipal User user, @PathVariable("rulesId") UUID rulesId) {
        return new ResponseEntity<>(rulesService.getRules(user, rulesId), HttpStatus.OK);
    }

    @GetMapping(value = "/rules/default/kind/{kind}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RulesSummaryDto> getDefaultRules(@AuthenticationPrincipal User user, @PathVariable("kind") GameType kind) {
        return new ResponseEntity<>(rulesService.getDefaultRules(kind), HttpStatus.OK);
    }

    @GetMapping(value = "/rules/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CountDto> getNumberOfRules(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(rulesService.getNumberOfRules(user), HttpStatus.OK);
    }

    @PostMapping(value = "/rules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createRules(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Rules rules) {
        rulesService.createRules(user, rules);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/rules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateRules(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody Rules rules) {
        rulesService.updateRules(user, rules);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/rules/{rulesId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteRules(@AuthenticationPrincipal User user, @PathVariable("rulesId") UUID rulesId) {
        rulesService.deleteRules(user, rulesId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/rules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAllRules(@AuthenticationPrincipal User user) {
        rulesService.deleteAllRules(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
