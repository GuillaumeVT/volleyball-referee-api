package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameDescription;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class GameTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        Game game = new Game();
        GameDescription gameDescription = new GameDescription();

        ParameterizedTypeReference<List<GameDescription>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<GameDescription>> getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getGameDescrResponse.getStatusCode());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/status/" + GameStatus.COMPLETED), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getGameDescrResponse.getStatusCode());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/available"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getGameDescrResponse.getStatusCode());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/completed"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getGameDescrResponse.getStatusCode());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/league/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getGameDescrResponse.getStatusCode());

        ResponseEntity<Game> getGameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Game.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getGameResponse.getStatusCode());

        ResponseEntity<Count> getGamesCountResponse = restTemplate.exchange(urlOf("/api/v3/games/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getGamesCountResponse.getStatusCode());

        getGamesCountResponse = restTemplate.exchange(urlOf("/api/v3/games/league" + UUID.randomUUID() + "/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getGamesCountResponse.getStatusCode());

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.POST, payloadWithAuth(testUserInvalidAuth, gameDescription), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.POST, payloadWithAuth(testUserInvalidAuth, game), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.PUT, payloadWithAuth(testUserInvalidAuth, gameDescription), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.PUT, payloadWithAuth(testUserInvalidAuth, game), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + UUID.randomUUID() + "/set/2"), HttpMethod.PATCH, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + UUID.randomUUID() + "/indexed/true"), HttpMethod.PATCH, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());
    }

    @Test
    public void testManageGame_scheduled() {
        Team team1 = buildBeachTeam("Team 1");
        Team team2 = buildBeachTeam("Team 2");
        Rules rules = buildBeachRules();

        UUID gameId = UUID.randomUUID();

        GameDescription gameDescription = new GameDescription();

        gameDescription.setId(gameId);
        gameDescription.setCreatedBy(testUser1Id);
        gameDescription.setCreatedAt(System.currentTimeMillis());
        gameDescription.setUpdatedAt(System.currentTimeMillis());
        gameDescription.setScheduledAt(System.currentTimeMillis());
        gameDescription.setRefereedBy(testUser1Id);
        gameDescription.setRefereeName("VBR1");
        gameDescription.setKind(GameType.BEACH);
        gameDescription.setGender(GenderType.LADIES);
        gameDescription.setUsage(UsageType.NORMAL);
        gameDescription.setStatus(GameStatus.SCHEDULED);
        gameDescription.setIndexed(true);
        gameDescription.setLeagueId(null);
        gameDescription.setLeagueName(null);
        gameDescription.setDivisionName(null);
        gameDescription.setHomeTeamId(team1.getId());
        gameDescription.setHomeTeamName(team1.getName());
        gameDescription.setGuestTeamId(team2.getId());
        gameDescription.setGuestTeamName(team2.getName());
        gameDescription.setHomeSets(0);
        gameDescription.setGuestSets(0);
        gameDescription.setRulesId(rules.getId());
        gameDescription.setRulesName(rules.getName());
        gameDescription.setScore("");

        User user = new User();
        user.setId(testUser1Id);
        user.setPseudo("VBR1");
        user.setFriends(new ArrayList<>());

        // Create user

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        // Create teams and rules

        ResponseEntity<String> rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.POST, payloadWithAuth(testUser1Auth, rules), String.class);
        assertEquals(HttpStatus.CREATED, rulesResponse.getStatusCode());

        ResponseEntity<String> teamResponse =  restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.POST, payloadWithAuth(testUser1Auth, team1), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        teamResponse =  restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.POST, payloadWithAuth(testUser1Auth, team2), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        // Game does not exist

        ResponseEntity<Game> getGameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + gameDescription.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Game.class);
        assertEquals(HttpStatus.NOT_FOUND, getGameResponse.getStatusCode());

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, gameDescription), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        // Create game

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.POST, payloadWithAuth(testUser1Auth, gameDescription), String.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        // Update game

        gameDescription.setScheduledAt(System.currentTimeMillis());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, gameDescription), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // List all games

        ParameterizedTypeReference<List<GameDescription>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<GameDescription>> getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // List all games with status

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/status/" + GameStatus.SCHEDULED), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/status/" + GameStatus.LIVE), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());

        // List all available games

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/available"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // Get game

        getGameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + gameDescription.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Game.class);
        assertEquals(HttpStatus.OK, getGameResponse.getStatusCode());

        // Teams and rules are used by a game

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/" + rules.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CONFLICT, rulesResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + team1.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CONFLICT, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + team2.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CONFLICT, teamResponse.getStatusCode());

        // Delete game

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + gameDescription.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());

        // Delete teams and rules

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/" + rules.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + team1.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/api/v3/teams/" + team2.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());
    }

    @Test
    public void testManageGame_notScheduled() {
        Team team1 = buildBeachTeam("Team 1");
        Team team2 = buildBeachTeam("Team 2");
        Rules rules = buildBeachRules();
        SelectedLeague league = buildBeachLeague("Test league", "Pool A");

        UUID gameId = UUID.randomUUID();
        UUID leagueId = league.getId();

        Game game = new Game();

        game.setId(gameId);
        game.setCreatedBy(testUser1Id);
        game.setCreatedAt(System.currentTimeMillis());
        game.setUpdatedAt(System.currentTimeMillis());
        game.setScheduledAt(System.currentTimeMillis());
        game.setRefereedBy(testUser1Id);
        game.setRefereeName("VBR1");
        game.setKind(GameType.BEACH);
        game.setGender(GenderType.LADIES);
        game.setUsage(UsageType.NORMAL);
        game.setStatus(GameStatus.LIVE);
        game.setIndexed(true);
        game.setLeague(league);
        game.setHomeTeam(team1);
        game.setGuestTeam(team2);
        game.setHomeSets(0);
        game.setGuestSets(0);
        game.setRules(rules);
        game.setSets(new ArrayList<>());
        game.getSets().add(buildSet());
        game.setHomeCards(new ArrayList<>());
        game.setGuestCards(new ArrayList<>());
        game.setScore("");

        User user = new User();
        user.setId(testUser1Id);
        user.setPseudo("VBR1");
        user.setFriends(new ArrayList<>());

        // Create user

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        // Game does not exist

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, game), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        // Create game

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.POST, payloadWithAuth(testUser1Auth, game), String.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        // Update set

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId() + "/set/1"), HttpMethod.PATCH, payloadWithAuth(testUser1Auth, buildSet()), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Update indexed

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId() + "/indexed/false"), HttpMethod.PATCH, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId() + "/indexed/true"), HttpMethod.PATCH, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Update game

        game.setStatus(GameStatus.COMPLETED);

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, game), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Get games in league

        ParameterizedTypeReference<List<GameDescription>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<GameDescription>> getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/league/" + leagueId), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        typeReference = new ParameterizedTypeReference<>() {};
        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/league/" + leagueId), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // Get games in division

        typeReference = new ParameterizedTypeReference<>() {};
        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/league/" + leagueId + "/division/" + game.getLeague().getDivision()), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        typeReference = new ParameterizedTypeReference<>() {};
        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/league/" + leagueId + "/division/somediv"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());

        // Download excel file for division

        gameResponse = restTemplate.exchange(urlOf("/api/v3/public/games/league/" + leagueId + "/division/" + game.getLeague().getDivision() + "/excel"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Count games in league

        ResponseEntity<Count> getGameCountResponse = restTemplate.exchange(urlOf("/api/v3/games/count"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Count.class);
        assertEquals(HttpStatus.OK, getGameCountResponse.getStatusCode());
        assertEquals(1L, getGameCountResponse.getBody().getCount());

        getGameCountResponse = restTemplate.exchange(urlOf("/api/v3/games/league/" + leagueId + "/count"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Count.class);
        assertEquals(HttpStatus.OK, getGameCountResponse.getStatusCode());
        assertEquals(1L, getGameCountResponse.getBody().getCount());

        // Delete game

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());
    }

    @Test
    public void testManageGame_refereedBy() {
        String pseudo1 = "VBR1";
        String pseudo2 = "VBR2";

        User user1 = new User();
        user1.setId(testUser1Id);
        user1.setPseudo(pseudo1);
        user1.setFriends(new ArrayList<>());

        User user2 = new User();
        user2.setId(testUser2Id);
        user2.setPseudo(pseudo2);
        user2.setFriends(new ArrayList<>());

        Team team1 = buildBeachTeam("Team 1");
        Team team2 = buildBeachTeam("Team 2");
        Rules rules = buildBeachRules();

        UUID gameId = UUID.randomUUID();

        GameDescription gameDescription = new GameDescription();

        gameDescription.setId(gameId);
        gameDescription.setCreatedBy(testUser1Id);
        gameDescription.setCreatedAt(System.currentTimeMillis());
        gameDescription.setUpdatedAt(System.currentTimeMillis());
        gameDescription.setScheduledAt(System.currentTimeMillis());
        gameDescription.setRefereedBy(testUser2Id);
        gameDescription.setRefereeName(pseudo2);
        gameDescription.setKind(GameType.BEACH);
        gameDescription.setGender(GenderType.LADIES);
        gameDescription.setUsage(UsageType.NORMAL);
        gameDescription.setStatus(GameStatus.SCHEDULED);
        gameDescription.setIndexed(true);
        gameDescription.setLeagueId(null);
        gameDescription.setLeagueName(null);
        gameDescription.setDivisionName(null);
        gameDescription.setHomeTeamId(team1.getId());
        gameDescription.setHomeTeamName(team1.getName());
        gameDescription.setGuestTeamId(team2.getId());
        gameDescription.setGuestTeamName(team2.getName());
        gameDescription.setHomeSets(0);
        gameDescription.setGuestSets(0);
        gameDescription.setRulesId(rules.getId());
        gameDescription.setRulesName(rules.getName());
        gameDescription.setScore("");

        // Create users

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user1), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user2), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        // Create teams and rules

        ResponseEntity<String> rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.POST, payloadWithAuth(testUser1Auth, rules), String.class);
        assertEquals(HttpStatus.CREATED, rulesResponse.getStatusCode());

        ResponseEntity<String> teamResponse =  restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.POST, payloadWithAuth(testUser1Auth, team1), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        teamResponse =  restTemplate.exchange(urlOf("/api/v3/teams"), HttpMethod.POST, payloadWithAuth(testUser1Auth, team2), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        // Can't create game when referee is not friend

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.POST, payloadWithAuth(testUser1Auth, gameDescription), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        // Friends

        ResponseEntity<String> friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/request/%s", pseudo2)), HttpMethod.POST, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<FriendRequest>> getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        UUID friendRequestId =  getFriendsResponse.getBody().get(0).getId();

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/accept/%s", friendRequestId)), HttpMethod.POST, emptyPayloadWithAuth(testUser2Auth), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        // Create game

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games"), HttpMethod.POST, payloadWithAuth(testUser1Auth, gameDescription), String.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        // List all available games for creator and referee

        ParameterizedTypeReference<List<GameDescription>> typeReference2 = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<GameDescription>> getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/available"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference2);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/available"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), typeReference2);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // Update game by creator

        Game game = new Game();

        game.setId(gameId);
        game.setCreatedBy(testUser1Id);
        game.setCreatedAt(System.currentTimeMillis());
        game.setUpdatedAt(System.currentTimeMillis());
        game.setScheduledAt(System.currentTimeMillis());
        game.setRefereedBy(testUser2Id);
        game.setRefereeName(pseudo2);
        game.setKind(GameType.BEACH);
        game.setGender(GenderType.LADIES);
        game.setUsage(UsageType.NORMAL);
        game.setStatus(GameStatus.LIVE);
        game.setIndexed(true);
        game.setLeague(null);
        game.setHomeTeam(team1);
        game.setGuestTeam(team2);
        game.setHomeSets(0);
        game.setGuestSets(0);
        game.setRules(rules);
        game.setSets(new ArrayList<>());
        game.getSets().add(buildSet());
        game.setHomeCards(new ArrayList<>());
        game.setGuestCards(new ArrayList<>());
        game.setScore("");

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, game), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Update referee

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId() + "/referee/" + testUser1Id), HttpMethod.PATCH, emptyPayloadWithAuth(testUser2Auth), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId() + "/referee/" + testUser1Id), HttpMethod.PATCH, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        ResponseEntity<Game> getGameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + gameDescription.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), Game.class);
        assertEquals(HttpStatus.NOT_FOUND, getGameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId() + "/referee/" + testUser2Id), HttpMethod.PATCH, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Update game by referee

        game.setStatus(GameStatus.COMPLETED);

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.PUT, payloadWithAuth(testUser2Auth, game), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId() + "/referee/" + testUser1Id), HttpMethod.PATCH, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        // List completed games

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/status/" + GameStatus.COMPLETED), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference2);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/status/" + GameStatus.COMPLETED), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference2);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/completed"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference2);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/games/completed"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference2);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // Referee cannot delete game of creator

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + game.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUser2Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());

        getGameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + gameDescription.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Game.class);
        assertEquals(HttpStatus.OK, getGameResponse.getStatusCode());

        getGameResponse = restTemplate.exchange(urlOf("/api/v3/games/" + gameDescription.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), Game.class);
        assertEquals(HttpStatus.OK, getGameResponse.getStatusCode());
    }

    @Test
    public void testManageGame_search() {
        Team team1 = buildBeachTeam("Team 1");
        Team team2 = buildBeachTeam("Team 2");
        Rules rules = buildBeachRules();
        SelectedLeague league = buildBeachLeague("Test league", "Test division");

        UUID gameId = UUID.randomUUID();
        UUID leagueId = league.getId();

        Game game = new Game();

        game.setId(gameId);
        game.setCreatedBy(testUser1Id);
        game.setCreatedAt(System.currentTimeMillis());
        game.setUpdatedAt(System.currentTimeMillis());
        game.setScheduledAt(System.currentTimeMillis());
        game.setRefereedBy(testUser1Id);
        game.setRefereeName("VBR1");
        game.setKind(GameType.BEACH);
        game.setGender(GenderType.LADIES);
        game.setUsage(UsageType.NORMAL);
        game.setStatus(GameStatus.LIVE);
        game.setIndexed(true);
        game.setLeague(league);
        game.setHomeTeam(team1);
        game.setGuestTeam(team2);
        game.setHomeSets(0);
        game.setGuestSets(0);
        game.setRules(rules);
        game.setSets(new ArrayList<>());
        game.getSets().add(buildSet());
        game.setHomeCards(new ArrayList<>());
        game.setGuestCards(new ArrayList<>());
        game.setScore("");

        User user = new User();
        user.setId(testUser1Id);
        user.setPseudo("VBR1");
        user.setFriends(new ArrayList<>());

        // Create user

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        // Create game

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.POST, payloadWithAuth(testUser1Auth, game), String.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        // Get game

        ResponseEntity<Game> getGameResponse = restTemplate.exchange(urlOf("/api/v3/public/games/" + game.getId()), HttpMethod.GET, emptyPayloadWithoutAuth(), Game.class);
        assertEquals(HttpStatus.OK, getGameResponse.getStatusCode());

        // List live games

        ParameterizedTypeReference<List<GameDescription>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<GameDescription>> getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/live"), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // Search not matching

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/notmatching"), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/team1"), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());

        // List by team search

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/tea"), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/" + game.getHomeTeam().getName()), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // List by referee search

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/vbr"), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/" + game.getRefereeName()), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // List by league search

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/lea"), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        // List by date search

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/date/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE)), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(1, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/date/" + LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE)), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/date/" + LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());

        // Game no longer indexed

        game.setIndexed(false);

        gameResponse = restTemplate.exchange(urlOf("/api/v3/games/full"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, game), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/vbr"), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/token/lea"), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());

        getGameDescrResponse = restTemplate.exchange(urlOf("/api/v3/public/games/date/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE)), HttpMethod.GET, emptyPayloadWithoutAuth(), typeReference);
        assertEquals(HttpStatus.OK, getGameDescrResponse.getStatusCode());
        assertEquals(0, getGameDescrResponse.getBody().size());
    }

    private Team buildBeachTeam(String teamName) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setCreatedBy(testUser1Id);
        team.setCreatedAt(System.currentTimeMillis());
        team.setUpdatedAt(System.currentTimeMillis());
        team.setName(teamName);
        team.setKind(GameType.BEACH);
        team.setGender(GenderType.LADIES);
        team.setColor("#fff");
        team.setLiberoColor("#000");
        team.setPlayers(new ArrayList<>());
        team.getPlayers().add(new Player(1, "me"));
        team.getPlayers().add(new Player(2, "you"));
        team.setLiberos(new ArrayList<>());
        team.setCaptain(1);
        return team;
    }

    private Rules buildBeachRules() {
        return new Rules(UUID.randomUUID(), testUser1Id, System.currentTimeMillis(), System.currentTimeMillis(), "Test beach rules", GameType.BEACH,
                3, 21, true, 15, true, true, true, 1, 30,
                true, 30, true, 60,
                Rules.FIVB_LIMITATION, 0, true, 7, 5, 9999);
    }

    private Set buildSet() {
        Set set = new Set();

        set.setLadder(new ArrayList<>());
        set.setServing("H");
        set.setFirstServing("H");
        set.setHomeSubstitutions(new ArrayList<>());
        set.setGuestSubstitutions(new ArrayList<>());
        set.setHomeCalledTimeouts(new ArrayList<>());
        set.setGuestCalledTimeouts(new ArrayList<>());

        return set;
    }

    private SelectedLeague buildBeachLeague(String name, String division) {
        SelectedLeague selectedLeague = new SelectedLeague();
        selectedLeague.setId(UUID.randomUUID());
        selectedLeague.setCreatedBy(testUser1Id);
        selectedLeague.setCreatedAt(System.currentTimeMillis());
        selectedLeague.setUpdatedAt(System.currentTimeMillis());
        selectedLeague.setName(name);
        selectedLeague.setKind(GameType.BEACH);
        selectedLeague.setDivision(division);
        return selectedLeague;
    }
}
