package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.GameSummaryDto;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameTests extends VbrServiceTests {

    private final RulesService rulesService;
    private final TeamService  teamService;
    private final GameService  gameService;

    public GameTests(@Autowired RulesService rulesService, @Autowired TeamService teamService, @Autowired GameService gameService) {
        this.rulesService = rulesService;
        this.teamService = teamService;
        this.gameService = gameService;
    }

    @Test
    void test_games_list() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createBeachGame(user.getId());

        // WHEN
        Page<GameSummaryDto> games = gameService.listGames(user, Set.of(), Set.of(), Set.of(), PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(1, games.getTotalElements());
        Assertions.assertEquals(game.getId(), games.getContent().getFirst().getId());
    }

    @Test
    void test_games_list_byStatus() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        Page<GameSummaryDto> games = gameService.listGames(user, Set.of(game.getStatus()), Set.of(), Set.of(), PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(1, games.getTotalElements());
        Assertions.assertEquals(game.getId(), games.getContent().getFirst().getId());
    }

    @Test
    void test_games_list_byStatus2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createScheduledBeachGame(user.getId());
        var noResultGameStatus = GameStatus.LIVE;

        // WHEN
        Page<GameSummaryDto> games = gameService.listGames(user, Set.of(noResultGameStatus), Set.of(), Set.of(), PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(0, games.getTotalElements());
    }

    @Test
    void test_games_list_byStatusAndKindAndGender() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        Page<GameSummaryDto> games = gameService.listGames(user, Set.of(game.getStatus(), GameStatus.LIVE), Set.of(game.getKind()),
                                                           Set.of(game.getGender()), PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(1, games.getTotalElements());
        Assertions.assertEquals(game.getId(), games.getContent().getFirst().getId());
    }

    @Test
    void test_games_list_byStatusAndKindAndGender2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        Page<GameSummaryDto> games = gameService.listGames(user, Set.of(game.getStatus()), Set.of(GameType.INDOOR_4X4),
                                                           Set.of(game.getGender()), PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(0, games.getTotalElements());
    }

    @Test
    void test_games_list_available() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        List<GameSummaryDto> games = gameService.listAvailableGames(user);

        // THEN
        Assertions.assertEquals(1, games.size());
        Assertions.assertEquals(game.getId(), games.getFirst().getId());
    }

    @Test
    void test_games_list_available_refereedByFriend() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);
        var game = sandbox.createBeachGame(user1.getId());
        gameService.setReferee(user1, game.getId(), user2.getId());

        // WHEN
        List<GameSummaryDto> games = gameService.listAvailableGames(user2);

        // THEN
        Assertions.assertEquals(1, games.size());
        Assertions.assertEquals(game.getId(), games.getFirst().getId());
    }

    @Test
    void test_games_list_completed() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.generateBeachGame(user.getId());
        game.setStatus(GameStatus.COMPLETED);
        gameService.upsertGame(user, game);

        // WHEN
        Page<GameSummaryDto> games = gameService.listCompletedGames(user, PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(1, games.getTotalElements());
        Assertions.assertEquals(game.getId(), games.getContent().getFirst().getId());
    }

    @Test
    void test_games_get() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var game2 = gameService.getGame(user, game.getId());

        // THEN
        Assertions.assertNotNull(game2);
        Assertions.assertEquals(game.getId(), game2.getId());
    }

    @Test
    void test_games_get_notFound() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        UUID unknownGameId = UUID.randomUUID();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> gameService.getGame(user, unknownGameId));
    }

    @Test
    void test_games_get_ingredients() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var gameIngredients = gameService.getGameIngredientsOfKind(user, game.getKind());

        // THEN
        Assertions.assertNotNull(gameIngredients);
        Assertions.assertNotNull(gameIngredients.defaultRules());
        Assertions.assertNotNull(gameIngredients.rules());
        Assertions.assertNotNull(gameIngredients.teams());
    }

    @Test
    void test_games_create() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.generateScheduledBeachGame(user.getId(), true);

        // WHEN / THEN
        Assertions.assertDoesNotThrow(() -> gameService.createGame(user, game));
    }

    @Test
    void test_games_update() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createBeachGame(user.getId());
        game.setStatus(GameStatus.COMPLETED);

        // WHEN
        gameService.upsertGame(user, game);

        // THEN
        var game2 = gameService.getGame(user, game.getId());
        Assertions.assertNotNull(game2);
        Assertions.assertEquals(game.getStatus(), game2.getStatus());
    }

    @Test
    void test_games_update_upsert() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.generateBeachGame(user.getId());

        // WHEN
        gameService.upsertGame(user, game);

        // THEN
        var game2 = gameService.getGame(user, game.getId());
        Assertions.assertNotNull(game2);
    }

    @Test
    void test_games_set_update() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createBeachGame(user.getId());

        // WHEN
        gameService.updateSet(user, game.getId(), 1, sandbox.generateSet());

        // THEN
        var game2 = gameService.getGame(user, game.getId());
        Assertions.assertNotNull(game2);
        Assertions.assertEquals(1, game2.getSets().size());
    }

    @Test
    void test_games_create_refereedByFriend() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);
        var game = sandbox.generateScheduledBeachGame(user1.getId(), true);
        game.setRefereedBy(user2.getId());

        // WHEN
        gameService.createGame(user1, game);

        // THEN
        assertEquals(1L, gameService.getNumberOfAvailableGames(user1).count());
        assertEquals(1L, gameService.getNumberOfAvailableGames(user2).count());
        assertNotNull(gameService.getGame(user2, game.getId()));
    }

    @Test
    void test_games_create_refereedByFriend_notFriend() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        var game = sandbox.generateScheduledBeachGame(user1.getId(), false);
        game.setRefereedBy(user2.getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> gameService.createGame(user1, game));
    }

    @Test
    void test_games_create_refereedByFriend_notAssigned() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user1.getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> gameService.getGame(user2, game.getId()));
    }

    @Test
    void test_games_update_refereedByFriend() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);
        Game game = sandbox.generateBeachGame(user1.getId());
        game.setRefereedBy(user2.getId());
        gameService.upsertGame(user1, game);

        // WHEN / THEN
        assertDoesNotThrow(() -> gameService.upsertGame(user2, game));
    }

    @Test
    void test_games_update_refereedByFriend2() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);
        Game game = sandbox.createBeachGame(user1.getId());

        // WHEN
        gameService.setReferee(user1, game.getId(), user2.getId());

        // THEN
        assertEquals(1L, gameService.getNumberOfAvailableGames(user1).count());
        assertEquals(1L, gameService.getNumberOfAvailableGames(user2).count());
    }

    @Test
    void test_games_update_refereedByCreator() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);
        Game game = sandbox.generateBeachGame(user1.getId());
        game.setRefereedBy(user2.getId());
        gameService.upsertGame(user1, game);

        // WHEN / THEN
        assertDoesNotThrow(() -> gameService.upsertGame(user1, game));
    }

    @Test
    void test_games_schedule_update() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());
        game.setRulesId(Rules.OFFICIAL_BEACH_RULES.getId());
        game.setRulesName(Rules.OFFICIAL_BEACH_RULES.getName());

        // WHEN
        gameService.updateGame(user, game);

        // THEN
        var game2 = gameService.getGame(user, game.getId());
        Assertions.assertNotNull(game2);
        Assertions.assertEquals(Rules.OFFICIAL_BEACH_RULES.getId(), game2.getRules().getId());
    }

    @Test
    void test_games_schedule_update_notFound() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.generateScheduledBeachGame(user.getId(), false);

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> gameService.updateGame(user, game));
    }

    @Test
    void test_games_deleteRules() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        GameSummaryDto gameSummary = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        gameService.deleteGame(user, gameSummary.getId());

        // THEN
        assertDoesNotThrow(() -> rulesService.deleteRules(user, gameSummary.getRulesId()));
    }

    @Test
    void test_games_deleteRules_conflict() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        GameSummaryDto gameSummary = sandbox.createScheduledBeachGame(user.getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> rulesService.deleteRules(user, gameSummary.getRulesId()));
    }

    @Test
    void test_games_deleteTeam() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        GameSummaryDto gameSummary = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        gameService.deleteGame(user, gameSummary.getId());

        // THEN
        assertDoesNotThrow(() -> teamService.deleteTeam(user, gameSummary.getHomeTeamId()));
    }

    @Test
    void test_games_deleteTeam_conflict() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        GameSummaryDto gameSummary = sandbox.createScheduledBeachGame(user.getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> teamService.deleteTeam(user, gameSummary.getHomeTeamId()));
    }

    @Test
    void test_games_delete() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        GameSummaryDto gameSummary = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        gameService.deleteGame(user, gameSummary.getId());

        // THEN
        assertThrows(ResponseStatusException.class, () -> gameService.getGame(gameSummary.getId()));
    }

    @Test
    void test_games_delete_refereedByFriend() {
        // Referee cannot delete game of creator
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);
        Game game = sandbox.generateBeachGame(user1.getId());
        game.setRefereedBy(user2.getId());
        gameService.upsertGame(user1, game);

        // WHEN
        gameService.deleteGame(user2, game.getId());

        // THEN
        assertNotNull(gameService.getGame(user2, game.getId()));
    }

    @Test
    void test_games_deleteAll() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachGame(user.getId());
        Game game = sandbox.createBeachGame(user.getId());
        game.setStatus(GameStatus.COMPLETED);
        gameService.upsertGame(user, game);

        // WHEN
        gameService.deleteAllGames(user);

        // THEN
        // only COMPLETED games are affected by deleteAll
        assertEquals(1, gameService.listGames(user, null, null, null, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    void test_games_deleteAll2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createScheduledBeachGame(user.getId());
        sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        gameService.deleteAllGames(user);

        // THEN
        // only COMPLETED games are affected by deleteAll
        assertEquals(2, gameService.listGames(user, null, null, null, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    void test_games_count() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createScheduledBeachGame(user.getId());
        sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var count = gameService.getNumberOfGames(user);

        // THEN
        Assertions.assertNotNull(count);
        Assertions.assertEquals(2L, count.count());
    }

    @Test
    void test_games_count_inLeague() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());
        sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var count = gameService.getNumberOfGamesInLeague(user, game.getLeagueId());

        // THEN
        Assertions.assertNotNull(count);
        Assertions.assertEquals(1L, count.count());
    }

    @Test
    void test_games_inLeague() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var games = gameService.listGamesInLeague(user, game.getLeagueId(), Set.of(game.getStatus()), Set.of(), PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(1, games.getTotalElements());
        Assertions.assertEquals(game.getId(), games.getContent().getFirst().getId());
    }

    @Test
    void test_games_public_list_token() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        Game game = sandbox.createBeachGame(user.getId());
        String token = game.getGuestTeam().getName().substring(0, 4);

        // WHEN
        Page<GameSummaryDto> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    void test_games_public_list_token2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachGame(user.getId());
        String token = user.getPseudo().substring(0, 3);

        // WHEN
        Page<GameSummaryDto> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    void test_games_public_list_token3() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        Game game = sandbox.createBeachGame(user.getId());
        String token = game.getLeague().getName().substring(0, 3);

        // WHEN
        Page<GameSummaryDto> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    void test_games_public_list_token_noMatch() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachGame(user.getId());
        String token = "noMatch";

        // WHEN
        Page<GameSummaryDto> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }

    @Test
    void test_games_public_list_date() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachGame(user.getId());
        LocalDate date = LocalDate.now();

        // WHEN
        Page<GameSummaryDto> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    void test_games_public_list_date_noMatch() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachGame(user.getId());
        LocalDate date = LocalDate.now().plusDays(10L);

        // WHEN
        Page<GameSummaryDto> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }

    @Test
    void test_games_public_get() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var game2 = gameService.getGame(game.getId());

        // THEN
        Assertions.assertNotNull(game2);
        Assertions.assertEquals(game.getId(), game2.getId());
    }

    @Test
    void test_games_public_get_notFound() {
        // GIVEN
        UUID unknownGameId = UUID.randomUUID();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> gameService.getGame(unknownGameId));
    }

    @Test
    void test_games_public_inLeague() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var games = gameService.listGamesInLeague(game.getLeagueId(), Set.of(game.getStatus()), Set.of(), PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(1, games.getTotalElements());
        Assertions.assertEquals(game.getId(), games.getContent().getFirst().getId());
    }

    @Test
    void test_games_public_inDivision() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var games = gameService.listGamesInDivision(game.getLeagueId(), game.getDivisionName(), Set.of(game.getStatus()), Set.of(),
                                                    PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(1, games.getTotalElements());
        Assertions.assertEquals(game.getId(), games.getContent().getFirst().getId());
    }

    @Test
    void test_games_public_inDivision2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());
        String unknownDivision = "unknownDivision";

        // WHEN
        var games = gameService.listGamesInDivision(game.getLeagueId(), unknownDivision, Set.of(game.getStatus()), Set.of(),
                                                    PageRequest.of(0, 20));

        // THEN
        Assertions.assertEquals(0, games.getTotalElements());
    }

    @Test
    void test_games_public_downloadDivision() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var game = sandbox.createScheduledBeachGame(user.getId());

        // WHEN
        var fileWrapper = Assertions.assertDoesNotThrow(
                () -> gameService.listGamesInDivisionExcel(game.getLeagueId(), game.getDivisionName()));

        // THEN
        Assertions.assertNotNull(fileWrapper);
        Assertions.assertNotNull(fileWrapper.data());
    }
}
