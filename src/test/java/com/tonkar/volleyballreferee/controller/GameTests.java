package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.GameService;
import com.tonkar.volleyballreferee.util.TestPageImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTests extends VbrMockedTests {

    private final GameService gameService;

    public GameTests(@Autowired GameService gameService) {
        super();
        this.gameService = gameService;
    }

    @Test
    void test_games_unauthorized() {
        final var invalidToken = "invalid";

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/games").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("status", GameStatus.COMPLETED)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/games/available")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/games/completed").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/league/%s".formatted(UUID.randomUUID()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/games/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/games/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri("/games/league/%s/count".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .post()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(new GameSummary())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .post()
                .uri("/games/full")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(new Game())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .put()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(new GameSummary())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .put()
                .uri("/games/full")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .bodyValue(new Game())
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .patch()
                .uri("/games/%s/set/2".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .patch()
                .uri("/games/%s/indexed/true".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .delete()
                .uri("/games/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .delete()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_games_list() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/games").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_list_byStatus() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("status", GameStatus.SCHEDULED)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("status", GameStatus.LIVE)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(0, page.getTotalElements()));
    }

    @Test
    void test_games_list_byStatusAndKindAndGender() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("status", GameStatus.SCHEDULED)
                        .queryParam("kind", GameType.INDOOR)
                        .queryParam("gender", GenderType.GENTS)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(0, page.getTotalElements()));

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("status", GameStatus.SCHEDULED)
                        .queryParam("kind", GameType.BEACH)
                        .queryParam("gender", GenderType.LADIES)
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_list_available() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/games/available")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(GameSummary.class)
                .hasSize(1);
    }

    @Test
    void test_games_list_available_refereedByFriend() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.user().id(), true);
        gameSummary.setRefereedBy(userToken2.user().id());
        gameService.createGame(sandbox.getUser(userToken.user().id()), gameSummary);

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/games/available")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken2.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(GameSummary.class)
                .hasSize(1);
    }

    @Test
    void test_games_list_completed() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.user().id());
        game.setStatus(GameStatus.COMPLETED);
        gameService.createGame(sandbox.getUser(userToken.user().id()), game);

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/games/completed").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/games/%s".formatted(gameSummary.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Game.class)
                .value(game -> assertEquals(gameSummary.getId(), game.getId()));
    }

    @Test
    void test_games_get_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UUID unknownGameId = UUID.randomUUID();

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/games/%s".formatted(unknownGameId))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_games_public_list_token() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.user().id());
        String token = game.getGuestTeam().getName().substring(0, 4);

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/token/%s".formatted(token))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_public_list_date() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachGame(userToken.user().id());
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/date/%s".formatted(date))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_public_list_live() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.user().id());
        game.setStatus(GameStatus.LIVE);
        gameService.createGame(sandbox.getUser(userToken.user().id()), game);

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/public/games/live").queryParam("page", 0).queryParam("size", 20).build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_public_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/public/games/%s".formatted(gameSummary.getId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Game.class)
                .value(game -> assertEquals(gameSummary.getId(), game.getId()));
    }

    @Test
    void test_games_public_get_notFound() {
        // GIVEN
        UUID unknownGameId = UUID.randomUUID();

        // WHEN / THEN
        webTestClient.get().uri("/public/games/%s".formatted(unknownGameId)).exchange().expectStatus().isNotFound();
    }

    @Test
    void test_games_get_ingredients() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/games/ingredients/%s".formatted(gameSummary.getKind()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(GameIngredients.class)
                .value(gameIngredients -> assertNotNull(gameIngredients.defaultRules()));
    }

    @Test
    void test_games_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/games/full")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(game)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void test_games_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.user().id());
        game.setStatus(GameStatus.COMPLETED);

        // WHEN / THEN
        webTestClient
                .put()
                .uri("/games/full")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(game)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_games_update_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .put()
                .uri("/games/full")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(game)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_games_set_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/games/%s/set/1".formatted(game.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(sandbox.generateSet())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_games_index_no() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/games/%s/indexed/false".formatted(game.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_games_index_yes() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/games/%s/indexed/true".formatted(game.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_games_update_refereedByFriend() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));
        Game game = sandbox.createBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/games/%s/referee/%s".formatted(game.getId(), userToken2.user().id()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_games_schedule_create() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.user().id(), true);

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(gameSummary)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void test_games_schedule_update() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .put()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(gameSummary)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_games_schedule_update_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.user().id(), false);

        // WHEN / THEN
        webTestClient
                .put()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(gameSummary)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_games_delete() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .delete()
                .uri("/games/%s".formatted(gameSummary.getId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void test_games_deleteAll() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.user().id());
        sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .delete()
                .uri("/games")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void test_games_count() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.user().id());
        sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/games/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Count.class)
                .value(count -> assertEquals(2L, count.count()));
    }

    @Test
    void test_games_count_inLeague() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());
        sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/games/league/%s/count".formatted(gameSummary.getLeagueId()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Count.class)
                .value(count -> assertEquals(1L, count.count()));
    }

    @Test
    void test_games_inLeague() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/league/%s".formatted(gameSummary.getLeagueId()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_public_inLeague() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/league/%s".formatted(gameSummary.getLeagueId()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_public_inDivision() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/league/%s/division/%s".formatted(gameSummary.getLeagueId(), gameSummary.getDivisionName()))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    void test_games_public_inDivision2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());
        String unknownDivision = "unknownDivision";

        // WHEN / THEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/games/league/%s/division/%s".formatted(gameSummary.getLeagueId(), unknownDivision))
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<TestPageImpl<GameSummary>>() {})
                .value(page -> assertEquals(0, page.getTotalElements()));
    }

    @Test
    void test_games_public_downloadDivision() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/public/games/league/%s/division/%s/excel".formatted(gameSummary.getLeagueId(), gameSummary.getDivisionName()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ByteArrayResource.class)
                .value(byteArrayResource -> assertTrue(byteArrayResource.contentLength() > 0L));
    }
}
