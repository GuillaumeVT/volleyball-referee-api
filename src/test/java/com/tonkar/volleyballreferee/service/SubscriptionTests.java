package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.configuration.VbrTestConfiguration;
import com.tonkar.volleyballreferee.entity.User;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(VbrTestConfiguration.class)
@ActiveProfiles("test")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class SubscriptionTests {

    @MockBean
    private EmailService emailService;

    @Value("${test.user.purchase-token}")
    private String testPurchaseToken;

    @PostConstruct
    void init() {
        Mockito
                .doNothing()
                .when(emailService)
                .sendUserCreatedNotificationEmail(Mockito.any(User.class));
        Mockito
                .doNothing()
                .when(emailService)
                .sendPasswordResetEmail(Mockito.anyString(), Mockito.any(UUID.class));
        Mockito
                .doNothing()
                .when(emailService)
                .sendPasswordUpdatedNotificationEmail(Mockito.any(User.class));
        Mockito
                .doNothing()
                .when(emailService)
                .sendFriendRequestEmail(Mockito.any(User.class), Mockito.any(User.class));
        Mockito
                .doNothing()
                .when(emailService)
                .sendAcceptFriendRequestEmail(Mockito.any(User.class), Mockito.any(User.class));
    }

    @Test
    void test_subscriptions_get(@Autowired SubscriptionService subscriptionService) {
        // The purchase token is not a subscription
        assertThrows(ResponseStatusException.class, () -> subscriptionService.getUserSubscription(testPurchaseToken));
    }

    @Test
    void test_subscriptions_validate(@Autowired SubscriptionService subscriptionService) {
        // WHEN
        var subscription = subscriptionService.validatePurchaseToken(testPurchaseToken);

        // THEN
        // The purchase token is not a subscription
        assertNull(subscription);
    }

    @Test
    void test_subscriptions_refresh(@Autowired SubscriptionService subscriptionService) {
        // The purchase token is not a subscription
        assertThrows(ResponseStatusException.class, () -> subscriptionService.refreshSubscriptionPurchaseToken(testPurchaseToken));
    }
}
