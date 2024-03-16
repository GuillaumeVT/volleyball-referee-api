package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.configuration.VbrTestConfiguration;
import com.tonkar.volleyballreferee.entity.User;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(VbrTestConfiguration.class)
@ActiveProfiles("test")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class EmailTests {

    private final String testTargetEmail;

    private final EmailService emailService;

    private final Faker faker;

    private final VbrTestConfiguration.Sandbox sandbox;

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.2").withReuse(true);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    public EmailTests(@Autowired EmailService emailService,
                      @Autowired Faker faker,
                      @Autowired VbrTestConfiguration.Sandbox sandbox,
                      @Value("${test.user.email}") String testTargetEmail) {
        this.emailService = emailService;
        this.faker = faker;
        this.sandbox = sandbox;
        this.testTargetEmail = testTargetEmail;
    }

    @Test
    void test_emails_userCreated() {
        User user = sandbox.generateUser(testTargetEmail);

        emailService.sendUserCreatedNotificationEmail(user);
    }

    @Test
    void test_emails_userDeleted_requested() {
        User user = sandbox.generateUser(testTargetEmail);

        emailService.sendUserDeletedNotificationEmail(user);
    }

    @Test
    void test_emails_userDeleted_inactive() {
        User user = sandbox.generateUser(testTargetEmail);

        emailService.sendInactiveUserDeletedNotificationEmail(user);
    }

    @Test
    void test_emails_passwordReset() {
        emailService.sendPasswordResetEmail(testTargetEmail, UUID.randomUUID());
    }

    @Test
    void test_emails_passwordUpdated() {
        User user = sandbox.generateUser(testTargetEmail);

        emailService.sendPasswordUpdatedNotificationEmail(user);
    }

    @Test
    void test_emails_pseudoUpdated() {
        User user = sandbox.generateUser(testTargetEmail);

        emailService.sendPseudoUpdatedNotificationEmail(user);
    }

    @Test
    void test_emails_friendRequested() {
        User senderUser = sandbox.generateUser(faker.internet().safeEmailAddress());
        User receiverUser = sandbox.generateUser(testTargetEmail);

        emailService.sendFriendRequestEmail(senderUser, receiverUser);
    }

    @Test
    void test_emails_friendAccepted() {
        User acceptingUser = sandbox.generateUser(faker.internet().safeEmailAddress());
        User senderUser = sandbox.generateUser(testTargetEmail);

        emailService.sendFriendRequestEmail(acceptingUser, senderUser);
    }
}
