package com.tonkar.volleyballreferee.service;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;

public interface SubscriptionService {

    SubscriptionPurchase getUserSubscription(String purchaseToken);

    void refreshSubscriptionPurchaseToken(String purchaseToken);

    SubscriptionPurchase validatePurchaseToken(String purchaseToken);
}
