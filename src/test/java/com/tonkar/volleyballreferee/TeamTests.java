package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.TeamDescription;
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

        ParameterizedTypeReference<List<TeamDescription>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<TeamDescription>> getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getTeamDescrResponse.getStatusCode());

        getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3/teams/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getTeamDescrResponse.getStatusCode());

        ResponseEntity<Team> getTeamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Team.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getTeamResponse.getStatusCode());

        ResponseEntity<Count> getTeamCountResponse = restTemplate.exchange(urlOf("/api/v3/teams/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getTeamCountResponse.getStatusCode());

        ResponseEntity<String> teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.POST, payloadWithAuth(testUserInvalidAuth, team), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.PUT, payloadWithAuth(testUserInvalidAuth, team), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, teamResponse.getStatusCode());
    }

    @Test
    public void testManageTeam() {
        UUID teamId = UUID.randomUUID();

        Team team = new Team();
        team.setId(teamId);
        team.setCreatedBy(testUser1Id);
        team.setCreatedAt(System.currentTimeMillis());
        team.setUpdatedAt(System.currentTimeMillis());
        team.setName("Test team");
        team.setKind(GameType.BEACH);
        team.setGender(GenderType.LADIES);
        team.setColor("#fff");
        team.setLiberoColor("#000");
        team.setPlayers(new ArrayList<>());
        team.getPlayers().add(new Player(1, "me"));
        team.getPlayers().add(new Player(2, "you"));
        team.setLiberos(new ArrayList<>());
        team.setCaptain(1);

        User user = new User();
        user.setId(testUser1Id);
        user.setPseudo("VBR1");
        user.setFriends(new ArrayList<>());

        // Create user

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        // Team does not exist yet

        ResponseEntity<Team> getTeamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + teamId), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Team.class);
        assertEquals(HttpStatus.NOT_FOUND, getTeamResponse.getStatusCode());

        ResponseEntity<String> teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, team), String.class);
        assertEquals(HttpStatus.NOT_FOUND, teamResponse.getStatusCode());

        // Create team

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.POST, payloadWithAuth(testUser1Auth, team), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        // Team already exists

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.POST, payloadWithAuth(testUser1Auth, team), String.class);
        assertEquals(HttpStatus.CONFLICT, teamResponse.getStatusCode());


        team.setId(UUID.randomUUID());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.POST, payloadWithAuth(testUser1Auth, team), String.class);
        assertEquals(HttpStatus.CONFLICT, teamResponse.getStatusCode());

        team.setId(teamId);

        // Update team

        team.setUpdatedAt(System.currentTimeMillis());
        team.setColor("#123456");

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, team), String.class);
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());

        // Count teams

        ResponseEntity<Count> getTeamCountResponse = restTemplate.exchange(urlOf("/api/v3/teams/count"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Count.class);
        assertEquals(HttpStatus.OK, getTeamCountResponse.getStatusCode());
        assertEquals(1L, getTeamCountResponse.getBody().getCount());

        // Get team

        getTeamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + teamId), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Team.class);
        assertEquals(HttpStatus.OK, getTeamResponse.getStatusCode());
        assertEquals(team.getName(), getTeamResponse.getBody().getName());

        // List all teams

        ParameterizedTypeReference<List<TeamDescription>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<TeamDescription>> getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getTeamDescrResponse.getStatusCode());
        assertEquals(1, getTeamDescrResponse.getBody().size());

        // List all team of kind

        getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3/teams/kind/" + GameType.BEACH), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getTeamDescrResponse.getStatusCode());
        assertEquals(1, getTeamDescrResponse.getBody().size());

        getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3/teams/kind/" + GameType.INDOOR_4X4), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getTeamDescrResponse.getStatusCode());
        assertEquals(0, getTeamDescrResponse.getBody().size());

        // Delete team

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + teamId), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());

        getTeamDescrResponse = restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getTeamDescrResponse.getStatusCode());
        assertEquals(0, getTeamDescrResponse.getBody().size());
    }
}
