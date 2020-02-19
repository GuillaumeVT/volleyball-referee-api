package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.GameIngredients;
import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

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
        GameSummary gameSummary = new GameSummary();

        ParameterizedTypeReference<List<GameSummary>> listType = new ParameterizedTypeReference<>() {};
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), pageType);
        assertEquals(HttpStatus.UNAUTHORIZED, pageGameDescrResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("status", GameStatus.COMPLETED)
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), pageType);
        assertEquals(HttpStatus.UNAUTHORIZED, pageGameDescrResponse.getStatusCode());

        ResponseEntity<List<GameSummary>> listGameDescrResponse = restTemplate.exchange(urlOf("/games/available"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), listType);
        assertEquals(HttpStatus.UNAUTHORIZED, listGameDescrResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games/completed"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), pageType);
        assertEquals(HttpStatus.UNAUTHORIZED, pageGameDescrResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games/league/" + UUID.randomUUID()))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), pageType);
        assertEquals(HttpStatus.UNAUTHORIZED, pageGameDescrResponse.getStatusCode());

        ResponseEntity<Game> getGameResponse = restTemplate.exchange(urlOf("/games/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Game.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getGameResponse.getStatusCode());

        ResponseEntity<Count> getGamesCountResponse = restTemplate.exchange(urlOf("/games/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getGamesCountResponse.getStatusCode());

        getGamesCountResponse = restTemplate.exchange(urlOf("/games/league" + UUID.randomUUID() + "/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getGamesCountResponse.getStatusCode());

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/games"), HttpMethod.POST, payloadWithAuth(testUserInvalidToken, gameSummary), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.POST, payloadWithAuth(testUserInvalidToken, game), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games"), HttpMethod.PUT, payloadWithAuth(testUserInvalidToken, gameSummary), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.PUT, payloadWithAuth(testUserInvalidToken, game), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/" + UUID.randomUUID() + "/set/2"), HttpMethod.PATCH, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/" + UUID.randomUUID() + "/indexed/true"), HttpMethod.PATCH, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, gameResponse.getStatusCode());
    }

    @Test
    public void testManageGame_scheduled() {
        createUser1();

        Team team1 = buildBeachTeam("Team 1");
        Team team2 = buildBeachTeam("Team 2");
        Rules rules = buildBeachRules();

        UUID gameId = UUID.randomUUID();

        GameSummary gameSummary = new GameSummary();

        gameSummary.setId(gameId);
        gameSummary.setCreatedBy(testUser1.getId());
        gameSummary.setCreatedAt(System.currentTimeMillis());
        gameSummary.setUpdatedAt(System.currentTimeMillis());
        gameSummary.setScheduledAt(System.currentTimeMillis());
        gameSummary.setRefereedBy(testUser1.getId());
        gameSummary.setRefereeName(testUser1.getPseudo());
        gameSummary.setKind(GameType.BEACH);
        gameSummary.setGender(GenderType.LADIES);
        gameSummary.setUsage(UsageType.NORMAL);
        gameSummary.setStatus(GameStatus.SCHEDULED);
        gameSummary.setIndexed(true);
        gameSummary.setLeagueId(null);
        gameSummary.setLeagueName(null);
        gameSummary.setDivisionName(null);
        gameSummary.setHomeTeamId(team1.getId());
        gameSummary.setHomeTeamName(team1.getName());
        gameSummary.setGuestTeamId(team2.getId());
        gameSummary.setGuestTeamName(team2.getName());
        gameSummary.setHomeSets(0);
        gameSummary.setGuestSets(0);
        gameSummary.setRulesId(rules.getId());
        gameSummary.setRulesName(rules.getName());
        gameSummary.setScore("");
        gameSummary.setReferee1Name("Referee 1");
        gameSummary.setReferee2Name("Referee 2");
        gameSummary.setScorerName("Scorer");

        // Create teams and rules

        ResponseEntity<String> rulesResponse = restTemplate.exchange(urlOf("/rules"), HttpMethod.POST, payloadWithAuth(testUserToken1, rules), String.class);
        assertEquals(HttpStatus.CREATED, rulesResponse.getStatusCode());

        ResponseEntity<String> teamResponse =  restTemplate.exchange(urlOf("/teams"), HttpMethod.POST, payloadWithAuth(testUserToken1, team1), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        teamResponse =  restTemplate.exchange(urlOf("/teams"), HttpMethod.POST, payloadWithAuth(testUserToken1, team2), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        // Game does not exist

        ResponseEntity<Game> getGameResponse = restTemplate.exchange(urlOf("/games/" + gameSummary.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Game.class);
        assertEquals(HttpStatus.NOT_FOUND, getGameResponse.getStatusCode());

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/games"), HttpMethod.PUT, payloadWithAuth(testUserToken1, gameSummary), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        // Create game

        gameResponse = restTemplate.exchange(urlOf("/games"), HttpMethod.POST, payloadWithAuth(testUserToken1, gameSummary), String.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        // Update game

        gameSummary.setScheduledAt(System.currentTimeMillis());

        gameResponse = restTemplate.exchange(urlOf("/games"), HttpMethod.PUT, payloadWithAuth(testUserToken1, gameSummary), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // List all games

        ParameterizedTypeReference<List<GameSummary>> listType = new ParameterizedTypeReference<>() {};
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        // List all games with status

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("status", GameStatus.SCHEDULED)
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("status", GameStatus.LIVE)
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        // List all games with status, kind, gender

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("status", GameStatus.SCHEDULED)
                .queryParam("kind", GameType.INDOOR)
                .queryParam("gender", GenderType.GENTS)
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("status", GameStatus.SCHEDULED)
                .queryParam("kind", GameType.BEACH)
                .queryParam("gender", GenderType.LADIES)
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        // List all available games

        ResponseEntity<List<GameSummary>> listGameDescrResponse = restTemplate.exchange(urlOf("/games/available"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, listGameDescrResponse.getStatusCode());
        assertEquals(1, listGameDescrResponse.getBody().size());

        // Get game

        getGameResponse = restTemplate.exchange(urlOf("/games/" + gameSummary.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Game.class);
        assertEquals(HttpStatus.OK, getGameResponse.getStatusCode());

        // Teams and rules are used by a game

        rulesResponse = restTemplate.exchange(urlOf("/rules/" + rules.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CONFLICT, rulesResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/teams/" + team1.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CONFLICT, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/teams/" + team2.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CONFLICT, teamResponse.getStatusCode());

        // Delete game

        gameResponse = restTemplate.exchange(urlOf("/games/" + gameSummary.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());

        // Delete teams and rules

        rulesResponse = restTemplate.exchange(urlOf("/rules/" + rules.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/teams/" + team1.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());

        teamResponse = restTemplate.exchange(urlOf("/teams/" + team2.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, teamResponse.getStatusCode());
    }

    @Test
    public void testManageGame_notScheduled() {
        createUser1();

        Team team1 = buildBeachTeam("Team 1");
        Team team2 = buildBeachTeam("Team 2");
        Rules rules = buildBeachRules();
        Game.SelectedLeague league = buildBeachLeague("Test league", "Pool A");

        UUID gameId = UUID.randomUUID();
        UUID leagueId = league.getId();

        Game game = new Game();

        game.setId(gameId);
        game.setCreatedBy(testUser1.getId());
        game.setCreatedAt(System.currentTimeMillis());
        game.setUpdatedAt(System.currentTimeMillis());
        game.setScheduledAt(System.currentTimeMillis());
        game.setRefereedBy(testUser1.getId());
        game.setRefereeName(testUser1.getPseudo());
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

        // Game does not exist

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.PUT, payloadWithAuth(testUserToken1, game), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        // Create game

        gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.POST, payloadWithAuth(testUserToken1, game), String.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        // Update set

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId() + "/set/1"), HttpMethod.PATCH, payloadWithAuth(testUserToken1, buildSet()), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Update indexed

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId() + "/indexed/false"), HttpMethod.PATCH, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId() + "/indexed/true"), HttpMethod.PATCH, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Update game

        game.setStatus(GameStatus.COMPLETED);

        gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.PUT, payloadWithAuth(testUserToken1, game), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Get games in league

        ParameterizedTypeReference<List<GameSummary>> listType = new ParameterizedTypeReference<>() {};
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games/league/" + leagueId))
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/league/" + leagueId))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        // Get games in division

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/league/" + leagueId + "/division/" + game.getLeague().getDivision()))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/league/" + leagueId + "/division/somediv"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        // Download excel file for division

        gameResponse = restTemplate.exchange(urlOf("/public/games/league/" + leagueId + "/division/" + game.getLeague().getDivision() + "/excel"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Count games in league

        ResponseEntity<Count> getGameCountResponse = restTemplate.exchange(urlOf("/games/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getGameCountResponse.getStatusCode());
        assertEquals(1L, getGameCountResponse.getBody().getCount());

        getGameCountResponse = restTemplate.exchange(urlOf("/games/league/" + leagueId + "/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getGameCountResponse.getStatusCode());
        assertEquals(1L, getGameCountResponse.getBody().getCount());

        // Delete game

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());
    }

    @Test
    public void testManageGame_refereedBy() {
        createUser1();
        createUser2();

        Team team1 = buildBeachTeam("Team 1");
        Team team2 = buildBeachTeam("Team 2");
        Rules rules = buildBeachRules();

        UUID gameId = UUID.randomUUID();

        GameSummary gameSummary = new GameSummary();

        gameSummary.setId(gameId);
        gameSummary.setCreatedBy(testUser1.getId());
        gameSummary.setCreatedAt(System.currentTimeMillis());
        gameSummary.setUpdatedAt(System.currentTimeMillis());
        gameSummary.setScheduledAt(System.currentTimeMillis());
        gameSummary.setRefereedBy(testUser2.getId());
        gameSummary.setRefereeName(testUser2.getPseudo());
        gameSummary.setKind(GameType.BEACH);
        gameSummary.setGender(GenderType.LADIES);
        gameSummary.setUsage(UsageType.NORMAL);
        gameSummary.setStatus(GameStatus.SCHEDULED);
        gameSummary.setIndexed(true);
        gameSummary.setLeagueId(null);
        gameSummary.setLeagueName(null);
        gameSummary.setDivisionName(null);
        gameSummary.setHomeTeamId(team1.getId());
        gameSummary.setHomeTeamName(team1.getName());
        gameSummary.setGuestTeamId(team2.getId());
        gameSummary.setGuestTeamName(team2.getName());
        gameSummary.setHomeSets(0);
        gameSummary.setGuestSets(0);
        gameSummary.setRulesId(rules.getId());
        gameSummary.setRulesName(rules.getName());
        gameSummary.setScore("");
        gameSummary.setReferee1Name("Referee 1");
        gameSummary.setReferee2Name("Referee 2");
        gameSummary.setScorerName("Scorer");

        // Create teams and rules

        ResponseEntity<String> rulesResponse = restTemplate.exchange(urlOf("/rules"), HttpMethod.POST, payloadWithAuth(testUserToken1, rules), String.class);
        assertEquals(HttpStatus.CREATED, rulesResponse.getStatusCode());

        ResponseEntity<String> teamResponse =  restTemplate.exchange(urlOf("/teams"), HttpMethod.POST, payloadWithAuth(testUserToken1, team1), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        teamResponse =  restTemplate.exchange(urlOf("/teams"), HttpMethod.POST, payloadWithAuth(testUserToken1, team2), String.class);
        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());

        // Can't create game when referee is not friend

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/games"), HttpMethod.POST, payloadWithAuth(testUserToken1, gameSummary), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        // Friends

        ResponseEntity<String> friendResponse = restTemplate.exchange(urlOf(String.format("/users/friends/request/%s", testUser2.getPseudo())), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<FriendRequest>> getFriendsResponse = restTemplate.exchange(urlOf("/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        UUID friendRequestId =  getFriendsResponse.getBody().get(0).getId();

        friendResponse = restTemplate.exchange(urlOf(String.format("/users/friends/accept/%s", friendRequestId)), HttpMethod.POST, emptyPayloadWithAuth(testUserToken2), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        // Get ingredients

        ResponseEntity<GameIngredients> ingredientsResponse = restTemplate.exchange(urlOf(String.format("/games/ingredients/%s", gameSummary.getKind())), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), GameIngredients.class);
        assertEquals(HttpStatus.OK, ingredientsResponse.getStatusCode());

        // Create game

        gameResponse = restTemplate.exchange(urlOf("/games"), HttpMethod.POST, payloadWithAuth(testUserToken1, gameSummary), String.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        // List all available games for creator and referee

        ParameterizedTypeReference<List<GameSummary>> listType = new ParameterizedTypeReference<>() {};

        ResponseEntity<List<GameSummary>> listGameDescrResponse = restTemplate.exchange(urlOf("/games/available"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, listGameDescrResponse.getStatusCode());
        assertEquals(1, listGameDescrResponse.getBody().size());

        listGameDescrResponse = restTemplate.exchange(urlOf("/games/available"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), listType);
        assertEquals(HttpStatus.OK, listGameDescrResponse.getStatusCode());
        assertEquals(1, listGameDescrResponse.getBody().size());

        // Update game by creator

        Game game = new Game();

        game.setId(gameId);
        game.setCreatedBy(testUser1.getId());
        game.setCreatedAt(System.currentTimeMillis());
        game.setUpdatedAt(System.currentTimeMillis());
        game.setScheduledAt(System.currentTimeMillis());
        game.setRefereedBy(testUser2.getId());
        game.setRefereeName(testUser2.getPseudo());
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

        gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.PUT, payloadWithAuth(testUserToken1, game), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Update referee

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId() + "/referee/" + testUser1.getId()), HttpMethod.PATCH, emptyPayloadWithAuth(testUserToken2), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId() + "/referee/" + testUser1.getId()), HttpMethod.PATCH, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        ResponseEntity<Game> getGameResponse = restTemplate.exchange(urlOf("/games/" + gameSummary.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), Game.class);
        assertEquals(HttpStatus.NOT_FOUND, getGameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId() + "/referee/" + testUser2.getId()), HttpMethod.PATCH, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        // Update game by referee

        game.setStatus(GameStatus.COMPLETED);

        gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.PUT, payloadWithAuth(testUserToken2, game), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId() + "/referee/" + testUser1.getId()), HttpMethod.PATCH, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        // List completed games

        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("status", GameStatus.COMPLETED)
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games"))
                .queryParam("status", GameStatus.COMPLETED)
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games/completed"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/games/completed"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        // Referee cannot delete game of creator

        gameResponse = restTemplate.exchange(urlOf("/games/" + game.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken2), String.class);
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());

        getGameResponse = restTemplate.exchange(urlOf("/games/" + gameSummary.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Game.class);
        assertEquals(HttpStatus.OK, getGameResponse.getStatusCode());

        getGameResponse = restTemplate.exchange(urlOf("/games/" + gameSummary.getId()), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), Game.class);
        assertEquals(HttpStatus.OK, getGameResponse.getStatusCode());
    }

    @Test
    public void testManageGame_search() {
        createUser1();

        Team team1 = buildBeachTeam("Team 1");
        Team team2 = buildBeachTeam("Team 2");
        Rules rules = buildBeachRules();
        Game.SelectedLeague league = buildBeachLeague("Test league", "Test division");

        UUID gameId = UUID.randomUUID();

        Game game = new Game();

        game.setId(gameId);
        game.setCreatedBy(testUser1.getId());
        game.setCreatedAt(System.currentTimeMillis());
        game.setUpdatedAt(System.currentTimeMillis());
        game.setScheduledAt(System.currentTimeMillis());
        game.setRefereedBy(testUser1.getId());
        game.setRefereeName(testUser1.getPseudo());
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
        user.setId(testUser1.getId());
        user.setPseudo(testUser1.getPseudo());
        user.setFriends(new ArrayList<>());

        // Create game

        ResponseEntity<String> gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.POST, payloadWithAuth(testUserToken1, game), String.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        // Get game

        ResponseEntity<Game> getGameResponse = restTemplate.exchange(urlOf("/public/games/" + game.getId()), HttpMethod.GET, emptyPayloadWithoutAuth(), Game.class);
        assertEquals(HttpStatus.OK, getGameResponse.getStatusCode());

        // List live games

        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/live"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        // Search not matching

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/notmatching"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/team1"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        // List by team search

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/tea"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/" + game.getHomeTeam().getName()))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        // List by referee search

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/vbr"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/" + game.getRefereeName()))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        // List by league search

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/lea"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        // List by date search

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/date/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(1, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/date/" + LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE)))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/date/" + LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        // Game no longer indexed

        game.setIndexed(false);

        gameResponse = restTemplate.exchange(urlOf("/games/full"), HttpMethod.PUT, payloadWithAuth(testUserToken1, game), String.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/vbr"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/token/lea"))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());

        uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/public/games/date/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
                .queryParam("page", 0)
                .queryParam("size", 20);
        pageGameDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);
        assertEquals(HttpStatus.OK, pageGameDescrResponse.getStatusCode());
        assertEquals(0, pageGameDescrResponse.getBody().getTotalElements());
    }

    private Team buildBeachTeam(String teamName) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setCreatedBy(testUser1.getId());
        team.setCreatedAt(System.currentTimeMillis());
        team.setUpdatedAt(System.currentTimeMillis());
        team.setName(teamName);
        team.setKind(GameType.BEACH);
        team.setGender(GenderType.LADIES);
        team.setColor("#fff");
        team.setLiberoColor("#000");
        team.setPlayers(new ArrayList<>());
        team.getPlayers().add(new Team.Player(1, "me"));
        team.getPlayers().add(new Team.Player(2, "you"));
        team.setLiberos(new ArrayList<>());
        team.setCaptain(1);
        return team;
    }

    private Rules buildBeachRules() {
        return new Rules(UUID.randomUUID(), testUser1.getId(), System.currentTimeMillis(), System.currentTimeMillis(), "Test beach rules", GameType.BEACH,
                3, 21, true, 15, true, true, Rules.WIN_TERMINATION, true, 1, 30,
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

    private Game.SelectedLeague buildBeachLeague(String name, String division) {
        Game.SelectedLeague selectedLeague = new Game.SelectedLeague();
        selectedLeague.setId(UUID.randomUUID());
        selectedLeague.setCreatedBy(testUser1.getId());
        selectedLeague.setCreatedAt(System.currentTimeMillis());
        selectedLeague.setUpdatedAt(System.currentTimeMillis());
        selectedLeague.setName(name);
        selectedLeague.setKind(GameType.BEACH);
        selectedLeague.setDivision(division);
        return selectedLeague;
    }
}
