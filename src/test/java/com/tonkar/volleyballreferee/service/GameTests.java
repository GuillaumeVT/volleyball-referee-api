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
        sandbox.addFriend(sandbox.getUser(userToken.getUser().getId()), sandbox.getUser(userToken2.getUser().getId()));
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.getUser().getId(), true);
        gameSummary.setRefereedBy(userToken2.getUser().getId());

        // WHEN
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), gameSummary);

        // THEN
        assertEquals(1L, gameService.getNumberOfAvailableGames(sandbox.getUser(userToken.getUser().getId())).getCount());
        assertEquals(1L, gameService.getNumberOfAvailableGames(sandbox.getUser(userToken2.getUser().getId())).getCount());
        assertNotNull(gameService.getGame(sandbox.getUser(userToken2.getUser().getId()), gameSummary.getId()));
    }

    @Test
    public void test_games_create_refereedByFriend_notFriend() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        GameSummary gameSummary = sandbox.generateScheduledBeachGame(userToken.getUser().getId(), false);
        gameSummary.setRefereedBy(userToken2.getUser().getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> gameService.createGame(sandbox.getUser(userToken.getUser().getId()), gameSummary));
    }

    @Test
    public void test_games_create_refereedByFriend_notAssigned() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> gameService.getGame(sandbox.getUser(userToken2.getUser().getId()), gameSummary.getId()));
    }

    @Test
    public void test_games_update_refereedByFriend() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.getUser().getId()), sandbox.getUser(userToken2.getUser().getId()));
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());
        game.setRefereedBy(userToken2.getUser().getId());
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), game);

        // WHEN / THEN
        assertDoesNotThrow(() -> gameService.updateGame(sandbox.getUser(userToken2.getUser().getId()), game));
    }

    @Test
    public void test_games_update_refereedByFriend2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.getUser().getId()), sandbox.getUser(userToken2.getUser().getId()));
        Game game = sandbox.createBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.setReferee(sandbox.getUser(userToken.getUser().getId()), game.getId(), userToken2.getUser().getId());

        // THEN
        assertEquals(1L, gameService.getNumberOfAvailableGames(sandbox.getUser(userToken.getUser().getId())).getCount());
        assertEquals(1L, gameService.getNumberOfAvailableGames(sandbox.getUser(userToken2.getUser().getId())).getCount());
    }

    @Test
    public void test_games_update_refereedByCreator() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.getUser().getId()), sandbox.getUser(userToken2.getUser().getId()));
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());
        game.setRefereedBy(userToken2.getUser().getId());
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), game);

        // WHEN / THEN
        assertDoesNotThrow(() -> gameService.updateGame(sandbox.getUser(userToken.getUser().getId()), game));
    }

    @Test
    public void test_games_deleteRules() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.deleteGame(sandbox.getUser(userToken.getUser().getId()), gameSummary.getId());

        // THEN
        assertDoesNotThrow(() -> rulesService.deleteRules(sandbox.getUser(userToken.getUser().getId()), gameSummary.getRulesId()));
    }

    @Test
    public void test_games_deleteRules_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> rulesService.deleteRules(sandbox.getUser(userToken.getUser().getId()), gameSummary.getRulesId()));
    }

    @Test
    public void test_games_deleteTeam() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.deleteGame(sandbox.getUser(userToken.getUser().getId()), gameSummary.getId());

        // THEN
        assertDoesNotThrow(() -> teamService.deleteTeam(sandbox.getUser(userToken.getUser().getId()), gameSummary.getHomeTeamId()));
    }

    @Test
    public void test_games_deleteTeam_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> teamService.deleteTeam(sandbox.getUser(userToken.getUser().getId()), gameSummary.getHomeTeamId()));
    }

    @Test
    public void test_games_delete() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        GameSummary gameSummary = sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.deleteGame(sandbox.getUser(userToken.getUser().getId()), gameSummary.getId());

        // THEN
        assertTrue(gameService.listGames(sandbox.getUser(userToken.getUser().getId()), null, null, null, PageRequest.of(0, 20)).getContent().isEmpty());
    }

    @Test
    public void test_games_delete_refereedByFriend() {
        // Referee cannot delete game of creator
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.getUser().getId()), sandbox.getUser(userToken2.getUser().getId()));
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());
        game.setRefereedBy(userToken2.getUser().getId());
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), game);

        // WHEN
        gameService.deleteGame(sandbox.getUser(userToken2.getUser().getId()), game.getId());

        // THEN
        assertNotNull(gameService.getGame(sandbox.getUser(userToken2.getUser().getId()), game.getId()));
    }

    @Test
    public void test_games_deleteAll() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createBeachGame(userToken.getUser().getId());
        Game game = sandbox.createBeachGame(userToken.getUser().getId());
        game.setStatus(GameStatus.COMPLETED);
        gameService.updateGame(sandbox.getUser(userToken.getUser().getId()), game);

        // WHEN
        gameService.deleteAllGames(sandbox.getUser(userToken.getUser().getId()));

        // THEN
        // only COMPLETED games are affected by deleteAll
        assertEquals(1, gameService.listGames(sandbox.getUser(userToken.getUser().getId()), null, null, null, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    public void test_games_deleteAll2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        sandbox.createScheduledBeachGame(userToken.getUser().getId());
        sandbox.createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.deleteAllGames(sandbox.getUser(userToken.getUser().getId()));

        // THEN
        // only COMPLETED games are affected by deleteAll
        assertEquals(2, gameService.listGames(sandbox.getUser(userToken.getUser().getId()), null, null, null, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    public void test_games_public_list_token() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.getUser().getId());
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
        sandbox.createBeachGame(userToken.getUser().getId());
        String token = userToken.getUser().getPseudo().substring(0, 3);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token3() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        Game game = sandbox.createBeachGame(userToken.getUser().getId());
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
        sandbox.createBeachGame(userToken.getUser().getId());
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
        sandbox.createBeachGame(userToken.getUser().getId());
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
        sandbox.createBeachGame(userToken.getUser().getId());
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
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());
        game.setIndexed(false);
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), game);
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
        Game game = sandbox.generateBeachGame(userToken.getUser().getId());
        game.setIndexed(false);
        gameService.createGame(sandbox.getUser(userToken.getUser().getId()), game);
        LocalDate date = LocalDate.now();

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }
}
