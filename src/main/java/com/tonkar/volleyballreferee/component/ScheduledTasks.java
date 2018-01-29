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

    @Scheduled(cron = "0 0 4 * * *")
    public void deleteOldLiveGames() {
        LOGGER.info("Deleting every live game older than 1 day");
        gameService.deleteOldLiveGames(1);
    }

    @Scheduled(cron = "0 30 4 1 * *")
    public void deleteOldGames() {
        LOGGER.info("Deleting every game older than 1 year");
        gameService.deleteOldGames(365);
    }

}
