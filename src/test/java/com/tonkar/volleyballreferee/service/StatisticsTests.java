package com.tonkar.volleyballreferee.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

class StatisticsTests extends VbrServiceTests {

    private final StatisticsService statisticsService;

    public StatisticsTests(@Autowired StatisticsService statisticsService) {
        super();
        this.statisticsService = statisticsService;
    }

    @Test
    void test_statistics_get() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachTeam(user.getId());
        sandbox.createBeachTeam(user.getId());
        sandbox.createBeachGame(user.getId());

        // WHEN
        var statistics = statisticsService.getUserStatistics(user);

        // THEN
        Assertions.assertNotNull(statistics);
        Assertions.assertEquals(2, statistics.userStatistics().teamStatistics().getFirst().count());
        Assertions.assertEquals(1, statistics.userStatistics().gameStatistics().getFirst().count());
        // Depends on other unit tests
        Assertions.assertTrue(2 <= statistics.globalStatistics().teamStatistics().getFirst().count());
        Assertions.assertTrue(1 <= statistics.globalStatistics().gameStatistics().getFirst().count());
    }

    @Test
    void test_statistics_public_get() {
        // GIVEN
        var user = sandbox.createAndGetUser();
        sandbox.createBeachTeam(user.getId());
        sandbox.createBeachTeam(user.getId());
        sandbox.createBeachTeam(user.getId());
        sandbox.createBeachGame(user.getId());
        sandbox.createBeachGame(user.getId());

        // WHEN
        var statistics = statisticsService.getGlobalStatistics();

        // THEN
        Assertions.assertNotNull(statistics);
        // Depends on other unit tests
        Assertions.assertTrue(3 <= statistics.globalStatistics().teamStatistics().getFirst().count());
        Assertions.assertTrue(2 <= statistics.globalStatistics().gameStatistics().getFirst().count());
    }
}
