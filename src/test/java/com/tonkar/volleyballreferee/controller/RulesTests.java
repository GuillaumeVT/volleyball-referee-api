package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.RulesSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import com.tonkar.volleyballreferee.service.RulesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RulesTests extends VbrMockedTests {

    @Test
    void test_rules_unauthorized() {
        final var invalidToken = "invalid";
        
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/rules", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/rules").queryParam("kind", GameType.INDOOR);
        errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules/default/kind/" + GameType.INDOOR, HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules/count", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules", HttpMethod.POST, payloadWithAuth(invalidToken, Rules.OFFICIAL_INDOOR_RULES), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules", HttpMethod.PUT, payloadWithAuth(invalidToken, Rules.OFFICIAL_INDOOR_RULES), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules/" + UUID.randomUUID(), HttpMethod.DELETE, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules", HttpMethod.DELETE, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    void test_rules_list() {
        // GIVEN
        ParameterizedTypeReference<List<RulesSummary>> listType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        sandbox.createIndoorRules(userToken.user().id());

        // WHEN
        ResponseEntity<List<RulesSummary>> rulesResponse = restTemplate.exchange("/rules", HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), listType);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(rulesResponse.getBody()).size());
    }

    @Test
    void test_rules_list_byKind() {
        // GIVEN
        ParameterizedTypeReference<List<RulesSummary>> listType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/rules")
                .queryParam("kind", rules.getKind());
        ResponseEntity<List<RulesSummary>> rulesResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), listType);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(rulesResponse.getBody()).size());

        // GIVEN
        GameType noResultGameType = GameType.BEACH;

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/rules")
                .queryParam("kind", noResultGameType);
        rulesResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), listType);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
        assertEquals(0, Objects.requireNonNull(rulesResponse.getBody()).size());

        // GIVEN
        String kind = String.join(",", noResultGameType.toString(), rules.getKind().toString());

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/rules")
                .queryParam("kind", kind);
        rulesResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), listType);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(rulesResponse.getBody()).size());
    }

    @Test
    void test_rules_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());

        // WHEN
        ResponseEntity<Rules> rulesResponse = restTemplate.exchange("/rules/" + rules.getId(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), Rules.class);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
        assertEquals(rules.getName(), Objects.requireNonNull(rulesResponse.getBody()).getName());
    }

    @Test
    void test_rules_get_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/rules/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    void test_rules_get_default() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameType gameType = GameType.INDOOR;

        // WHEN
        ResponseEntity<RulesSummary> rulesResponse = restTemplate.exchange("/rules/default/kind/" + gameType, HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), RulesSummary.class);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
        assertEquals(gameType, Objects.requireNonNull(rulesResponse.getBody()).kind());

        // GIVEN
        gameType = GameType.BEACH;

        // WHEN
        rulesResponse = restTemplate.exchange("/rules/default/kind/" + gameType, HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), RulesSummary.class);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
        assertEquals(gameType, Objects.requireNonNull(rulesResponse.getBody()).kind());
    }

    @Test
    void test_rules_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.generateIndoorRules(userToken.user().id());

        // WHEN
        ResponseEntity<Void> rulesResponse = restTemplate.exchange("/rules", HttpMethod.POST, payloadWithAuth(userToken.token(), rules), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, rulesResponse.getStatusCode());
    }

    @Test
    void test_rules_create_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/rules", HttpMethod.POST, payloadWithAuth(userToken.token(), rules), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    void test_rules_create_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());
        rules.setId(UUID.randomUUID());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/rules", HttpMethod.POST, payloadWithAuth(userToken.token(), rules), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
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

        // WHEN
        ResponseEntity<Void> rulesResponse = restTemplate.exchange("/rules", HttpMethod.PUT, payloadWithAuth(userToken.token(), rules), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
    }

    @Test
    void test_rules_update_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.generateIndoorRules(userToken.user().id());

        // WHEN
        ResponseEntity<Void> rulesResponse = restTemplate.exchange("/rules", HttpMethod.PUT, payloadWithAuth(userToken.token(), rules), Void.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, rulesResponse.getStatusCode());
    }

    @Test
    void test_rules_count() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createIndoorRules(userToken.user().id());

        // WHEN
        ResponseEntity<Count> rulesResponse = restTemplate.exchange("/rules/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());
        assertEquals(1L, Objects.requireNonNull(rulesResponse.getBody()).count());
    }

    @Test
    void test_rules_delete(@Autowired RulesService rulesService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Rules rules = sandbox.createIndoorRules(userToken.user().id());

        // WHEN
        ResponseEntity<Void> rulesResponse = restTemplate.exchange("/rules/" + rules.getId(), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.token()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());
        assertTrue(rulesService.listRules(sandbox.getUser(userToken.user().id()), List.of(GameType.values())).isEmpty());
    }

    @Test
    void test_rules_deleteAll(@Autowired RulesService rulesService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createIndoorRules(userToken.user().id());

        // WHEN
        ResponseEntity<Void> rulesResponse = restTemplate.exchange("/rules", HttpMethod.DELETE, emptyPayloadWithAuth(userToken.token()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());
        assertTrue(rulesService.listRules(sandbox.getUser(userToken.user().id()), List.of(GameType.values())).isEmpty());
    }
}
