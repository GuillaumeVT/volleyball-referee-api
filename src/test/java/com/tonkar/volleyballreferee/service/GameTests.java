package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

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
    public void test_games_create_refereedByFriend() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.user().id(), true);
        gameSummary.setRefereedBy(userToken2.user().id());

        // WHEN
        gameService.createGame(sandbox.getUser(userToken.user().id()), gameSummary);

        // THEN
        assertEquals(1L, gameService.getNumberOfAvailableGames(sandbox.getUser(userToken.user().id())).count());
        assertEquals(1L, gameService.getNumberOfAvailableGames(sandbox.getUser(userToken2.user().id())).count());
        assertNotNull(gameService.getGame(sandbox.getUser(userToken2.user().id()), gameSummary.getId()));
    }

    @Test
    public void test_games_create_refereedByFriend_notFriend() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.user().id(), false);
        gameSummary.setRefereedBy(userToken2.user().id());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> gameService.createGame(sandbox.getUser(userToken.user().id()), gameSummary));
    }

    @Test
    public void test_games_create_refereedByFriend_notAssigned() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> gameService.getGame(sandbox.getUser(userToken2.user().id()), gameSummary.getId()));
    }

    @Test
    public void test_games_update_refereedByFriend() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));
        Game game = sandbox.generateBeachGame(userToken.user().id());
        game.setRefereedBy(userToken2.user().id());
        gameService.createGame(sandbox.getUser(userToken.user().id()), game);

        // WHEN / THEN
        assertDoesNotThrow(() -> gameService.updateGame(sandbox.getUser(userToken2.user().id()), game));
    }

    @Test
    public void test_games_update_refereedByFriend2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));
        Game game = sandbox.createBeachGame(userToken.user().id());

        // WHEN
        gameService.setReferee(sandbox.getUser(userToken.user().id()), game.getId(), userToken2.user().id());

        // THEN
        assertEquals(1L, gameService.getNumberOfAvailableGames(sandbox.getUser(userToken.user().id())).count());
        assertEquals(1L, gameService.getNumberOfAvailableGames(sandbox.getUser(userToken2.user().id())).count());
    }

    @Test
    public void test_games_update_refereedByCreator() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));
        Game game = sandbox.generateBeachGame(userToken.user().id());
        game.setRefereedBy(userToken2.user().id());
        gameService.createGame(sandbox.getUser(userToken.user().id()), game);

        // WHEN / THEN
        assertDoesNotThrow(() -> gameService.updateGame(sandbox.getUser(userToken.user().id()), game));
    }

    @Test
    public void test_games_deleteRules() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN
        gameService.deleteGame(sandbox.getUser(userToken.user().id()), gameSummary.getId());

        // THEN
        assertDoesNotThrow(() -> rulesService.deleteRules(sandbox.getUser(userToken.user().id()), gameSummary.getRulesId()));
    }

    @Test
    public void test_games_deleteRules_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> rulesService.deleteRules(sandbox.getUser(userToken.user().id()), gameSummary.getRulesId()));
    }

    @Test
    public void test_games_deleteTeam() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN
        gameService.deleteGame(sandbox.getUser(userToken.user().id()), gameSummary.getId());

        // THEN
        assertDoesNotThrow(() -> teamService.deleteTeam(sandbox.getUser(userToken.user().id()), gameSummary.getHomeTeamId()));
    }

    @Test
    public void test_games_deleteTeam_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> teamService.deleteTeam(sandbox.getUser(userToken.user().id()), gameSummary.getHomeTeamId()));
    }

    @Test
    public void test_games_delete() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN
        gameService.deleteGame(sandbox.getUser(userToken.user().id()), gameSummary.getId());

        // THEN
        assertTrue(gameService.listGames(sandbox.getUser(userToken.user().id()), null, null, null, PageRequest.of(0, 20)).getContent().isEmpty());
    }

    @Test
    public void test_games_delete_refereedByFriend() {
        // Referee cannot delete game of creator
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));
        Game game = sandbox.generateBeachGame(userToken.user().id());
        game.setRefereedBy(userToken2.user().id());
        gameService.createGame(sandbox.getUser(userToken.user().id()), game);

        // WHEN
        gameService.deleteGame(sandbox.getUser(userToken2.user().id()), game.getId());

        // THEN
        assertNotNull(gameService.getGame(sandbox.getUser(userToken2.user().id()), game.getId()));
    }

    @Test
    public void test_games_deleteAll() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachGame(userToken.user().id());
        Game game = sandbox.createBeachGame(userToken.user().id());
        game.setStatus(GameStatus.COMPLETED);
        gameService.updateGame(sandbox.getUser(userToken.user().id()), game);

        // WHEN
        gameService.deleteAllGames(sandbox.getUser(userToken.user().id()));

        // THEN
        // only COMPLETED games are affected by deleteAll
        assertEquals(1, gameService.listGames(sandbox.getUser(userToken.user().id()), null, null, null, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    public void test_games_deleteAll2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.user().id());
        sandbox.createScheduledBeachGame(userToken.user().id());

        // WHEN
        gameService.deleteAllGames(sandbox.getUser(userToken.user().id()));

        // THEN
        // only COMPLETED games are affected by deleteAll
        assertEquals(2, gameService.listGames(sandbox.getUser(userToken.user().id()), null, null, null, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    public void test_games_public_list_token() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.user().id());
        String token = game.getGuestTeam().getName().substring(0, 4);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachGame(userToken.user().id());
        String token = userToken.user().pseudo().substring(0, 3);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token3() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.user().id());
        String token = game.getLeague().getName().substring(0, 3);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token_noMatch() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachGame(userToken.user().id());
        String token = "noMatch";

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_date() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachGame(userToken.user().id());
        LocalDate date = LocalDate.now();

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_date_noMatch() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachGame(userToken.user().id());
        LocalDate date = LocalDate.now().plusDays(10L);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token_notIndexed() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.user().id());
        game.setIndexed(false);
        gameService.createGame(sandbox.getUser(userToken.user().id()), game);
        String token = game.getGuestTeam().getName().substring(0, 4);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_date_notIndexed() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.generateBeachGame(userToken.user().id());
        game.setIndexed(false);
        gameService.createGame(sandbox.getUser(userToken.user().id()), game);
        LocalDate date = LocalDate.now();

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }
}
