package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.RulesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RulesTests extends VbrMockedTests {

    private final RulesService rulesService;

    public RulesTests(@Autowired RulesService rulesService) {
        super();
        this.rulesService = rulesService;
    }

    @Test
    void test_rules_unauthorized() {
        final var invalidToken = "invalid";

        webTestClient
                .get()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/rules").queryParam("kind", GameType.INDOOR).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/rules/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/rules/default/kind/%s".formatted(GameType.INDOOR))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/rules/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .post()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(Rules.OFFICIAL_INDOOR_RULES)
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .put()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(Rules.OFFICIAL_INDOOR_RULES)
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .delete()
                .uri("/rules/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .delete()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_rules_list() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createIndoorRules(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(RulesSummary.class)
                .hasSize(1);
    }

    @Test
    void test_rules_list_byKind() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());
        GameType noResultGameType = GameType.BEACH;
        String kinds = String.join(",", noResultGameType.toString(), rules.getKind().toString());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/rules").queryParam("kind", rules.getKind()).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(RulesSummary.class)
                .hasSize(1);

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/rules").queryParam("kind", noResultGameType).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(RulesSummary.class)
                .hasSize(0);

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/rules").queryParam("kind", kinds).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(RulesSummary.class)
                .hasSize(1);
    }

    @Test
    void test_rules_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/rules/%s".formatted(rules.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Rules.class)
                .value(rules1 -> assertEquals(rules.getName(), rules1.getName()));
    }

    @Test
    void test_rules_get_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/rules/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @ParameterizedTest
    @EnumSource(GameType.class)
    void test_rules_get_default(final GameType gameType) {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/rules/default/kind/%s".formatted(gameType))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Rules.class)
                .value(rules -> assertEquals(gameType, rules.getKind()));
    }

    @Test
    void test_rules_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.generateIndoorRules(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(rules)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void test_rules_create_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(rules)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_rules_create_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());
        rules.setId(UUID.randomUUID());

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(rules)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_rules_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());

        rules.setUpdatedAt(System.currentTimeMillis());
        rules.setCustomConsecutiveServesPerPlayer(10);
        rules.setBeachCourtSwitches(true);
        rules.setGameIntervalDuration(10);

        // WHEN / THEN
        webTestClient
                .put()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(rules)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_rules_update_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.generateIndoorRules(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .put()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(rules)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_rules_count() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createIndoorRules(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/rules/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Count.class)
                .value(count -> assertEquals(1L, count.count()));
    }

    @Test
    void test_rules_delete() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .delete()
                .uri("/rules/%s".formatted(rules.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(rulesService.listRules(sandbox.getUser(userToken.user().id()), List.of(GameType.values())).isEmpty());
    }

    @Test
    void test_rules_deleteAll() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createIndoorRules(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .delete()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(rulesService.listRules(sandbox.getUser(userToken.user().id()), List.of(GameType.values())).isEmpty());
    }
}
