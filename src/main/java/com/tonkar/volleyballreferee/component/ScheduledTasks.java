package com.tonkar.volleyballreferee.component;

import com.tonkar.volleyballreferee.model.UserId;
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
    public void deleteOldLiveGames() {
        LOGGER.info("Deleting every live game older than 1 day");
        gameService.deleteOldLiveGames(1, UserId.VBR_USER_ID);
    }

    // Every day at 4:05am
    @Scheduled(cron = "0 5 4 * * *")
    public void deleteVeryOldLiveGames() {
        LOGGER.info("Deleting every live game older than 30 days");
        gameService.deleteOldLiveGames(7);
    }

    // Every day at 4:15am
    @Scheduled(cron = "0 15 4 * * *")
    public void deleteTestGames() {
        LOGGER.info("Deleting every test game with a set duration under 5 minutes");
        gameService.deleteTestGames(5);
    }

    // Every monday at 4:30am
    @Scheduled(cron = "0 30 4 1 * *")
    public void deleteOldGames() {
        LOGGER.info("Deleting every game older than 1 year");
        gameService.deleteOldGames(365);
        LOGGER.info("Deleting every code older than 1 year");
        gameService.deleteOldCodes(365);
    }

    // Every day at 4pm
    @Scheduled(cron = "0 0 16 * * *")
    public void deleteOldLiveGames2() {
        LOGGER.info("Deleting every live game older than 1 day");
        gameService.deleteOldLiveGames(1, UserId.VBR_USER_ID);
    }

}
