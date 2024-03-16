package com.tonkar.volleyballreferee;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.configuration.VbrTestConfiguration;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.*;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.*;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(VbrTestConfiguration.class)
@ActiveProfiles("test")
public class VbrMockedTests {

    @LocalServerPort
    private int port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    protected WebTestClient webTestClient;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private EmailService emailService;

    @Autowired
    protected VbrTestConfiguration.Sandbox sandbox;

    @Autowired
    protected Faker faker;

    protected final String invalidPurchaseToken = "invalidPurchaseToken";

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.2").withReuse(true);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @PostConstruct
    public void init() {
        String baseUrl = String.format("http://localhost:%d%s", port, contextPath);
        webTestClient = WebTestClient.bindToServer().baseUrl(baseUrl).responseTimeout(Duration.ofSeconds(20L)).build();

        SubscriptionPurchase subscriptionPurchase = new SubscriptionPurchase();
        subscriptionPurchase.setAutoRenewing(true);
        subscriptionPurchase.setPaymentState(0);
        subscriptionPurchase.setExpiryTimeMillis(LocalDateTime.of(2100, 12, 31, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli());

        Mockito.doReturn(subscriptionPurchase).when(subscriptionService).getUserSubscription(Mockito.anyString());
        Mockito
                .doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(subscriptionService)
                .getUserSubscription(invalidPurchaseToken);
        Mockito.doNothing().when(subscriptionService).refreshSubscriptionPurchaseToken(Mockito.anyString());
        Mockito
                .doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(subscriptionService)
                .refreshSubscriptionPurchaseToken(invalidPurchaseToken);
        Mockito.doReturn(subscriptionPurchase).when(subscriptionService).validatePurchaseToken(Mockito.anyString());
        Mockito
                .doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(subscriptionService)
                .validatePurchaseToken(invalidPurchaseToken);
        Mockito.doNothing().when(subscriptionService).cancelUserSubscription(Mockito.anyString());
    }

    @BeforeEach
    public void setUp(@Autowired MongoTemplate mongoTemplate) {
        if (mongoTemplate.collectionExists(User.class)) {
            mongoTemplate.dropCollection(User.class);
        }
        if (mongoTemplate.collectionExists(FriendRequest.class)) {
            mongoTemplate.dropCollection(FriendRequest.class);
        }
        if (mongoTemplate.collectionExists(Rules.class)) {
            mongoTemplate.dropCollection(Rules.class);
        }
        if (mongoTemplate.collectionExists(Team.class)) {
            mongoTemplate.dropCollection(Team.class);
        }
        if (mongoTemplate.collectionExists(League.class)) {
            mongoTemplate.dropCollection(League.class);
        }
        if (mongoTemplate.collectionExists(Game.class)) {
            mongoTemplate.dropCollection(Game.class);
        }
        if (mongoTemplate.collectionExists(PasswordReset.class)) {
            mongoTemplate.dropCollection(PasswordReset.class);
        }
    }

    protected String bearer(String token) {
        return "Bearer %s".formatted(token);
    }
}
