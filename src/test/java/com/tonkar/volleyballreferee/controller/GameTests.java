package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.GameService;
import com.tonkar.volleyballreferee.service.RulesService;
import com.tonkar.volleyballreferee.service.TeamService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameTests extends VbrMockedTests {

    private final RulesService rulesService;
    private final TeamService teamService;
    private final GameService gameService;

    public GameTests(@Autowired RulesService rulesService, @Autowired TeamService teamService, @Autowired GameService gameService) {
        this.rulesService = rulesService;
        this.teamService = teamService;
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
        UserToken userToken = createUser();
        createScheduledBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};

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
        UserToken userToken = createUser();
        createScheduledBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
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
        UserToken userToken = createUser();
        createScheduledBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<TestPageImpl<GameSummary>> pageType = new ParameterizedTypeReference<>() {};
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
        UserToken userToken = createUser();
        createScheduledBeachGame(userToken.getUser().getId());
        ParameterizedTypeReference<List<GameSummary>> listType = new ParameterizedTypeReference<>() {};

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
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));
        GameSummary gameSummary = generateScheduledBeachGame(userToken.getUser().getId(), true);
        gameSummary.setRefereedBy(userToken2.getUser().getId());
        gameService.createGame(getUser(userToken.getUser().getId()), gameSummary);

        // WHEN
        ResponseEntity<List<GameSummary>> gameResponse = restTemplate.exchange("/games/available", HttpMethod.GET, emptyPayloadWithAuth(userToken2.getToken()), listType);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(gameResponse.getBody()).size());
    }

    @Test
    public void test_games_list_completed() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = generateBeachGame(userToken.getUser().getId());
        game.setStatus(GameStatus.COMPLETED);
        gameService.createGame(getUser(userToken.getUser().getId()), game);
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
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Game> gameResponse = restTemplate.exchange("/games/" + gameSummary.getId(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Game.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_get_notFound() {
        // GIVEN
        UserToken userToken = createUser();
        UUID unknownGameId = UUID.randomUUID();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/games/" + unknownGameId, HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    public void test_games_public_list_token() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = createBeachGame(userToken.getUser().getId());
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
        UserToken userToken = createUser();
        createBeachGame(userToken.getUser().getId());
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
        UserToken userToken = createUser();
        Game game = generateBeachGame(userToken.getUser().getId());
        game.setStatus(GameStatus.LIVE);
        gameService.createGame(getUser(userToken.getUser().getId()), game);
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
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

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
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<GameIngredients> gameResponse = restTemplate.exchange(String.format("/games/ingredients/%s", gameSummary.getKind()), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), GameIngredients.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_create() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = generateBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/full", HttpMethod.POST, payloadWithAuth(userToken.getToken(), game), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_update() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = createBeachGame(userToken.getUser().getId());
        game.setStatus(GameStatus.COMPLETED);

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/full", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), game), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_update_notFound() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = generateBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/full", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), game), Void.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_set_update() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = createBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + game.getId() + "/set/1", HttpMethod.PATCH, payloadWithAuth(userToken.getToken(), generateSet()), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_index_no() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = createBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + game.getId() + "/indexed/false", HttpMethod.PATCH, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_index_yes() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = createBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + game.getId() + "/indexed/true", HttpMethod.PATCH, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_update_refereedByFriend() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));
        Game game = createBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + game.getId() + "/referee/" + userToken2.getUser().getId(), HttpMethod.PATCH, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_schedule_create() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = generateScheduledBeachGame(userToken.getUser().getId(), true);

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games", HttpMethod.POST, payloadWithAuth(userToken.getToken(), gameSummary), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_schedule_update() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), gameSummary), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_schedule_update_notFound() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = generateScheduledBeachGame(userToken.getUser().getId(), false);

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/games", HttpMethod.PUT, payloadWithAuth(userToken.getToken(), gameSummary), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    public void test_games_delete() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games/" + gameSummary.getId(), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_deleteAll() {
        // GIVEN
        UserToken userToken = createUser();
        createScheduledBeachGame(userToken.getUser().getId());
        createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Void> gameResponse = restTemplate.exchange("/games", HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, gameResponse.getStatusCode());
    }

    @Test
    public void test_games_count() {
        // GIVEN
        UserToken userToken = createUser();
        createScheduledBeachGame(userToken.getUser().getId());
        createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Count> gameResponse = restTemplate.exchange("/games/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(2L, Objects.requireNonNull(gameResponse.getBody()).getCount());
    }

    @Test
    public void test_games_count_inLeague() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());
        createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<Count> gameResponse = restTemplate.exchange("/games/league/" + gameSummary.getLeagueId() + "/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
        assertEquals(1L, Objects.requireNonNull(gameResponse.getBody()).getCount());
    }

    @Test
    public void test_games_inLeague() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());
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
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());
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
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());
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
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());
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
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        ResponseEntity<ByteArrayResource> gameResponse = restTemplate.exchange("/public/games/league/" + gameSummary.getLeagueId() + "/division/" + gameSummary.getDivisionName() + "/excel", HttpMethod.GET, emptyPayloadWithoutAuth(), ByteArrayResource.class);

        // THEN
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());
    }

    private GameSummary createScheduledBeachGame(String userId) {
        GameSummary gameSummary = generateScheduledBeachGame(userId, true);
        gameService.createGame(getUser(userId), gameSummary);
        return gameSummary;
    }

    private GameSummary generateScheduledBeachGame(String userId, boolean createRequiredData) {
        var gameSummary = new GameSummary();
        var team1 = generateBeachTeam(userId);
        var team2 = generateBeachTeam(userId);
        var rules = generateBeachRules(userId);
        var league = generateBeachLeague(userId);

        if (createRequiredData) {
            var user = getUser(userId);
            rulesService.createRules(user, rules);
            teamService.createTeam(user, team1);
            teamService.createTeam(user, team2);
        }

        gameSummary.setId(UUID.randomUUID());
        gameSummary.setCreatedBy(userId);
        gameSummary.setCreatedAt(System.currentTimeMillis());
        gameSummary.setUpdatedAt(System.currentTimeMillis());
        gameSummary.setScheduledAt(System.currentTimeMillis());
        gameSummary.setRefereedBy(userId);
        gameSummary.setRefereeName(getUser(userId).getPseudo());
        gameSummary.setKind(GameType.BEACH);
        gameSummary.setGender(GenderType.LADIES);
        gameSummary.setUsage(UsageType.NORMAL);
        gameSummary.setStatus(GameStatus.SCHEDULED);
        gameSummary.setIndexed(true);
        gameSummary.setLeagueId(league.getId());
        gameSummary.setLeagueName(league.getName());
        gameSummary.setDivisionName(league.getDivision());
        gameSummary.setHomeTeamId(team1.getId());
        gameSummary.setHomeTeamName(team1.getName());
        gameSummary.setGuestTeamId(team2.getId());
        gameSummary.setGuestTeamName(team2.getName());
        gameSummary.setHomeSets(0);
        gameSummary.setGuestSets(0);
        gameSummary.setRulesId(rules.getId());
        gameSummary.setRulesName(rules.getName());
        gameSummary.setScore("");
        gameSummary.setReferee1Name(faker.name().fullName());
        gameSummary.setReferee2Name(faker.name().fullName());
        gameSummary.setScorerName(faker.name().fullName());

        return gameSummary;
    }

    private Game createBeachGame(String userId) {
        Game game = generateBeachGame(userId);
        gameService.createGame(getUser(userId), game);
        return game;
    }

    private Game generateBeachGame(String userId) {
        var game = new Game();
        var team1 = generateBeachTeam(userId);
        var team2 = generateBeachTeam(userId);
        var rules = generateBeachRules(userId);
        var league = generateBeachLeague(userId);

        game.setId(UUID.randomUUID());
        game.setCreatedBy(userId);
        game.setCreatedAt(System.currentTimeMillis());
        game.setUpdatedAt(System.currentTimeMillis());
        game.setScheduledAt(System.currentTimeMillis());
        game.setRefereedBy(userId);
        game.setRefereeName(getUser(userId).getPseudo());
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
        game.getSets().add(generateSet());
        game.setHomeCards(new ArrayList<>());
        game.setGuestCards(new ArrayList<>());
        game.setScore("");

        return game;
    }

    private Team generateBeachTeam(String userId) {
        var team = new Team();
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

    private Rules generateBeachRules(String userId) {
        return new Rules(UUID.randomUUID(), userId, 0L, 0L, faker.company().buzzword(), GameType.BEACH,
                3, 21, true, 15, true, true, Rules.WIN_TERMINATION, true, 1, 30,
                true, 30, true, 60,
                Rules.FIVB_LIMITATION, 0, true, 7, 5, 9999);
    }

    private Game.SelectedLeague generateBeachLeague(String userId) {
        Game.SelectedLeague selectedLeague = new Game.SelectedLeague();
        selectedLeague.setId(UUID.randomUUID());
        selectedLeague.setCreatedBy(userId);
        selectedLeague.setCreatedAt(System.currentTimeMillis());
        selectedLeague.setUpdatedAt(System.currentTimeMillis());
        selectedLeague.setName(faker.country().name());
        selectedLeague.setKind(GameType.BEACH);
        selectedLeague.setDivision(faker.country().capital());
        return selectedLeague;
    }

    private Set generateSet() {
        var set = new Set();

        set.setLadder(new ArrayList<>());
        set.setServing("H");
        set.setFirstServing("H");
        set.setHomeSubstitutions(new ArrayList<>());
        set.setGuestSubstitutions(new ArrayList<>());
        set.setHomeCalledTimeouts(new ArrayList<>());
        set.setGuestCalledTimeouts(new ArrayList<>());

        return set;
    }
}
