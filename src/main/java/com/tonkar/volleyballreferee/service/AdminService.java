package com.tonkar.volleyballreferee.service;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<User> listUsers(String filter, Pageable pageable);

    SubscriptionPurchase getUserSubscription(String userId);

    void refreshUserSubscription(String userId);

    void updateUserSubscription(String userId, String purchaseToken);
}
