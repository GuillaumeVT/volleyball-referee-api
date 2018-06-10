package com.tonkar.volleyballreferee.component;

import com.tonkar.volleyballreferee.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private GameService gameService;

    // Every day at 4am
    @Scheduled(cron = "0 0 4 * * *")
    public void deletePublicLiveGames() {
        LOGGER.info("Deleting every public live game older than 1 day");
        gameService.deletePublicLiveGames(1);
    }

    // Every day at 4:05am
    @Scheduled(cron = "0 5 4 * * *")
    public void deleteOldLiveGames() {
        LOGGER.info("Deleting every live game older than 7 days");
        gameService.deleteOldLiveGames(7);
    }

    // Every day at 4:10am
    @Scheduled(cron = "0 10 4 * * *")
    public void deletePublicTestGames() {
        LOGGER.info("Deleting every public test game with all sets duration under 5 minutes");
        gameService.deletePublicTestGames(5);
    }

    // Every day at 4:15am
    @Scheduled(cron = "0 15 4 * * *")
    public void deletePublicGames() {
        LOGGER.info("Deleting every public game older than 2 days");
        gameService.deletePublicGames(2);
    }

    // Every day at 4:20am
    @Scheduled(cron = "0 20 4 * * *")
    public void deleteOldCodes() {
        LOGGER.info("Deleting every code older than 90 days");
        gameService.deleteOldCodes(90);
    }

    // Every day at 4pm
    @Scheduled(cron = "0 0 16 * * *")
    public void deletePublicLiveGames2() {
        LOGGER.info("Deleting every public live game older than 1 day");
        gameService.deletePublicLiveGames(1);
    }

}
