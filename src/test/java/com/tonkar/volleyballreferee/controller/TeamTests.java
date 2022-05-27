package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.TeamSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.service.TeamService;
import com.tonkar.volleyballreferee.util.TestPageImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeamTests extends VbrMockedTests {

    @Test
    public void test_teams_unauthorized() {
        final var invalidToken = "invalid";

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", GameType.INDOOR)
                .queryParam("page", 0)
                .queryParam("size", 20);
        errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/teams/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/teams/count", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/teams", HttpMethod.POST, payloadWithAuth(invalidToken, new Team()), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/teams", HttpMethod.PUT, payloadWithAuth(invalidToken, new Team()), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/teams/" + UUID.randomUUID(), HttpMethod.DELETE, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/teams", HttpMethod.DELETE, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void test_teams_list() {
        // GIVEN
        ParameterizedTypeReference<TestPageImpl<TeamSummary>> pageType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<TeamSummary>> teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_teams_list_byKind() {
        // GIVEN
        ParameterizedTypeReference<TestPageImpl<TeamSummary>> pageType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", team.getKind())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<TeamSummary>> teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());

        // GIVEN
        GameType noResultGameType = GameType.INDOOR_4X4;

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", noResultGameType)
                .queryParam("page", 0)
                .queryParam("size", 20);
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(0, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());

        // GIVEN
        String kind = String.join(",", noResultGameType.toString(), team.getKind().toString());

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", kind)
                .queryParam("page", 0)
                .queryParam("size", 20);
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_teams_list_byGender() {
        // GIVEN
        ParameterizedTypeReference<TestPageImpl<TeamSummary>> pageType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("gender", team.getGender())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<TeamSummary>> teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());

        // GIVEN
        GenderType noResultGenderType = GenderType.GENTS;

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("gender", noResultGenderType)
                .queryParam("page", 0)
                .queryParam("size", 20);
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(0, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());

        // GIVEN
        String gender = String.join(",", noResultGenderType.toString(), team.getGender().toString());

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("gender", gender)
                .queryParam("page", 0)
                .queryParam("size", 20);
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_teams_list_byKindAndGender() {
        // GIVEN
        ParameterizedTypeReference<TestPageImpl<TeamSummary>> pageType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", team.getKind())
                .queryParam("gender", team.getGender())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<TeamSummary>> teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());

        // GIVEN
        GameType noResultGameType = GameType.INDOOR_4X4;
        GenderType noResultGenderType = GenderType.GENTS;

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", noResultGameType)
                .queryParam("gender", noResultGenderType)
                .queryParam("page", 0)
                .queryParam("size", 20);
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(0, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());

        // GIVEN
        String kind = String.join(",", noResultGameType.toString(), team.getKind().toString());
        String gender = String.join(",", noResultGenderType.toString(), team.getGender().toString());

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", kind)
                .queryParam("gender", gender)
                .queryParam("page", 0)
                .queryParam("size", 20);
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_teams_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        ResponseEntity<Team> teamResponse = restTemplate.exchange("/teams/" + team.getId(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), Team.class);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(team.getName(), Objects.requireNonNull(teamResponse.getBody()).getName());
    }

    @Test
    public void test_teams_get_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/teams/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    public void test_teams_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.generateBeachTeam(userToken.user().id());

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams", HttpMethod.POST, payloadWithAuth(userToken.token(), team), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());
    }

    @Test
    public void test_teams_create_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/teams", HttpMethod.POST, payloadWithAuth(userToken.token(), team), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    public void test_teams_create_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());
        team.setId(UUID.randomUUID());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/teams", HttpMethod.POST, payloadWithAuth(userToken.token(), team), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    public void test_teams_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        team.setUpdatedAt(System.currentTimeMillis());
        team.setColor(faker.color().hex());

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams", HttpMethod.PUT, payloadWithAuth(userToken.token(), team), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
    }

    @Test
    public void test_teams_update_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.generateBeachTeam(userToken.user().id());

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams", HttpMethod.PUT, payloadWithAuth(userToken.token(), team), Void.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, teamResponse.getStatusCode());
    }

    @Test
    public void test_teams_count() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        ResponseEntity<Count> teamResponse = restTemplate.exchange("/teams/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1L, Objects.requireNonNull(teamResponse.getBody()).count());
    }

    @Test
    public void test_teams_delete(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Team team = sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams/" + team.getId(), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.token()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());
        assertTrue(teamService.listTeams(sandbox.getUser(userToken.user().id()), List.of(GameType.values()), List.of(GenderType.values()), PageRequest.of(0, 50)).isEmpty());
    }

    @Test
    public void test_teams_deleteAll(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachTeam(userToken.user().id());

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams", HttpMethod.DELETE, emptyPayloadWithAuth(userToken.token()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());
        assertTrue(teamService.listTeams(sandbox.getUser(userToken.user().id()), List.of(GameType.values()), List.of(GenderType.values()), PageRequest.of(0, 50)).isEmpty());
    }
}
