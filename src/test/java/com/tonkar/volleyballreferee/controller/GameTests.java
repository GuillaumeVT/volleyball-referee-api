package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import com.tonkar.volleyballreferee.service.GameService;
import com.tonkar.volleyballreferee.util.TestPageImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameTests extends VbrMockedTests {

    private final GameService gameService;

    public GameTests(@Autowired GameService gameService) {
        this.gameService = gameService;
    }

    @Test
    public void test_games_unauthorized() {
        final var invalidToken = "invalid";

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/games")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder
                .fromUriString("/games")
                .queryParam("status", GameStatus.COMPLETED)
                .queryParam("page", 0)
                .queryParam("size", 20);
        errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/available", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder
                .fromUriString("/games/completed")
                .queryParam("page", 0)
                .queryParam("size", 20);
        errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder
                .fromUriString("/games/league/" + UUID.randomUUID())
                .queryParam("page", 0)
                .queryParam("size", 20);
        errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/count", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/league" + UUID.randomUUID() + "/count", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games", HttpMethod.POST, payloadWithAuth(invalidToken, new GameSummary()), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/full", HttpMethod.POST, payloadWithAuth(invalidToken, new Game()), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games", HttpMethod.PUT, payloadWithAuth(invalidToken, new GameSummary()), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/full", HttpMethod.PUT, payloadWithAuth(invalidToken, new Game()), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/" + UUID.randomUUID() + "/set/2", HttpMethod.PATCH, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/" + UUID.randomUUID() + "/indexed/true", HttpMethod.PATCH, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games/" + UUID.randomUUID(), HttpMethod.DELETE, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/games", HttpMethod.DELETE, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void test_games_list() {
        // GIVEN
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/games")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_list_byStatus() {
        // GIVEN
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.getUser().getId());
        GameStatus status = GameStatus.SCHEDULED;

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/games")
                .queryParam("status", status)
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());

        // GIVEN
        status = GameStatus.LIVE;

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/games")
                .queryParam("status", status)
                .queryParam("page", 0)
                .queryParam("size", 20);
        gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(0, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_list_byStatusAndKindAndGender() {
        // GIVEN
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.getUser().getId());
        GameStatus status = GameStatus.SCHEDULED;
        GameType kind = GameType.INDOOR;
        GenderType gender = GenderType.GENTS;

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/games")
                .queryParam("status", status)
                .queryParam("kind", kind)
                .queryParam("gender", gender)
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(0, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());

        // GIVEN
        kind = GameType.BEACH;
        gender = GenderType.LADIES;

        // WHEN
        uriBuilder = UriComponentsBuilder
                .fromUriString("/games")
                .queryParam("status", status)
                .queryParam("kind", kind)
                .queryParam("gender", gender)
                .queryParam("page", 0)
                .queryParam("size", 20);
        gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_list_available() {
        // GIVEN
        ParameterizedTypeReference<List<GameSummary>> listType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<List<GameSummary>> gameResponse = restTemplate.exchange("/games/available", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), listType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).size());
    }

    @Test
    public void test_games_list_available_refereedByFriend() {
        // GIVEN
        ParameterizedTypeReference<List<GameSummary>> listType = new ParameterizedTypeReference<>() {};
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.getUser().getId()), sandbox.getUser(userToken2.getUser().getId()));
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.getUser().getId(), true);
        gameSummary.setRefereedBy(userToken2.getUser().getId());
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), gameSummary);

        // WHEN
        ResponseEntity<List<GameSummary>> gameResponse = restTemplate.exchange("/games/available", HttpMethod.GET, emptyPayloadWithAuth(userToken2.getToken()), listType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).size());
    }

    @Test
    public void test_games_list_completed() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());
        game.setStatus(GameStatus.COMPLETED);
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), game);
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/games/completed")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Game> gameResponse = restTemplate.exchange("/games/" + gameSummary.getId(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Game.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_get_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UUID unknownGameId = UUID.randomUUID();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/games/" + unknownGameId, HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    public void test_games_public_list_token() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
        String token = game.getGuestTeam().getName().substring(0, 4);

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/public/games/token/" + token)
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_public_list_date() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/public/games/date/" + date)
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_public_list_live() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());
        game.setStatus(GameStatus.LIVE);
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), game);
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/public/games/live")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_public_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Game> gameResponse = restTemplate.exchange("/public/games/" + gameSummary.getId(), HttpMethod.GET, emptyPayloadWithoutAuth(), Game.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_public_get_notFound() {
        // GIVEN
        UUID unknownGameId = UUID.randomUUID();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/public/games/" + unknownGameId, HttpMethod.GET, emptyPayloadWithoutAuth(), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    public void test_games_get_ingredients() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<GameIngredients> gameResponse = restTemplate.exchange(String.format("/games/ingredients/%s", gameSummary.getKind()), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), GameIngredients.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/full", HttpMethod.POST, payloadWithAuth(userToken.getToken(), game), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.getUser().getId());
        game.setStatus(GameStatus.COMPLETED);

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/full", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), game), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_update_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/full", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), game), Void.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_set_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + game.getId() + "/set/1", HttpMethod.PATCH, payloadWithAuth(userToken.getToken(), sandbox.generateSet()), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_index_no() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + game.getId() + "/indexed/false", HttpMethod.PATCH, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_index_yes() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + game.getId() + "/indexed/true", HttpMethod.PATCH, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_update_refereedByFriend() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.getUser().getId()), sandbox.getUser(userToken2.getUser().getId()));
        Game game = sandbox.createBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + game.getId() + "/referee/" + userToken2.getUser().getId(), HttpMethod.PATCH, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_schedule_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.getUser().getId(), true);

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games", HttpMethod.POST, payloadWithAuth(userToken.getToken(), gameSummary), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_schedule_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), gameSummary), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_schedule_update_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.getUser().getId(), false);

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/games", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), gameSummary), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    public void test_games_delete() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + gameSummary.getId(), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_deleteAll() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.getUser().getId());
        sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games", HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_count() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.getUser().getId());
        sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Count> gameResponse = restTemplate.exchange("/games/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(2L, Objects.requireNonNull(gameResponse.getBody()).getCount());
    }

    @Test
    public void test_games_count_inLeague() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());
        sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Count> gameResponse = restTemplate.exchange("/games/league/" + gameSummary.getLeagueId() + "/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1L, Objects.requireNonNull(gameResponse.getBody()).getCount());
    }

    @Test
    public void test_games_inLeague() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/games/league/" + gameSummary.getLeagueId())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_public_inLeague() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/public/games/league/" + gameSummary.getLeagueId())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_public_inDivision() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/public/games/league/" + gameSummary.getLeagueId() + "/division/" + gameSummary.getDivisionName())
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_public_inDivision2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
        String unknownDivision = "unknownDivision";

        // WHEN
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/public/games/league/" + gameSummary.getLeagueId() + "/division/" + unknownDivision)
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<GameSummary>> gameResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithoutAuth(), pageType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(0, Objects.requireNonNull(gameResponse.getBody()).getTotalElements());
    }

    @Test
    public void test_games_public_downloadDivision() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<ByteArrayResource> gameResponse = restTemplate.exchange("/public/games/league/" + gameSummary.getLeagueId() + "/division/" + gameSummary.getDivisionName() + "/excel", HttpMethod.GET, emptyPayloadWithoutAuth(), ByteArrayResource.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }
}
