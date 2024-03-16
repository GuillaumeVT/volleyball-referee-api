package com.tonkar.volleyballreferee.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.*;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.tonkar.volleyballreferee.dao.UserDao;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionService {

    private final UserDao userDao;

    @Value("${vbr.android.app.packageName}")
    private String androidPackageName;

    @Value("${vbr.android.app.billing.sku-purchase}")
    private String androidPurchaseSku;

    @Value("${vbr.android.app.billing.sku-subscription}")
    private String androidSubscriptionSku;

    @Value("${vbr.android.app.billing.credential}")
    private String androidCredential;

    private AndroidPublisher.Purchases.Subscriptions subscriptions;
    private AndroidPublisher.Purchases.Products      products;

    @PostConstruct
    public void init() {
        try (InputStream stream = new ByteArrayInputStream(androidCredential.getBytes(StandardCharsets.UTF_8))) {
            var credentials = ServiceAccountCredentials
                    .fromStream(stream)
                    .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
            var requestInitializer = new HttpCredentialsAdapter(credentials);
            var publisher = new AndroidPublisher.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
                                                         requestInitializer).setApplicationName(androidPackageName).build();
            subscriptions = publisher.purchases().subscriptions();
            products = publisher.purchases().products();
        } catch (IOException | GeneralSecurityException e) {
            log.error(e.getMessage());
            throw new IllegalStateException("Failed to start the subscription service");
        }
    }

    public SubscriptionPurchase getUserSubscription(String purchaseToken) {
        try {
            return subscriptions.get(androidPackageName, androidSubscriptionSku, purchaseToken).execute();
        } catch (IOException e) {
            printGoogleApiError(purchaseToken, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find the purchase token %s", purchaseToken));
        }
    }

    public void refreshSubscriptionPurchaseToken(String purchaseToken) {
        // A subscription  may contain a linked purchase token which is the previous purchase token for this user.
        // Browse this chain of purchase tokens to find the user and when found, update their purchase token and expiry
        var optionalUser = userDao.findUserByPurchaseToken(purchaseToken);
        var subscription = getUserSubscription(purchaseToken);
        final long subscriptionExpiryAt;

        if (subscription.getAutoRenewing() && (subscription.getPaymentState() == 0)) {
            subscriptionExpiryAt = subscription.getExpiryTimeMillis() + 604800000L; // Users have auto renew and the payment is pending, give 7 extra days of access
        } else {
            subscriptionExpiryAt = subscription.getExpiryTimeMillis();
        }

        String linkedPurchaseToken = purchaseToken;

        while (optionalUser.isEmpty() && linkedPurchaseToken != null) {
            SubscriptionPurchase linkedSubscription = getUserSubscription(linkedPurchaseToken);
            optionalUser = userDao.findUserByPurchaseToken(linkedPurchaseToken);
            linkedPurchaseToken = linkedSubscription.getLinkedPurchaseToken();
        }

        // Store the latest purchase token and the subscription expiry date
        optionalUser.ifPresent(userSummary -> {
            log.info("Found the user {} ({}) from the linked purchase tokens, store new token {} with expiry {}", userSummary.id(),
                     userSummary.pseudo(), purchaseToken, subscriptionExpiryAt);
            userDao.updateSubscriptionPurchaseToken(userSummary.id(), purchaseToken, subscriptionExpiryAt);
        });
    }

    public SubscriptionPurchase validatePurchaseToken(String purchaseToken) {
        try {
            return getUserSubscription(purchaseToken);
        } catch (ResponseStatusException e) {
            if (isValidPurchaseToken(purchaseToken)) {
                // The purchase token is not a subscription but is a valid legacy lifetime purchase
                return null;
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                  String.format("User provided an invalid purchase token %s", purchaseToken));
            }
        }
    }

    private boolean isValidPurchaseToken(String purchaseToken) {
        boolean valid = false;

        try {
            products.get(androidPackageName, androidPurchaseSku, purchaseToken).execute();
            valid = true;
        } catch (IOException e) {
            printGoogleApiError(purchaseToken, e);
        }

        return valid;
    }

    public void cancelUserSubscription(String purchaseToken) {
        try {
            subscriptions.cancel(androidPackageName, androidSubscriptionSku, purchaseToken).execute();
        } catch (IOException e) {
            printGoogleApiError(purchaseToken, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find the purchase token %s", purchaseToken));
        }
    }

    private void printGoogleApiError(String purchaseToken, IOException exception) {
        if (exception instanceof GoogleJsonResponseException googleJsonResponseException) {
            log.error("Error while retrieving the purchase token {}: {}", purchaseToken,
                      googleJsonResponseException.getDetails().getOrDefault("message", googleJsonResponseException.getContent()));
        } else {
            log.error(exception.getMessage());
        }
    }
}
