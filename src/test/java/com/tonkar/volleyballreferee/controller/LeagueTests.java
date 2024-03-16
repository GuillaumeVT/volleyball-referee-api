package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeagueTests extends VbrMockedTests {

    @Test
    void test_leagues_unauthorized() {
        final var invalidToken = "invalid";

        webTestClient
                .get()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/leagues").queryParam("kind", GameType.INDOOR).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/leagues/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/leagues/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .post()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(new League())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .delete()
                .uri("/leagues/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_leagues_list() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createLeague(userToken.user().id(), GameType.INDOOR_4X4);

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(LeagueSummary.class)
                .hasSize(1);
    }

    @Test
    void test_leagues_list_byKind() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        League league = sandbox.createLeague(userToken.user().id(), GameType.INDOOR_4X4);
        GameType noResultGameType = GameType.BEACH;
        String kinds = String.join(",", noResultGameType.toString(), league.getKind().toString());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/leagues").queryParam("kind", league.getKind()).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(LeagueSummary.class)
                .hasSize(1);

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/leagues").queryParam("kind", noResultGameType).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(LeagueSummary.class)
                .hasSize(0);

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/leagues").queryParam("kind", kinds).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(LeagueSummary.class)
                .hasSize(1);
    }

    @Test
    void test_leagues_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        League league = sandbox.createLeague(userToken.user().id(), GameType.INDOOR_4X4);

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/leagues/%s".formatted(league.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(League.class)
                .value(league1 -> assertEquals(league.getName(), league1.getName()));
    }

    @Test
    void test_leagues_get_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/leagues/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_leagues_public_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        League league = sandbox.createLeague(userToken.user().id(), GameType.INDOOR);

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/public/leagues/%s".formatted(league.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(League.class)
                .value(league1 -> assertEquals(league.getName(), league1.getName()));
    }

    @Test
    void test_leagues_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        League league = sandbox.generateLeague(userToken.user().id(), GameType.INDOOR_4X4);

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(league)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void test_leagues_create_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        League league = sandbox.createLeague(userToken.user().id(), GameType.INDOOR_4X4);

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(league)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_leagues_create_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        League league = sandbox.createLeague(userToken.user().id(), GameType.BEACH);
        league.setId(UUID.randomUUID());

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/leagues")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(league)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_leagues_count() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createLeague(userToken.user().id(), GameType.INDOOR_4X4);

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/leagues/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Count.class)
                .value(count -> assertEquals(1L, count.count()));
    }

    @Test
    void test_leagues_delete() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        League league = sandbox.createLeague(userToken.user().id(), GameType.INDOOR_4X4);

        // WHEN / THEN
        webTestClient
                .delete()
                .uri("/leagues/%s".formatted(league.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
