package com.tonkar.volleyballreferee.scheduling;

import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final GameService           gameService;
    private final UserService           userService;
    private final SubscriptionService   subscriptionService;
    private final GdprComplianceService gdprComplianceService;
    private final UserDao               userDao;

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
        log.info("Refreshing inactive accounts with subscription expiry older than 1 month");
        userDao.findUsersBySubscriptionExpiryBefore(1L).forEach(user -> {
            try {
                subscriptionService.refreshSubscriptionPurchaseToken(user.getPurchaseToken());
            } catch (Exception e) {
                log.info(
                        "The subscription purchase token of user with pseudo {} does not appear to be refresh-able. It may be gone because cancelled for more than 2 months",
                        user.getPseudo());
            }
        });
    }

    // Every day at 4:30am
    @Scheduled(cron = "0 30 4 * * *")
    public void purgeInactiveAccounts() {
        log.info("Deleting inactive accounts with subscription expiry older than 6 months");
        userDao.findUsersBySubscriptionExpiryBefore(6L).forEach(user -> {
            try {
                gdprComplianceService.deleteUser(user, false);
            } catch (Exception e) {
                log.error("Error while deleting the user with pseudo {}: {}", user.getPseudo(), e.getMessage());
            }
        });
    }

    // On the 15-20th of Dec at 2am
    @Scheduled(cron = "0 0 2 15-20 DEC *")
    public void cancelAllSubscriptions() {
        String voidPurchaseToken = "void";
        boolean hasNext = true;
        Pageable pageRequest = PageRequest.of(0, 50);

        while (hasNext) {
            Page<User> userPage = userDao.listUsers(null, pageRequest);

            for (User user : userPage.getContent()) {
                if (user.isSubscription() && !voidPurchaseToken.equals(user.getPurchaseToken()) && user.getSubscriptionExpiryAt() > 0L) {
                    try {
                        subscriptionService.cancelUserSubscription(user.getPurchaseToken());
                        user.setEnabled(false);
                        user.setSubscription(false);
                        user.setPurchaseToken(voidPurchaseToken);
                        user.setSubscriptionExpiryAt(0L);
                        userDao.save(user);
                    } catch (Exception e) {
                        log.error("Error while canceling the user with pseudo {}: {}", user.getPseudo(), e.getMessage());
                    }
                }
            }

            hasNext = userPage.hasNext();
            pageRequest = userPage.nextPageable();
        }
    }
}
