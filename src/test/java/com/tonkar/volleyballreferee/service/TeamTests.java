package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.*;

class TeamTests extends VbrServiceTests {

    private final TeamService teamService;

    public TeamTests(@Autowired TeamService teamService) {
        super();
        this.teamService = teamService;
    }

    @Test
    void test_teams_list() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        var teams = teamService.listTeams(user, Set.of(), Set.of(), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(1, teams.getTotalElements());
        Assertions.assertEquals(team.getName(), teams.getContent().getFirst().name());
    }

    @Test
    void test_teams_list_byKind() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        var teams = teamService.listTeams(user, Set.of(team.getKind()), Set.of(), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(1, teams.getTotalElements());
        Assertions.assertEquals(team.getName(), teams.getContent().getFirst().name());
    }

    @Test
    void test_teams_list_byKind2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachTeam(user.getId());
        var noResultGameType = GameType.INDOOR_4X4;

        // WHEN
        var teams = teamService.listTeams(user, Set.of(noResultGameType), Set.of(), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(0, teams.getTotalElements());
    }

    @Test
    void test_teams_list_byKind3() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        var teams = teamService.listTeams(user, Set.of(team.getKind(), GameType.INDOOR), Set.of(), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(1, teams.getTotalElements());
        Assertions.assertEquals(team.getName(), teams.getContent().getFirst().name());
    }

    @Test
    void test_teams_list_byGender() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        var teams = teamService.listTeams(user, Set.of(), Set.of(team.getGender()), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(1, teams.getTotalElements());
        Assertions.assertEquals(team.getName(), teams.getContent().getFirst().name());
    }

    @Test
    void test_teams_list_byGender2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachTeam(user.getId());
        var noResultGenderType = GenderType.GENTS;

        // WHEN
        var teams = teamService.listTeams(user, Set.of(), Set.of(noResultGenderType), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(0, teams.getTotalElements());
    }

    @Test
    void test_teams_list_byGender3() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        var teams = teamService.listTeams(user, Set.of(), Set.of(team.getGender(), GenderType.GENTS), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(1, teams.getTotalElements());
        Assertions.assertEquals(team.getName(), teams.getContent().getFirst().name());
    }

    @Test
    void test_teams_list_byKindAndGender() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        var teams = teamService.listTeams(user, Set.of(team.getKind()), Set.of(team.getGender()), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(1, teams.getTotalElements());
        Assertions.assertEquals(team.getName(), teams.getContent().getFirst().name());
    }

    @Test
    void test_teams_list_byKindAndGender2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachTeam(user.getId());
        var noResultGameType = GameType.INDOOR_4X4;
        var noResultGenderType = GenderType.GENTS;

        // WHEN
        var teams = teamService.listTeams(user, Set.of(noResultGameType), Set.of(noResultGenderType), PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(0, teams.getTotalElements());
    }

    @Test
    void test_teams_list_byKindAndGender3() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        var teams = teamService.listTeams(user, Set.of(team.getKind(), GameType.INDOOR_4X4), Set.of(team.getGender(), GenderType.MIXED),
                                          PageRequest.of(0, 5));

        // THEN
        Assertions.assertEquals(1, teams.getTotalElements());
        Assertions.assertEquals(team.getName(), teams.getContent().getFirst().name());
    }

    @Test
    void test_teams_get() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        var team2 = teamService.getTeam(user, team.getId());

        // THEN
        Assertions.assertNotNull(team2);
        Assertions.assertEquals(team.getName(), team2.getName());
    }

    @Test
    void test_teams_get_notFound() {
        // GIVEN
        var user = sandbox.createAndGetUser();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> teamService.getTeam(user, UUID.randomUUID()));
    }

    @Test
    void test_teams_create() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.generateBeachTeam(user.getId());

        // WHEN / THEN
        Assertions.assertDoesNotThrow(() -> teamService.createTeam(user, team));
    }

    @Test
    void test_teams_create_conflict() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> teamService.createTeam(user, team));
    }

    @Test
    void test_teams_create_conflict2() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());
        team.setId(UUID.randomUUID());

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> teamService.createTeam(user, team));
    }

    @Test
    void test_teams_update() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        team.setUpdatedAt(System.currentTimeMillis());
        team.setColor(faker.color().hex());

        // WHEN
        teamService.updateTeam(user, team);

        // THEN
        var team2 = teamService.getTeam(user, team.getId());
        Assertions.assertNotNull(team2);
        Assertions.assertEquals(team.getColor(), team2.getColor());
    }

    @Test
    void test_teams_update_notFound() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.generateBeachTeam(user.getId());

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> teamService.updateTeam(user, team));
    }

    @Test
    void test_teams_count() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachTeam(user.getId());
        sandbox.createBeachTeam(user.getId());

        // WHEN
        var count = teamService.getNumberOfTeams(user);

        // THEN
        Assertions.assertNotNull(count);
        Assertions.assertEquals(2L, count.count());
    }

    @Test
    void test_teams_delete() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        var team = sandbox.createBeachTeam(user.getId());

        // WHEN
        teamService.deleteTeam(user, team.getId());

        // THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> teamService.getTeam(user, team.getId()));
    }

    @Test
    void test_teams_deleteAll() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachTeam(user.getId());
        sandbox.createBeachTeam(user.getId());

        // WHEN
        teamService.deleteAllTeams(user);

        // THEN
        var count = teamService.getNumberOfTeams(user);
        Assertions.assertNotNull(count);
        Assertions.assertEquals(0L, count.count());
    }
}
