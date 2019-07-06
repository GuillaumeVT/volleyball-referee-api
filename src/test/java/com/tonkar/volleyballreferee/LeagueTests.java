package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class LeagueTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        League league = new League();

        ParameterizedTypeReference<List<LeagueSummary>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<LeagueSummary>> getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getLeaguesResponse.getStatusCode());

        getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getLeaguesResponse.getStatusCode());

        ResponseEntity<League> getLeagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), League.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getLeagueResponse.getStatusCode());

        ResponseEntity<Count> getLeagueCountResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getLeagueCountResponse.getStatusCode());

        ResponseEntity<String> leagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues"), HttpMethod.POST, payloadWithAuth(testUserInvalidToken, league), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, leagueResponse.getStatusCode());

        leagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, leagueResponse.getStatusCode());
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

        ResponseEntity<League> getLeagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/" + leagueId), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), League.class);
        assertEquals(HttpStatus.NOT_FOUND, getLeagueResponse.getStatusCode());

        // Create league

        ResponseEntity<String> leagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues"), HttpMethod.POST, payloadWithAuth(testUserToken1, league), String.class);
        assertEquals(HttpStatus.CREATED, leagueResponse.getStatusCode());

        // League already exists

        leagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues"), HttpMethod.POST, payloadWithAuth(testUserToken1, league), String.class);
        assertEquals(HttpStatus.CONFLICT, leagueResponse.getStatusCode());

        league.setId(UUID.randomUUID());

        leagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues"), HttpMethod.POST, payloadWithAuth(testUserToken1, league), String.class);
        assertEquals(HttpStatus.CONFLICT, leagueResponse.getStatusCode());

        league.setId(leagueId);

        // Count leagues

        ResponseEntity<Count> getLeagueCountResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getLeagueCountResponse.getStatusCode());
        assertEquals(1L, getLeagueCountResponse.getBody().getCount());

        // List all leagues

        ParameterizedTypeReference<List<LeagueSummary>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<LeagueSummary>> getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(1, getLeaguesResponse.getBody().size());

        // List all leagues of kind

        getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/kind/" + GameType.INDOOR_4X4), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(1, getLeaguesResponse.getBody().size());

        getLeaguesResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getLeaguesResponse.getStatusCode());
        assertEquals(0, getLeaguesResponse.getBody().size());

        // Get league

        getLeagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/" + leagueId), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), League.class);
        assertEquals(HttpStatus.OK, getLeagueResponse.getStatusCode());
        assertEquals(league.getName(), getLeagueResponse.getBody().getName());

        // Delete league

        leagueResponse = restTemplate.exchange(urlOf("/api/v3.1/leagues/" + leagueId), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, leagueResponse.getStatusCode());
    }

}
