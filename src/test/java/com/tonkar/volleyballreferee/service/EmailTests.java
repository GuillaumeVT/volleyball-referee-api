package com.tonkar.volleyballreferee.service;

import com.github.javafaker.Faker;
import com.tonkar.volleyballreferee.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@ActiveProfiles("test")
public class EmailTests {

    @Value("${test.user.email}")
    String testMail;

    @Test
    public void test_emails_userCreated(@Autowired EmailService emailService) {
        User user = generateUser(testMail);

        emailService.sendUserCreatedNotificationEmail(user);
    }

    @Test
    public void test_emails_passwordReset(@Autowired EmailService emailService) {
        emailService.sendPasswordResetEmail(testMail, UUID.randomUUID());
    }

    @Test
    public void test_emails_passwordUpdated(@Autowired EmailService emailService) {
        User user = generateUser(testMail);

        emailService.sendPasswordUpdatedNotificationEmail(user);
    }

    @Test
    public void test_emails_friendRequested(@Autowired EmailService emailService) {
        final var faker = new Faker(Locale.ENGLISH);
        User senderUser = generateUser(faker.internet().safeEmailAddress());
        User receiverUser = generateUser(testMail);

        emailService.sendFriendRequestEmail(senderUser, receiverUser);
    }

    @Test
    public void test_emails_friendAccepted(@Autowired EmailService emailService) {
        final var faker = new Faker(Locale.ENGLISH);
        User acceptingUser = generateUser(faker.internet().safeEmailAddress());
        User senderUser = generateUser(testMail);

        emailService.sendFriendRequestEmail(acceptingUser, senderUser);
    }

    private User generateUser(String email) {
        final var faker = new Faker(Locale.ENGLISH);
        final var now = LocalDateTime.now();
        final var nowMillis = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        final var expiryMillis = now.plusYears(50).toInstant(ZoneOffset.UTC).toEpochMilli();

        var user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPseudo(faker.name().firstName());
        user.setEmail(email == null ? faker.internet().safeEmailAddress() : email);
        user.setPassword("Password1234+");
        user.setPurchaseToken(faker.finance().iban());
        user.setSubscriptionExpiryAt(expiryMillis);
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(nowMillis);
        user.setLastLoginAt(nowMillis);
        user.setFailedAuthentication(new User.FailedAuthentication());

        return user;
    }
}
