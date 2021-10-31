package com.tonkar.volleyballreferee;

import com.github.javafaker.Faker;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.EmailService;
import com.tonkar.volleyballreferee.service.FriendService;
import com.tonkar.volleyballreferee.service.SubscriptionService;
import com.tonkar.volleyballreferee.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@ActiveProfiles("test")
public class VbrMockedTests {

    @LocalServerPort
    private int port;

    protected TestRestTemplate restTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    protected Faker faker;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendService friendService;

    protected String invalidPurchaseToken = "invalidPurchaseToken";

    @PostConstruct
    public void init() {
        var restTemplateBuilder = new RestTemplateBuilder()
                .rootUri(String.format("http://localhost:%d%s", port, contextPath))
                .setReadTimeout(Duration.ofMillis(20000L));
        restTemplate = new TestRestTemplate(restTemplateBuilder, null, null);

        faker = new Faker(Locale.ENGLISH);

        SubscriptionPurchase subscriptionPurchase = new SubscriptionPurchase();
        subscriptionPurchase.setAutoRenewing(true);
        subscriptionPurchase.setPaymentState(0);
        subscriptionPurchase.setExpiryTimeMillis(LocalDateTime.of(2100, 12, 31, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli());

        Mockito
                .when(subscriptionService.getUserSubscription(Mockito.anyString()))
                .thenReturn(subscriptionPurchase);
        Mockito
                .when(subscriptionService.getUserSubscription(invalidPurchaseToken))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        Mockito
                .doNothing()
                .when(subscriptionService)
                .refreshSubscriptionPurchaseToken(Mockito.anyString());
        Mockito
                .doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(subscriptionService)
                .refreshSubscriptionPurchaseToken(invalidPurchaseToken);
        Mockito
                .when(subscriptionService.validatePurchaseToken(Mockito.anyString()))
                .thenReturn(subscriptionPurchase);
        Mockito
                .when(subscriptionService.validatePurchaseToken(invalidPurchaseToken))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

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

    @BeforeEach
    public void setUp(@Autowired MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(FriendRequest.class);
        mongoTemplate.dropCollection(Rules.class);
        mongoTemplate.dropCollection(Team.class);
        mongoTemplate.dropCollection(League.class);
        mongoTemplate.dropCollection(Game.class);
        mongoTemplate.dropCollection(PasswordReset.class);
    }

    private HttpHeaders headersWithAuth(String testUser) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", String.format("Bearer %s", testUser));
        return headers;
    }

    private HttpHeaders headersWithoutAuth() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpEntity<String> emptyPayloadWithAuth(String testUser) {
        return new HttpEntity<>(headersWithAuth(testUser));
    }

    protected HttpEntity<String> emptyPayloadWithoutAuth() {
        return new HttpEntity<>(headersWithoutAuth());
    }

    protected HttpEntity<?> payloadWithAuth(String testUser, Object body) {
        return new HttpEntity<>(body, headersWithAuth(testUser));
    }

    protected HttpEntity<?> payloadWithoutAuth(Object body) {
        return new HttpEntity<>(body, headersWithoutAuth());
    }

    protected User generateUser(String email) {
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

    protected UserToken createUser() {
        return createUser(null);
    }

    protected UserToken createUser(String email) {
        return userService.createUser(generateUser(email));
    }

    protected User getUser(String userId) {
        return userService.getUser(userId);
    }

    protected void addFriend(User user1, User user2) {
        UUID friendRequestId = friendService.sendFriendRequest(user1, user2.getPseudo());
        friendService.acceptFriendRequest(user2, friendRequestId);
    }
}
