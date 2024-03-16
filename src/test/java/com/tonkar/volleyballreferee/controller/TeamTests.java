package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.TeamService;
import com.tonkar.volleyballreferee.util.TestPageImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TeamTests extends VbrMockedTests {

    private final TeamService teamService;

    public TeamTests(@Autowired TeamService teamService) {
        super();
        this.teamService = teamService;
    }

    @Test
    void test_teams_unauthorized() {
        final var invalidToken = "invalid";

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/teams").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("kind", GameType.INDOOR)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/teams/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/teams/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .post()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(new Team())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .put()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(new Team())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .delete()
                .uri("/teams/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .delete()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_teams_list() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/teams").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_teams_list_byKind() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("kind", team.getKind())
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));

        // GIVEN
        GameType noResultGameType = GameType.INDOOR_4X4;

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("kind", noResultGameType)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(0, page.getTotalElements()));

        // GIVEN
        String kinds = String.join(",", noResultGameType.toString(), team.getKind().toString());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/teams").queryParam("kind", kinds).queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_teams_list_byGender() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("gender", team.getGender())
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));

        // GIVEN
        GenderType noResultGenderType = GenderType.GENTS;

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("gender", noResultGenderType)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(0, page.getTotalElements()));

        // GIVEN
        String genders = String.join(",", noResultGenderType.toString(), team.getGender().toString());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("gender", genders)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_teams_list_byKindAndGender() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("kind", team.getKind())
                        .queryParam("gender", team.getGender())
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));

        // GIVEN
        GameType noResultGameType = GameType.INDOOR_4X4;
        GenderType noResultGenderType = GenderType.GENTS;

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("kind", noResultGameType)
                        .queryParam("gender", noResultGenderType)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(0, page.getTotalElements()));

        // GIVEN
        String kinds = String.join(",", noResultGameType.toString(), team.getKind().toString());
        String genders = String.join(",", noResultGenderType.toString(), team.getGender().toString());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("kind", kinds)
                        .queryParam("gender", genders)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<TeamSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_teams_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/teams/%s".formatted(team.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Team.class)
                .value(team1 -> assertEquals(team.getName(), team1.getName()));
    }

    @Test
    void test_teams_get_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/teams/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_teams_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.generateBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(team)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void test_teams_create_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(team)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_teams_create_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());
        team.setId(UUID.randomUUID());

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(team)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_teams_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        team.setUpdatedAt(System.currentTimeMillis());
        team.setColor(faker.color().hex());

        // WHEN / THEN
        webTestClient
                .put()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(team)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_teams_update_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.generateBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .put()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(team)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_teams_count() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/teams/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Count.class)
                .value(count -> assertEquals(1L, count.count()));
    }

    @Test
    void test_teams_delete() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .delete()
                .uri("/teams/%s".formatted(team.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(teamService
                           .listTeams(sandbox.getUser(userToken.user().id()), List.of(GameType.values()), List.of(GenderType.values()),
                                      PageRequest.of(0, 50))
                           .isEmpty());
    }

    @Test
    void test_teams_deleteAll() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachTeam(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .delete()
                .uri("/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(teamService
                           .listTeams(sandbox.getUser(userToken.user().id()), List.of(GameType.values()), List.of(GenderType.values()),
                                      PageRequest.of(0, 50))
                           .isEmpty());
    }
}
