package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GdprComplianceService {

    private final GameService         gameService;
    private final LeagueService       leagueService;
    private final TeamService         teamService;
    private final RulesService        rulesService;
    private final UserService         userService;
    private final SubscriptionService subscriptionService;
    private final EmailService        emailService;

    public void deleteUser(User user, boolean requestedByUser) {
        log.info("Deleting user {} {}", user.getId(), user.getEmail());
        gameService.deleteAllGames(user);
        teamService.deleteAllTeams(user);
        rulesService.deleteAllRules(user);
        leagueService.deleteAllLeagues(user);
        userService.deleteUser(user);

        if (requestedByUser) {
            if (user.isSubscription() && !user.isAccountNonExpired()) {
                subscriptionService.cancelUserSubscription(user.getPurchaseToken());
            }
            emailService.sendUserDeletedNotificationEmail(user);
        } else {
            emailService.sendInactiveUserDeletedNotificationEmail(user);
        }
    }
}
