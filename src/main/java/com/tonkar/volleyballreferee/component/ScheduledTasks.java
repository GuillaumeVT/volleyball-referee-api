package com.tonkar.volleyballreferee.component;

import com.tonkar.volleyballreferee.repository.UserTokenRepository;
import com.tonkar.volleyballreferee.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledTasks {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserTokenRepository userTokenRepository;

    // Every day at 4:00am
    @Scheduled(cron = "0 0 4 * * *")
    public void deleteOldLiveGames() {
        log.info("Deleting every live game older than 7 days");
        gameService.deleteOldLiveGames(7);
    }

    // Every day at 4:05am
    @Scheduled(cron = "0 5 4 * * *")
    public void deleteOldScheduledGames() {
        log.info("Deleting every scheduled game older than 30 days");
        gameService.deleteOldScheduledGames(30);
    }

    // Every day at 4:10am
    @Scheduled(cron = "0 10 4 * * *")
    public void deleteTokens() {
        log.info("Deleting every tokens");
        userTokenRepository.deleteAll();
    }

}
