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
public class AdminServiceImpl implements  AdminService {

    private final UserService userService;
    private final UserDao     userDao;

    @Override
    public Page<User> listUsers(String filter, Pageable pageable) {
        return userDao.listUsers(filter, pageable);
    }

    @Override
    public SubscriptionPurchase getUserSubscription(String userId) {
        User user = userService.getUser(userId);
        return userService.getUserSubscription(user.getPurchaseToken());
    }

    @Override
    public void refreshUserSubscription(String userId) {
        User user = userService.getUser(userId);
        userService.refreshSubscriptionPurchaseToken(user.getPurchaseToken());
    }

    @Override
    public void updateUserSubscription(String userId, String purchaseToken) {
        userDao.updateSubscriptionPurchaseToken(userId, purchaseToken, 0L);
        userService.refreshSubscriptionPurchaseToken(purchaseToken);
    }
}
