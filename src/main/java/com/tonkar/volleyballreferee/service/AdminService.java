package com.tonkar.volleyballreferee.service;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService         userService;
    private final SubscriptionService subscriptionService;
    private final UserDao             userDao;

    public Page<User> listUsers(String filter, Pageable pageable) {
        return userDao.listUsers(filter, pageable);
    }

    public SubscriptionPurchase getUserSubscription(String userId) {
        User user = userService.getUser(userId);
        return subscriptionService.getUserSubscription(user.getPurchaseToken());
    }

    public void refreshUserSubscription(String userId) {
        User user = userService.getUser(userId);
        if (user.isSubscription()) {
            subscriptionService.refreshSubscriptionPurchaseToken(user.getPurchaseToken());
        }
    }

    public void updateUserSubscription(String userId, String purchaseToken) {
        User user = userService.getUser(userId);
        if (user.isSubscription()) {
            userDao.updateSubscriptionPurchaseToken(user.getId(), purchaseToken, 0L);
            subscriptionService.refreshSubscriptionPurchaseToken(purchaseToken);
        }
    }
}
