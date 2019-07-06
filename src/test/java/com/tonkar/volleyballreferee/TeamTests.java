package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamSummary;
import com.tonkar.volleyballreferee.entity.*;
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
public class TeamTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        Team team = new Team();

        ParameterizedTypeReference<List<TeamSummary>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<TeamSummary>> getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getTeamDescrResponse.getStatusCode());

        getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getTeamDescrResponse.getStatusCode());

        ResponseEntity<Team> getTeamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Team.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getTeamResponse.getStatusCode());

        ResponseEntity<Count> getTeamCountResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getTeamCountResponse.getStatusCode());

        ResponseEntity<String> teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.POST, payloadWithAuth(testUserInvalidToken, team), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.PUT, payloadWithAuth(testUserInvalidToken, team), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, teamResponse.getStatusCode());
    }

    @Test
    public void testManageTeam() {
        createUser1();

        UUID teamId = UUID.randomUUID();

        Team team = new Team();
        team.setId(teamId);
        team.setCreatedBy(testUser1.getId());
        team.setCreatedAt(System.currentTimeMillis());
        team.setUpdatedAt(System.currentTimeMillis());
        team.setName("Test team");
        team.setKind(GameType.BEACH);
        team.setGender(GenderType.LADIES);
        team.setColor("#fff");
        team.setLiberoColor("#000");
        team.setPlayers(new ArrayList<>());
        team.getPlayers().add(new Team.Player(1, "me"));
        team.getPlayers().add(new Team.Player(2, "you"));
        team.setLiberos(new ArrayList<>());
        team.setCaptain(1);

        // Team does not exist yet

        ResponseEntity<Team> getTeamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/" + teamId), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Team.class);
        assertEquals(HttpStatus.NOT_FOUND, getTeamResponse.getStatusCode());

        ResponseEntity<String> teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.PUT, payloadWithAuth(testUserToken1, team), String.class);
        assertEquals(HttpStatus.NOT_FOUND, teamResponse.getStatusCode());

        // Create team

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.POST, payloadWithAuth(testUserToken1, team), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        // Team already exists

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.POST, payloadWithAuth(testUserToken1, team), String.class);
        assertEquals(HttpStatus.CONFLICT, teamResponse.getStatusCode());


        team.setId(UUID.randomUUID());

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.POST, payloadWithAuth(testUserToken1, team), String.class);
        assertEquals(HttpStatus.CONFLICT, teamResponse.getStatusCode());

        team.setId(teamId);

        // Update team

        team.setUpdatedAt(System.currentTimeMillis());
        team.setColor("#123456");

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.PUT, payloadWithAuth(testUserToken1, team), String.class);
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());

        // Count teams

        ResponseEntity<Count> getTeamCountResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getTeamCountResponse.getStatusCode());
        assertEquals(1L, getTeamCountResponse.getBody().getCount());

        // Get team

        getTeamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/" + teamId), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Team.class);
        assertEquals(HttpStatus.OK, getTeamResponse.getStatusCode());
        assertEquals(team.getName(), getTeamResponse.getBody().getName());

        // List all teams

        ParameterizedTypeReference<List<TeamSummary>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<TeamSummary>> getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getTeamDescrResponse.getStatusCode());
        assertEquals(1, getTeamDescrResponse.getBody().size());

        // List all team of kind

        getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/kind/" + GameType.BEACH), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getTeamDescrResponse.getStatusCode());
        assertEquals(1, getTeamDescrResponse.getBody().size());

        getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/kind/" + GameType.INDOOR_4X4), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getTeamDescrResponse.getStatusCode());
        assertEquals(0, getTeamDescrResponse.getBody().size());

        // Delete team

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams/" + teamId), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());

        getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/teams"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getTeamDescrResponse.getStatusCode());
        assertEquals(0, getTeamDescrResponse.getBody().size());
    }
}
