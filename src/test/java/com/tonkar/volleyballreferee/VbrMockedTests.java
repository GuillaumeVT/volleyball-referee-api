package com.tonkar.volleyballreferee;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.configuration.VbrTestConfiguration;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.EmailService;
import com.tonkar.volleyballreferee.service.SubscriptionService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
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
import java.util.UUID;

@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(VbrTestConfiguration.class)
@ActiveProfiles("test")
public class VbrMockedTests {

    @LocalServerPort
    private int port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    protected TestRestTemplate restTemplate;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private EmailService emailService;

    @Autowired
    protected VbrTestConfiguration.Sandbox sandbox;

    @Autowired
    protected Faker faker;

    protected final String invalidPurchaseToken = "invalidPurchaseToken";

    @PostConstruct
    public void init() {
        var restTemplateBuilder = new RestTemplateBuilder()
                .rootUri(String.format("http://localhost:%d%s", port, contextPath))
                .setReadTimeout(Duration.ofMillis(20000L));
        restTemplate = new TestRestTemplate(restTemplateBuilder, null, null);

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
}
