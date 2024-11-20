package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.LoginCredentialsDto;
import com.tonkar.volleyballreferee.entity.FileWrapper;
import com.tonkar.volleyballreferee.service.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@ContextConfiguration(classes = PublicController.class)
class PublicTests extends VbrControllerTests {

    @MockBean
    private GameService gameService;

    @MockBean
    private StatisticsService statisticsService;

    @MockBean
    private TeamService teamService;

    @MockBean
    private LeagueService leagueService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_public_signIn() {
        webTestClient
                .post()
                .uri("/public/users/token")
                .bodyValue(new LoginCredentialsDto(faker.internet().safeEmailAddress(), faker.code().ean8()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_getGlobalStatistics(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/statistics")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_getGame(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_getScoreSheet(String token, HttpStatus responseCode) {
        Mockito.doReturn(new FileWrapper(faker.book().title(), new byte[]{})).when(gameService).getScoreSheet(Mockito.any(UUID.class));

        webTestClient
                .get()
                .uri("/public/games/%s/score-sheet".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listLiveGames(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/public/games/live").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listGamesMatchingToken(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/token/%s".formatted(faker.code().ean8()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listGamesWithScheduleDate(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/date/%s".formatted(LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listGamesInLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/league/%s".formatted(UUID.randomUUID()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_getGamesInLeagueGroupedByStatus(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/group".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listLiveGamesInLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/live".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listNext10GamesInLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/next-10".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listLast10GamesInLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/last-10".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listGamesOfTeamInLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/league/%s/team/%s".formatted(UUID.randomUUID(), UUID.randomUUID()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listGamesInDivision(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/league/%s/division/%s".formatted(UUID.randomUUID(), faker.animal().name()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_getGamesInDivisionGroupedByStatus(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/division/%s/group".formatted(UUID.randomUUID(), faker.animal().name()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listLiveGamesInDivision(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/division/%s/live".formatted(UUID.randomUUID(), faker.animal().name()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listNext10GamesInDivision(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/division/%s/next-10".formatted(UUID.randomUUID(), faker.animal().name()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listLast10GamesInDivision(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/division/%s/last-10".formatted(UUID.randomUUID(), faker.animal().name()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listGamesOfTeamInDivision(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/league/%s/division/%s/team/%s".formatted(UUID.randomUUID(), faker.animal().name(),
                                                                                      UUID.randomUUID()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listRankingsInDivision(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/games/league/%s/division/%s/rankings".formatted(UUID.randomUUID(), faker.animal().name()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listGamesInDivisionExcel(String token, HttpStatus responseCode) throws IOException {
        Mockito
                .doReturn(new FileWrapper(faker.book().title(), new byte[]{}))
                .when(gameService)
                .listGamesInDivisionExcel(Mockito.any(UUID.class), Mockito.anyString());

        webTestClient
                .get()
                .uri("/public/games/league/%s/division/%s/excel".formatted(UUID.randomUUID(), faker.animal().name()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listTeamsOfLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/teams/league/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_listTeamsOfDivision(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/teams/league/%s/division/%s".formatted(UUID.randomUUID(), faker.animal().name()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, OK" })
    void test_public_getLeague(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/public/leagues/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
