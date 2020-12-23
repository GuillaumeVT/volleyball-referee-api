package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.LeagueSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class LeagueTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        League league = new League();

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(urlOf("/leagues"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(urlOf("/leagues")).queryParam("kind", GameType.INDOOR);
        errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf("/leagues/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf("/leagues/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf("/leagues"), HttpMethod.POST, payloadWithAuth(testUserInvalidToken, league), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf("/leagues/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void testManageLeague() {
        createUser1();

        UUID leagueId = UUID.randomUUID();

        League league = new League();
        league.setId(leagueId);
        league.setCreatedBy(testUser1.getId());
        league.setCreatedAt(System.currentTimeMillis());
        league.setUpdatedAt(System.currentTimeMillis());
        league.setKind(GameType.INDOOR_4X4);
        league.setName("Test league");
        league.setDivisions(new ArrayList<>());
        league.getDivisions().add("Division 1");
        league.getDivisions().add("Division 2");

        // League does not exist yet

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(urlOf("/leagues/" + leagueId), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());

        // Create league

        ResponseEntity<Void> leagueResponse = restTemplate.exchange(urlOf("/leagues"), HttpMethod.POST, payloadWithAuth(testUserToken1, league), Void.class);
        assertEquals(HttpStatus.CREATED, leagueResponse.getStatusCode());

        // League already exists

        errorResponse = restTemplate.exchange(urlOf("/leagues"), HttpMethod.POST, payloadWithAuth(testUserToken1, league), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        league.setId(UUID.randomUUID());

        errorResponse = restTemplate.exchange(urlOf("/leagues"), HttpMethod.POST, payloadWithAuth(testUserToken1, league), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        league.setId(leagueId);

        // Count leagues

        ResponseEntity<Count> getLeagueCountResponse = restTemplate.exchange(urlOf("/leagues/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getLeagueCountResponse.getStatusCode());
        assertEquals(1L, getLeagueCountResponse.getBody().getCount());

        // List all leagues

        ParameterizedTypeReference<List<LeagueSummary>> listType = new ParameterizedTypeReference<>() {};
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(urlOf("/leagues"));
        ResponseEntity<List<LeagueSummary>> getLeaguesResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(1, getLeaguesResponse.getBody().size());

        // List all leagues of kind

        uriBuilder = UriComponentsBuilder.fromUriString(urlOf("/leagues")).queryParam("kind", GameType.INDOOR_4X4);
        getLeaguesResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(1, getLeaguesResponse.getBody().size());

        uriBuilder = UriComponentsBuilder.fromUriString(urlOf("/leagues")).queryParam("kind", GameType.INDOOR);
        getLeaguesResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(0, getLeaguesResponse.getBody().size());

        uriBuilder = UriComponentsBuilder.fromUriString(urlOf("/leagues")).queryParam("kind", String.join(",", GameType.BEACH.toString(), GameType.INDOOR_4X4.toString()));
        getLeaguesResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(1, getLeaguesResponse.getBody().size());

        // Get league

        ResponseEntity<League> getLeagueResponse = restTemplate.exchange(urlOf("/leagues/" + leagueId), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), League.class);
        assertEquals(HttpStatus.OK, getLeagueResponse.getStatusCode());
        assertEquals(league.getName(), getLeagueResponse.getBody().getName());

        // Delete league

        leagueResponse = restTemplate.exchange(urlOf("/leagues/" + leagueId), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, leagueResponse.getStatusCode());
    }

}
