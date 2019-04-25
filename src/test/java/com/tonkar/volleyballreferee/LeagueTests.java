package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class LeagueTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        League league = new League();

        ParameterizedTypeReference<List<League>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<League>> getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3/leagues"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getLeaguesResponse.getStatusCode());

        getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3/leagues/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getLeaguesResponse.getStatusCode());

        ResponseEntity<League> getLeagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), League.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getLeagueResponse.getStatusCode());

        ResponseEntity<Count> getLeagueCountResponse = restTemplate.exchange(urlOf("/api/v3/leagues/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getLeagueCountResponse.getStatusCode());

        ResponseEntity<String> leagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues"), HttpMethod.POST, payloadWithAuth(testUserInvalidAuth, league), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, leagueResponse.getStatusCode());

        leagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, leagueResponse.getStatusCode());
    }

    @Test
    public void testManageLeague() {
        UUID leagueId = UUID.randomUUID();

        League league = new League();
        league.setId(leagueId);
        league.setCreatedBy(testUser1Id);
        league.setCreatedAt(System.currentTimeMillis());
        league.setUpdatedAt(System.currentTimeMillis());
        league.setKind(GameType.INDOOR_4X4);
        league.setName("Test league");
        league.setDivisions(new ArrayList<>());
        league.getDivisions().add("Division 1");
        league.getDivisions().add("Division 2");

        // League does not exist yet

        ResponseEntity<League> getLeagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues/" + leagueId), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), League.class);
        assertEquals(HttpStatus.NOT_FOUND, getLeagueResponse.getStatusCode());

        // Create league

        ResponseEntity<String> leagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues"), HttpMethod.POST, payloadWithAuth(testUser1Auth, league), String.class);
        assertEquals(HttpStatus.CREATED, leagueResponse.getStatusCode());

        // League already exists

        leagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues"), HttpMethod.POST, payloadWithAuth(testUser1Auth, league), String.class);
        assertEquals(HttpStatus.CONFLICT, leagueResponse.getStatusCode());

        league.setId(UUID.randomUUID());

        leagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues"), HttpMethod.POST, payloadWithAuth(testUser1Auth, league), String.class);
        assertEquals(HttpStatus.CONFLICT, leagueResponse.getStatusCode());

        league.setId(leagueId);

        // Count leagues

        ResponseEntity<Count> getLeagueCountResponse = restTemplate.exchange(urlOf("/api/v3/leagues/count"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Count.class);
        assertEquals(HttpStatus.OK, getLeagueCountResponse.getStatusCode());
        assertEquals(1L, getLeagueCountResponse.getBody().getCount());

        // List all leagues

        ParameterizedTypeReference<List<League>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<League>> getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3/leagues"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(1, getLeaguesResponse.getBody().size());

        // List all leagues of kind

        getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3/leagues/kind/" + GameType.INDOOR_4X4), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(1, getLeaguesResponse.getBody().size());

        getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3/leagues/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(0, getLeaguesResponse.getBody().size());

        // Get league

        getLeagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues/" + leagueId), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), League.class);
        assertEquals(HttpStatus.OK, getLeagueResponse.getStatusCode());
        assertEquals(league.getName(), getLeagueResponse.getBody().getName());

        // Delete league

        leagueResponse = restTemplate.exchange(urlOf("/api/v3/leagues/" + leagueId), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, leagueResponse.getStatusCode());
    }

}
