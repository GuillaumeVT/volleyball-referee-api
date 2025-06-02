package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.LeagueService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

@ContextConfiguration(classes = LeagueController.class)
class LeagueTests extends VbrControllerTests {

    @MockitoBean
    private LeagueService leagueService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_leagues_listLeagues(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_leagues_getLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/leagues/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_leagues_getNumberOfLeagues(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/leagues/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, CREATED", "adminToken, CREATED", "invalidToken, UNAUTHORIZED" })
    void test_leagues_createLeague(String token, HttpStatus responseCode) {
        var league = new League();
        league.setId(UUID.randomUUID());
        league.setCreatedBy(UUID.randomUUID());
        league.setKind(GameType.INDOOR);
        league.setName(faker.rockBand().name());
        league.setDivisions(new ArrayList<>());

        webTestClient
                .post()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(league)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_leagues_deleteLeague(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/leagues/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_leagues_deleteAllLeagues(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
