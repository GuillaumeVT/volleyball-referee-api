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

import java.util.ArrayList;
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
    public void test_teams_list(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);
        ParameterizedTypeReference<TestPageImpl<TeamSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<TeamSummary>> teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_teams_list_byKind(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);
        ParameterizedTypeReference<TestPageImpl<TeamSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", team.getKind())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<TeamSummary>> teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

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
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

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
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_teams_list_byGender(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);
        ParameterizedTypeReference<TestPageImpl<TeamSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("gender", team.getGender())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<TeamSummary>> teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

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
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

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
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_teams_list_byKindAndGender(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);
        ParameterizedTypeReference<TestPageImpl<TeamSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/teams")
                .queryParam("kind", team.getKind())
                .queryParam("gender", team.getGender())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<TeamSummary>> teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

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
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

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
        teamResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(teamResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_teams_get(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);

        // WHEN
        ResponseEntity<Team> teamResponse = restTemplate.exchange("/teams/" + team.getId(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Team.class);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(team.getName(), Objects.requireNonNull(teamResponse.getBody()).getName());
    }

    @Test
    public void test_teams_get_notFound() {
        // GIVEN
        UserToken userToken = createUser();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/teams/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    public void test_teams_create() {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams", HttpMethod.POST, payloadWithAuth(userToken.getToken(), team), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());
    }

    @Test
    public void test_teams_create_conflict(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/teams", HttpMethod.POST, payloadWithAuth(userToken.getToken(), team), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    public void test_teams_create_conflict2(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);
        team.setId(UUID.randomUUID());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/teams", HttpMethod.POST, payloadWithAuth(userToken.getToken(), team), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    public void test_teams_update(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);

        team.setUpdatedAt(System.currentTimeMillis());
        team.setColor(faker.color().hex());

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), team), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
    }

    @Test
    public void test_teams_update_notFound() {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), team), Void.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, teamResponse.getStatusCode());
    }

    @Test
    public void test_teams_count(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);

        // WHEN
        ResponseEntity<Count> teamResponse = restTemplate.exchange("/teams/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());
        assertEquals(1L, Objects.requireNonNull(teamResponse.getBody()).getCount());
    }

    @Test
    public void test_teams_delete(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams/" + team.getId(), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());
        assertTrue(teamService.listTeams(getUser(userToken.getUser().getId()), List.of(GameType.values()), List.of(GenderType.values()), PageRequest.of(0, 50)).isEmpty());
    }

    @Test
    public void test_teams_deleteAll(@Autowired TeamService teamService) {
        // GIVEN
        UserToken userToken = createUser();
        Team team = generateTeam(userToken.getUser().getId());
        teamService.createTeam(getUser(userToken.getUser().getId()), team);

        // WHEN
        ResponseEntity<Void> teamResponse = restTemplate.exchange("/teams", HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());
        assertTrue(teamService.listTeams(getUser(userToken.getUser().getId()), List.of(GameType.values()), List.of(GenderType.values()), PageRequest.of(0, 50)).isEmpty());
    }
    
    private Team generateTeam(String userId) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setCreatedBy(userId);
        team.setCreatedAt(System.currentTimeMillis());
        team.setUpdatedAt(System.currentTimeMillis());
        team.setName(faker.team().name());
        team.setKind(GameType.BEACH);
        team.setGender(GenderType.LADIES);
        team.setColor(faker.color().hex());
        team.setLiberoColor(faker.color().hex());
        team.setPlayers(new ArrayList<>());
        team.getPlayers().add(new Team.Player(1, faker.name().fullName()));
        team.getPlayers().add(new Team.Player(2, faker.name().fullName()));
        team.setLiberos(new ArrayList<>());
        team.setCaptain(1);
        return team;
    }
}
