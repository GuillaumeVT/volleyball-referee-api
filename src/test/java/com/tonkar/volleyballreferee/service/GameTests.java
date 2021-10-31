package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

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
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));
        GameSummary gameSummary = generateScheduledBeachGame(userToken.getUser().getId(), true);
        gameSummary.setRefereedBy(userToken2.getUser().getId());

        // WHEN
        gameService.createGame(getUser(userToken.getUser().getId()), gameSummary);

        // THEN
        assertEquals(1L, gameService.getNumberOfAvailableGames(getUser(userToken.getUser().getId())).getCount());
        assertEquals(1L, gameService.getNumberOfAvailableGames(getUser(userToken2.getUser().getId())).getCount());
        assertNotNull(gameService.getGame(getUser(userToken2.getUser().getId()), gameSummary.getId()));
    }

    @Test
    public void test_games_create_refereedByFriend_notFriend() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        GameSummary gameSummary = generateScheduledBeachGame(userToken.getUser().getId(), false);
        gameSummary.setRefereedBy(userToken2.getUser().getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> gameService.createGame(getUser(userToken.getUser().getId()), gameSummary));
    }

    @Test
    public void test_games_create_refereedByFriend_notAssigned() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> gameService.getGame(getUser(userToken2.getUser().getId()), gameSummary.getId()));
    }

    @Test
    public void test_games_update_refereedByFriend() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));
        Game game = generateBeachGame(userToken.getUser().getId());
        game.setRefereedBy(userToken2.getUser().getId());
        gameService.createGame(getUser(userToken.getUser().getId()), game);

        // WHEN / THEN
        assertDoesNotThrow(() -> gameService.updateGame(getUser(userToken2.getUser().getId()), game));
    }

    @Test
    public void test_games_update_refereedByFriend2() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));
        Game game = createBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.setReferee(getUser(userToken.getUser().getId()), game.getId(), userToken2.getUser().getId());

        // THEN
        assertEquals(1L, gameService.getNumberOfAvailableGames(getUser(userToken.getUser().getId())).getCount());
        assertEquals(1L, gameService.getNumberOfAvailableGames(getUser(userToken2.getUser().getId())).getCount());
    }

    @Test
    public void test_games_update_refereedByCreator() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));
        Game game = generateBeachGame(userToken.getUser().getId());
        game.setRefereedBy(userToken2.getUser().getId());
        gameService.createGame(getUser(userToken.getUser().getId()), game);

        // WHEN / THEN
        assertDoesNotThrow(() -> gameService.updateGame(getUser(userToken.getUser().getId()), game));
    }

    @Test
    public void test_games_deleteRules() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.deleteGame(getUser(userToken.getUser().getId()), gameSummary.getId());

        // THEN
        assertDoesNotThrow(() -> rulesService.deleteRules(getUser(userToken.getUser().getId()), gameSummary.getRulesId()));
    }

    @Test
    public void test_games_deleteRules_conflict() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> rulesService.deleteRules(getUser(userToken.getUser().getId()), gameSummary.getRulesId()));
    }

    @Test
    public void test_games_deleteTeam() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.deleteGame(getUser(userToken.getUser().getId()), gameSummary.getId());

        // THEN
        assertDoesNotThrow(() -> teamService.deleteTeam(getUser(userToken.getUser().getId()), gameSummary.getHomeTeamId()));
    }

    @Test
    public void test_games_deleteTeam_conflict() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> teamService.deleteTeam(getUser(userToken.getUser().getId()), gameSummary.getHomeTeamId()));
    }

    @Test
    public void test_games_delete() {
        // GIVEN
        UserToken userToken = createUser();
        GameSummary gameSummary = createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.deleteGame(getUser(userToken.getUser().getId()), gameSummary.getId());

        // THEN
        assertTrue(gameService.listGames(getUser(userToken.getUser().getId()), null, null, null, PageRequest.of(0, 20)).getContent().isEmpty());
    }

    @Test
    public void test_games_delete_refereedByFriend() {
        // Referee cannot delete game of creator
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));
        Game game = generateBeachGame(userToken.getUser().getId());
        game.setRefereedBy(userToken2.getUser().getId());
        gameService.createGame(getUser(userToken.getUser().getId()), game);

        // WHEN
        gameService.deleteGame(getUser(userToken2.getUser().getId()), game.getId());

        // THEN
        assertNotNull(gameService.getGame(getUser(userToken2.getUser().getId()), game.getId()));
    }

    @Test
    public void test_games_deleteAll() {
        // GIVEN
        UserToken userToken = createUser();
        createBeachGame(userToken.getUser().getId());
        Game game = createBeachGame(userToken.getUser().getId());
        game.setStatus(GameStatus.COMPLETED);
        gameService.updateGame(getUser(userToken.getUser().getId()), game);

        // WHEN
        gameService.deleteAllGames(getUser(userToken.getUser().getId()));

        // THEN
        // only COMPLETED games are affected by deleteAll
        assertEquals(1, gameService.listGames(getUser(userToken.getUser().getId()), null, null, null, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    public void test_games_deleteAll2() {
        // GIVEN
        UserToken userToken = createUser();
        createScheduledBeachGame(userToken.getUser().getId());
        createScheduledBeachGame(userToken.getUser().getId());

        // WHEN
        gameService.deleteAllGames(getUser(userToken.getUser().getId()));

        // THEN
        // only COMPLETED games are affected by deleteAll
        assertEquals(2, gameService.listGames(getUser(userToken.getUser().getId()), null, null, null, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    public void test_games_public_list_token() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = createBeachGame(userToken.getUser().getId());
        String token = game.getGuestTeam().getName().substring(0, 4);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token2() {
        // GIVEN
        UserToken userToken = createUser();
        createBeachGame(userToken.getUser().getId());
        String token = userToken.getUser().getPseudo().substring(0, 3);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token3() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = createBeachGame(userToken.getUser().getId());
        String token = game.getLeague().getName().substring(0, 3);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token_noMatch() {
        // GIVEN
        UserToken userToken = createUser();
        createBeachGame(userToken.getUser().getId());
        String token = "noMatch";

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_date() {
        // GIVEN
        UserToken userToken = createUser();
        createBeachGame(userToken.getUser().getId());
        LocalDate date = LocalDate.now();

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(1, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_date_noMatch() {
        // GIVEN
        UserToken userToken = createUser();
        createBeachGame(userToken.getUser().getId());
        LocalDate date = LocalDate.now().plusDays(10L);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_token_notIndexed() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = generateBeachGame(userToken.getUser().getId());
        game.setIndexed(false);
        gameService.createGame(getUser(userToken.getUser().getId()), game);
        String token = game.getGuestTeam().getName().substring(0, 4);

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesMatchingToken(token, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
    }

    @Test
    public void test_games_public_list_date_notIndexed() {
        // GIVEN
        UserToken userToken = createUser();
        Game game = generateBeachGame(userToken.getUser().getId());
        game.setIndexed(false);
        gameService.createGame(getUser(userToken.getUser().getId()), game);
        LocalDate date = LocalDate.now();

        // WHEN
        Page<GameSummary> gameSummary = gameService.listGamesWithScheduleDate(date, null, null, null, PageRequest.of(0, 20));

        // THEN
        assertEquals(0, gameSummary.getTotalElements());
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
