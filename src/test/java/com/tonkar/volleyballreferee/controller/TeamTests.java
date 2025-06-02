package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.TeamService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

@ContextConfiguration(classes = TeamController.class)
class TeamTests extends VbrControllerTests {

    @MockitoBean
    private TeamService teamService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_teams_listTeams(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("kind", GameType.INDOOR)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_teams_getTeam(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/teams/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_teams_getNumberOfTeams(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/teams/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, CREATED", "adminToken, CREATED", "invalidToken, UNAUTHORIZED" })
    void test_teams_createTeam(String token, HttpStatus responseCode) {
        var team = new Team();
        team.setId(UUID.randomUUID());
        team.setCreatedBy(UUID.randomUUID());
        team.setName(faker.team().name());
        team.setKind(GameType.BEACH);
        team.setGender(GenderType.LADIES);
        team.setColor(faker.color().hex());
        team.setLiberoColor(faker.color().hex());
        team.setPlayers(new ArrayList<>());
        team.setLiberos(new ArrayList<>());

        webTestClient
                .post()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(team)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_teams_updateTeam(String token, HttpStatus responseCode) {
        var team = new Team();
        team.setId(UUID.randomUUID());
        team.setCreatedBy(UUID.randomUUID());
        team.setName(faker.team().name());
        team.setKind(GameType.BEACH);
        team.setGender(GenderType.LADIES);
        team.setColor(faker.color().hex());
        team.setLiberoColor(faker.color().hex());
        team.setPlayers(new ArrayList<>());
        team.setLiberos(new ArrayList<>());

        webTestClient
                .put()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(team)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_teams_deleteTeam(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/teams/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_teams_deleteAllTeams(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
