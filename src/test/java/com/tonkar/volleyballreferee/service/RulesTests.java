package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.GameType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

class RulesTests extends VbrServiceTests {

    private final RulesService rulesService;

    public RulesTests(@Autowired RulesService rulesService) {
        super();
        this.rulesService = rulesService;
    }

    @Test
    void test_rules_list() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.createIndoorRules(user.getId());

        // WHEN
        var rulesList = rulesService.listRules(user, Set.of());

        // THEN
        Assertions.assertEquals(1, rulesList.size());
        Assertions.assertEquals(rules.getName(), rulesList.getFirst().name());
    }

    @Test
    void test_rules_list_byKind() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.createIndoorRules(user.getId());

        // WHEN
        var rulesList = rulesService.listRules(user, Set.of(rules.getKind()));

        // THEN
        Assertions.assertEquals(1, rulesList.size());
        Assertions.assertEquals(rules.getName(), rulesList.getFirst().name());
    }

    @Test
    void test_rules_list_byKind2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createIndoorRules(user.getId());
        var noResultGameType = GameType.BEACH;

        // WHEN
        var rulesList = rulesService.listRules(user, Set.of(noResultGameType));

        // THEN
        Assertions.assertEquals(0, rulesList.size());
    }

    @Test
    void test_rules_list_byKind3() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.createIndoorRules(user.getId());

        // WHEN
        var rulesList = rulesService.listRules(user, Set.of(rules.getKind(), GameType.BEACH));

        // THEN
        Assertions.assertEquals(1, rulesList.size());
        Assertions.assertEquals(rules.getName(), rulesList.getFirst().name());
    }

    @Test
    void test_rules_get() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.createIndoorRules(user.getId());

        // WHEN
        var rules2 = rulesService.getRules(user, rules.getId());

        // THEN
        Assertions.assertNotNull(rules2);
        Assertions.assertEquals(rules.getName(), rules2.getName());
    }

    @Test
    void test_rules_get_notFound() {
        // GIVEN
        var user = sandbox.createAndGetUser();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> rulesService.getRules(user, UUID.randomUUID()));
    }

    @ParameterizedTest
    @EnumSource(GameType.class)
    void test_rules_get_default(final GameType gameType) {
        // WHEN
        var rules = rulesService.getDefaultRules(gameType);

        // THEN
        Assertions.assertNotNull(rules);
        Assertions.assertEquals(gameType, rules.kind());
    }

    @Test
    void test_rules_create() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.generateIndoorRules(user.getId());

        // WHEN / THEN
        Assertions.assertDoesNotThrow(() -> rulesService.createRules(user, rules));
    }

    @Test
    void test_rules_create_conflict() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.createIndoorRules(user.getId());

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> rulesService.createRules(user, rules));
    }

    @Test
    void test_rules_create_conflict2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.createIndoorRules(user.getId());
        rules.setId(UUID.randomUUID());

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> rulesService.createRules(user, rules));
    }

    @Test
    void test_rules_update() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.createIndoorRules(user.getId());

        rules.setUpdatedAt(System.currentTimeMillis());
        rules.setCustomConsecutiveServesPerPlayer(10);
        rules.setBeachCourtSwitches(true);
        rules.setGameIntervalDuration(10);

        // WHEN
        rulesService.updateRules(user, rules);

        // THEN
        var rules2 = rulesService.getRules(user, rules.getId());
        Assertions.assertNotNull(rules2);
        Assertions.assertEquals(rules.getCustomConsecutiveServesPerPlayer(), rules2.getCustomConsecutiveServesPerPlayer());
        Assertions.assertEquals(rules.isBeachCourtSwitches(), rules2.isBeachCourtSwitches());
        Assertions.assertEquals(rules.getGameIntervalDuration(), rules2.getGameIntervalDuration());
    }

    @Test
    void test_rules_update_notFound() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.generateBeachRules(user.getId());

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> rulesService.updateRules(user, rules));
    }

    @Test
    void test_rules_count() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createIndoorRules(user.getId());
        sandbox.createIndoorRules(user.getId());

        // WHEN
        var count = rulesService.getNumberOfRules(user);

        // THEN
        Assertions.assertNotNull(count);
        Assertions.assertEquals(2L, count.count());
    }

    @Test
    void test_rules_delete() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var rules = sandbox.createIndoorRules(user.getId());

        // WHEN
        rulesService.deleteRules(user, rules.getId());

        // THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> rulesService.getRules(user, rules.getId()));
    }

    @Test
    void test_rules_deleteAll() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createIndoorRules(user.getId());
        sandbox.createIndoorRules(user.getId());

        // WHEN
        rulesService.deleteAllRules(user);

        // THEN
        var count = rulesService.getNumberOfRules(user);
        Assertions.assertNotNull(count);
        Assertions.assertEquals(0L, count.count());
    }
}
