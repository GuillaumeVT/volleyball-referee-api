package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.LeagueSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import com.tonkar.volleyballreferee.service.LeagueService;
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

public class LeagueTests extends VbrMockedTests {

    @Test
    public void test_leagues_unauthorized() {
        final var invalidToken = "invalid";

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/leagues", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/leagues").queryParam("kind", GameType.INDOOR);
        errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/leagues/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/leagues/count", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/leagues", HttpMethod.POST, payloadWithAuth(invalidToken, new League()), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/leagues/" + UUID.randomUUID(), HttpMethod.DELETE, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void test_leagues_list(@Autowired LeagueService leagueService) {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());
        leagueService.createLeague(getUser(userToken.getUser().getId()), league);
        ParameterizedTypeReference<List<LeagueSummary>> listType = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<LeagueSummary>> leagueResponse = restTemplate.exchange("/leagues", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), listType);

        // THEN
        assertEquals(HttpStatus.OK, leagueResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(leagueResponse.getBody()).size());
    }

    @Test
    public void test_leagues_list_byKind(@Autowired LeagueService leagueService) {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());
        leagueService.createLeague(getUser(userToken.getUser().getId()), league);
        ParameterizedTypeReference<List<LeagueSummary>> listType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/leagues")
                .queryParam("kind", league.getKind());
        ResponseEntity<List<LeagueSummary>> leagueResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), listType);

        // THEN
        assertEquals(HttpStatus.OK, leagueResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(leagueResponse.getBody()).size());

        // GIVEN
        GameType noResultGameType = GameType.BEACH;

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/leagues")
                .queryParam("kind", noResultGameType);
        leagueResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), listType);

        // THEN
        assertEquals(HttpStatus.OK, leagueResponse.getStatusCode());
        assertEquals(0, Objects.requireNonNull(leagueResponse.getBody()).size());

        // GIVEN
        String kind = String.join(",", noResultGameType.toString(), league.getKind().toString());

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/leagues")
                .queryParam("kind", kind);
        leagueResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), listType);

        // THEN
        assertEquals(HttpStatus.OK, leagueResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(leagueResponse.getBody()).size());
    }

    @Test
    public void test_leagues_get(@Autowired LeagueService leagueService) {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());
        leagueService.createLeague(getUser(userToken.getUser().getId()), league);

        // WHEN
        ResponseEntity<League> leagueResponse = restTemplate.exchange("/leagues/" + league.getId(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), League.class);

        // THEN
        assertEquals(HttpStatus.OK, leagueResponse.getStatusCode());
        assertEquals(league.getName(), Objects.requireNonNull(leagueResponse.getBody()).getName());
    }

    @Test
    public void test_leagues_get_notFound() {
        // GIVEN
        UserToken userToken = createUser();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/leagues/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    public void test_leagues_public_get(@Autowired LeagueService leagueService) {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());
        leagueService.createLeague(getUser(userToken.getUser().getId()), league);

        // WHEN
        ResponseEntity<League> leagueResponse = restTemplate.exchange("/public/leagues/" + league.getId(), HttpMethod.GET, emptyPayloadWithoutAuth(), League.class);

        // THEN
        assertEquals(HttpStatus.OK, leagueResponse.getStatusCode());
        assertEquals(league.getName(), Objects.requireNonNull(leagueResponse.getBody()).getName());
    }

    @Test
    public void test_leagues_create() {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> leagueResponse = restTemplate.exchange("/leagues", HttpMethod.POST, payloadWithAuth(userToken.getToken(), league), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, leagueResponse.getStatusCode());
    }

    @Test
    public void test_leagues_create_conflict(@Autowired LeagueService leagueService) {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());
        leagueService.createLeague(getUser(userToken.getUser().getId()), league);

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/leagues", HttpMethod.POST, payloadWithAuth(userToken.getToken(), league), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    public void test_leagues_create_conflict2(@Autowired LeagueService leagueService) {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());
        leagueService.createLeague(getUser(userToken.getUser().getId()), league);
        league.setId(UUID.randomUUID());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/leagues", HttpMethod.POST, payloadWithAuth(userToken.getToken(), league), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    public void test_leagues_count(@Autowired LeagueService leagueService) {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());
        leagueService.createLeague(getUser(userToken.getUser().getId()), league);

        // WHEN
        ResponseEntity<Count> leagueResponse = restTemplate.exchange("/leagues/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, leagueResponse.getStatusCode());
        assertEquals(1L, Objects.requireNonNull(leagueResponse.getBody()).getCount());
    }

    @Test
    public void test_leagues_delete(@Autowired LeagueService leagueService) {
        // GIVEN
        UserToken userToken = createUser();
        League league = generateLeague(userToken.getUser().getId());
        leagueService.createLeague(getUser(userToken.getUser().getId()), league);

        // WHEN
        ResponseEntity<Void> leagueResponse = restTemplate.exchange("/leagues/" + league.getId(), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, leagueResponse.getStatusCode());
    }

    private League generateLeague(String userId) {
        League league = new League();
        league.setId(UUID.randomUUID());
        league.setCreatedBy(userId);
        league.setCreatedAt(System.currentTimeMillis());
        league.setUpdatedAt(System.currentTimeMillis());
        league.setKind(GameType.INDOOR_4X4);
        league.setName(faker.esports().league());
        league.setDivisions(List.of(faker.medical().diseaseName(), faker.medical().diseaseName()));
        return league;
    }
}
