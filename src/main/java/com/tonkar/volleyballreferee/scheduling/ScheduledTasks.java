package com.tonkar.volleyballreferee.scheduling;

import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final GameService         gameService;
    private final TeamService         teamService;
    private final RulesService        rulesService;
    private final LeagueService       leagueService;
    private final UserService         userService;
    private final SubscriptionService subscriptionService;
    private final UserDao             userDao;

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
    public void purgeOldPasswordResets() {
        userService.purgeOldPasswordResets(1);
    }

    // Every day at 4:15am
    @Scheduled(cron = "0 15 4 * * *")
    public void refreshInactiveAccounts() {
        log.info("Refresh inactive accounts with subscription expiry older than 3 months");
        userDao.findUsersBySubscriptionExpiryBefore(3L).forEach(user -> subscriptionService.refreshSubscriptionPurchaseToken(user.getPurchaseToken()));
    }

    // Every day at 4:30am
    @Scheduled(cron = "0 30 4 * * *")
    public void purgeOldCancelledAccounts() {
        log.info("Deleted cancelled accounts with subscription expiry older than 6 months");
        userDao.findUsersBySubscriptionExpiryBefore(6L).forEach(user -> {
            log.info(String.format("Deleting user %s %s", user.getId(), user.getEmail()));
            gameService.deleteAllGames(user);
            teamService.deleteAllTeams(user);
            rulesService.deleteAllRules(user);
            leagueService.deleteAllLeagues(user);
            userService.deleteUser(user);
        });
    }
}
