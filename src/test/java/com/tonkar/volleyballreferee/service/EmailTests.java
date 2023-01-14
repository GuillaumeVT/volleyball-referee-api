package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.configuration.VbrTestConfiguration;
import com.tonkar.volleyballreferee.entity.User;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(VbrTestConfiguration.class)
@ActiveProfiles("test")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class EmailTests {

    @Value("${test.user.email}")
    private String testTargetEmail;

    private final Faker                        faker;
    private final VbrTestConfiguration.Sandbox sandbox;

    public EmailTests(@Autowired Faker faker, @Autowired VbrTestConfiguration.Sandbox sandbox) {
        this.faker = faker;
        this.sandbox = sandbox;
    }

    @Test
    public void test_emails_userCreated(@Autowired EmailService emailService) {
        User user = sandbox.generateUser(testTargetEmail);

        emailService.sendUserCreatedNotificationEmail(user);
    }

    @Test
    public void test_emails_passwordReset(@Autowired EmailService emailService) {
        emailService.sendPasswordResetEmail(testTargetEmail, UUID.randomUUID());
    }

    @Test
    public void test_emails_passwordUpdated(@Autowired EmailService emailService) {
        User user = sandbox.generateUser(testTargetEmail);

        emailService.sendPasswordUpdatedNotificationEmail(user);
    }

    @Test
    public void test_emails_pseudoUpdated(@Autowired EmailService emailService) {
        User user = sandbox.generateUser(testTargetEmail);

        emailService.sendPseudoUpdatedNotificationEmail(user);
    }

    @Test
    public void test_emails_friendRequested(@Autowired EmailService emailService) {
        User senderUser = sandbox.generateUser(faker.internet().safeEmailAddress());
        User receiverUser = sandbox.generateUser(testTargetEmail);

        emailService.sendFriendRequestEmail(senderUser, receiverUser);
    }

    @Test
    public void test_emails_friendAccepted(@Autowired EmailService emailService) {
        User acceptingUser = sandbox.generateUser(faker.internet().safeEmailAddress());
        User senderUser = sandbox.generateUser(testTargetEmail);

        emailService.sendFriendRequestEmail(acceptingUser, senderUser);
    }
}
