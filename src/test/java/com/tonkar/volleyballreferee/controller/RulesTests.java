package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.RulesService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

@ContextConfiguration(classes = RulesController.class)
class RulesTests extends VbrControllerTests {

    @MockBean
    private RulesService rulesService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_rules_listRules(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_rules_getRules(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/rules/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_rules_getDefaultRules(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/rules/default/kind/%s".formatted(GameType.INDOOR))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_rules_getNumberOfRules(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/rules/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, CREATED", "adminToken, CREATED", "invalidToken, UNAUTHORIZED" })
    void test_rules_createRules(String token, HttpStatus responseCode) {
        var rules = Rules.OFFICIAL_INDOOR_RULES;
        rules.setCreatedBy(UUID.randomUUID());

        webTestClient
                .post()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(rules)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_rules_updateRules(String token, HttpStatus responseCode) {
        var rules = Rules.OFFICIAL_INDOOR_RULES;
        rules.setCreatedBy(UUID.randomUUID());

        webTestClient
                .put()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(rules)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_rules_deleteRules(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/rules/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_rules_deleteAllRules(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/rules")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
