package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.GameSummaryDto;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.entity.Set;
import com.tonkar.volleyballreferee.service.GameService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

@ContextConfiguration(classes = GameController.class)
class GameTests extends VbrControllerTests {

    @MockitoBean
    private GameService gameService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_listGames(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("status", GameStatus.COMPLETED)
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
    void test_games_listAvailableGames(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/games/available")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_listCompletedGames(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/games/completed").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_listGamesInLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/league/%s".formatted(UUID.randomUUID()))
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
    void test_games_getGame(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/games/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_getNumberOfGames(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/games/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_getNumberOfGamesInLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/games/league/%s/count".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_getNumberOfAvailableGames(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/games/available/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, CREATED", "adminToken, CREATED", "invalidToken, UNAUTHORIZED" })
    void test_games_createGame(String token, HttpStatus responseCode) {
        var gameSummary = new GameSummaryDto();
        gameSummary.setId(UUID.randomUUID());
        gameSummary.setCreatedBy(UUID.randomUUID());
        gameSummary.setRefereedBy(UUID.randomUUID());
        gameSummary.setRefereeName(faker.name().firstName());
        gameSummary.setKind(GameType.BEACH);
        gameSummary.setGender(GenderType.LADIES);
        gameSummary.setUsage(UsageType.NORMAL);
        gameSummary.setStatus(GameStatus.SCHEDULED);
        gameSummary.setHomeTeamId(UUID.randomUUID());
        gameSummary.setHomeTeamName(faker.rockBand().name());
        gameSummary.setGuestTeamId(UUID.randomUUID());
        gameSummary.setGuestTeamName(faker.rockBand().name());
        gameSummary.setRulesId(UUID.randomUUID());
        gameSummary.setRulesName(faker.book().title());

        webTestClient
                .post()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(gameSummary)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_updateGame(String token, HttpStatus responseCode) {
        var gameSummary = new GameSummaryDto();
        gameSummary.setId(UUID.randomUUID());
        gameSummary.setCreatedBy(UUID.randomUUID());
        gameSummary.setRefereedBy(UUID.randomUUID());
        gameSummary.setRefereeName(faker.name().firstName());
        gameSummary.setKind(GameType.BEACH);
        gameSummary.setGender(GenderType.LADIES);
        gameSummary.setUsage(UsageType.NORMAL);
        gameSummary.setStatus(GameStatus.SCHEDULED);
        gameSummary.setHomeTeamId(UUID.randomUUID());
        gameSummary.setHomeTeamName(faker.rockBand().name());
        gameSummary.setGuestTeamId(UUID.randomUUID());
        gameSummary.setGuestTeamName(faker.rockBand().name());
        gameSummary.setRulesId(UUID.randomUUID());
        gameSummary.setRulesName(faker.book().title());

        webTestClient
                .put()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(gameSummary)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_updateSet(String token, HttpStatus responseCode) {
        var set = new Set();
        set.setLadder(new ArrayList<>());
        set.setServing("H");
        set.setFirstServing("H");
        set.setHomeSubstitutions(new ArrayList<>());
        set.setGuestSubstitutions(new ArrayList<>());
        set.setHomeCalledTimeouts(new ArrayList<>());
        set.setGuestCalledTimeouts(new ArrayList<>());

        webTestClient
                .patch()
                .uri("/games/%s/set/2".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(set)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_games_setReferee(String token, HttpStatus responseCode) {
        webTestClient
                .patch()
                .uri("/games/%s/referee/%s".formatted(UUID.randomUUID(), UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_games_deleteGame(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/games/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_games_deleteAllGames(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_games_deleteAllGamesInLeague(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/games/league/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
