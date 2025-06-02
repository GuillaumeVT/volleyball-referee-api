package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.Set;

class LeagueTests extends VbrServiceTests {

    private final LeagueService leagueService;

    public LeagueTests(@Autowired LeagueService leagueService) {
        super();
        this.leagueService = leagueService;
    }

    @Test
    void test_leagues_list() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var league = sandbox.createLeague(user.getId(), GameType.INDOOR_4X4);

        // WHEN
        var leagues = leagueService.listLeagues(user, Set.of());

        // THEN
        Assertions.assertEquals(1, leagues.size());
        Assertions.assertEquals(league.getName(), leagues.getFirst().getName());
    }

    @Test
    void test_leagues_list_byKind() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var league = sandbox.createLeague(user.getId(), GameType.INDOOR_4X4);

        // WHEN
        var leagues = leagueService.listLeagues(user, Set.of(GameType.INDOOR_4X4));

        // THEN
        Assertions.assertEquals(1, leagues.size());
        Assertions.assertEquals(league.getName(), leagues.getFirst().getName());
    }

    @Test
    void test_leagues_list_byKind2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createLeague(user.getId(), GameType.INDOOR_4X4);

        // WHEN
        var leagues = leagueService.listLeagues(user, Set.of(GameType.BEACH, GameType.INDOOR));

        // THEN
        Assertions.assertEquals(0, leagues.size());
    }

    @Test
    void test_leagues_list_byKind3() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createLeague(user.getId(), GameType.INDOOR_4X4);
        sandbox.createLeague(user.getId(), GameType.INDOOR);

        // WHEN
        var leagues = leagueService.listLeagues(user, Set.of(GameType.INDOOR, GameType.INDOOR_4X4));

        // THEN
        Assertions.assertEquals(2, leagues.size());
    }

    @Test
    void test_leagues_get() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var league = sandbox.createLeague(user.getId(), GameType.INDOOR_4X4);

        // WHEN
        var league2 = leagueService.getLeague(user, league.getId());

        // THEN
        Assertions.assertNotNull(league2);
        Assertions.assertEquals(league.getName(), league2.getName());
    }

    @Test
    void test_leagues_get_notFound() {
        // GIVEN
        var user = sandbox.createAndGetUser();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> leagueService.getLeague(user, UUID.randomUUID()));
    }

    @Test
    void test_leagues_public_get() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var league = sandbox.createLeague(user.getId(), GameType.INDOOR_4X4);

        // WHEN
        var league2 = leagueService.getLeague(league.getId());

        // THEN
        Assertions.assertNotNull(league2);
        Assertions.assertEquals(league.getName(), league2.getName());
    }

    @Test
    void test_leagues_create() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        League league = sandbox.generateLeague(user.getId(), GameType.BEACH);

        // WHEN / THEN
        Assertions.assertDoesNotThrow(() -> leagueService.createLeague(user, league));
    }

    @Test
    void test_leagues_create_conflict() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var league = sandbox.createLeague(user.getId(), GameType.INDOOR);

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> leagueService.createLeague(user, league));
    }

    @Test
    void test_leagues_create_conflict2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var league = sandbox.createLeague(user.getId(), GameType.INDOOR);
        league.setId(UUID.randomUUID());

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> leagueService.createLeague(user, league));
    }

    @Test
    void test_leagues_count() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createLeague(user.getId(), GameType.INDOOR);
        sandbox.createLeague(user.getId(), GameType.BEACH);

        // WHEN
        var count = leagueService.getNumberOfLeagues(user);

        // THEN
        Assertions.assertNotNull(count);
        Assertions.assertEquals(2L, count.count());
    }

    @Test
    void test_leagues_delete() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var league = sandbox.createLeague(user.getId(), GameType.INDOOR);

        // WHEN
        leagueService.deleteLeague(user, league.getId());

        // THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> leagueService.getLeague(league.getId()));
    }

    @Test
    void test_leagues_deleteAllLeagues() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createLeague(user.getId(), GameType.INDOOR);
        sandbox.createLeague(user.getId(), GameType.INDOOR);

        // WHEN
        leagueService.deleteAllLeagues(user);

        // THEN
        var count = leagueService.getNumberOfLeagues(user);
        Assertions.assertNotNull(count);
        Assertions.assertEquals(0L, count.count());
    }
}
