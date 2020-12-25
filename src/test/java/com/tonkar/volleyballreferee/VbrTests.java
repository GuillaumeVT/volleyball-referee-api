package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.UserSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@ActiveProfiles("test")
public class VbrTests {

    @LocalServerPort
    private int port;

    final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${server.servlet.context-path}")
    String contextPath;

    @Value("${test.user1.email}")
    String testMail1;

    @Value("${test.user2.email}")
    String testMail2;

    @Value("${test.user1.pseudo}")
    String testUserPseudo1;

    @Value("${test.user2.pseudo}")
    String testUserPseudo2;

    @Value("${test.user1.purchase-token}")
    String testPurchaseToken1;

    @Value("${test.user2.purchase-token}")
    String testPurchaseToken2;

    String testUserToken1;
    String testUserToken2;
    String testUserInvalidToken = "invalid";

    UserSummary testUser1;
    UserSummary testUser2;

    String testPassword = "Password1234+";

    @BeforeEach
    public void setUp() {
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(FriendRequest.class);
        mongoTemplate.dropCollection(Rules.class);
        mongoTemplate.dropCollection(Team.class);
        mongoTemplate.dropCollection(League.class);
        mongoTemplate.dropCollection(Game.class);
        mongoTemplate.dropCollection(PasswordReset.class);
    }

    void createUser1() {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPseudo(testUserPseudo1);
        user.setEmail(testMail1);
        user.setPassword(testPassword);
        user.setPurchaseToken(testPurchaseToken1);
        user.setSubscriptionExpiryAt(4133980799000L);
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setFailedAuthentication(new User.FailedAuthentication());

        ResponseEntity<UserToken> response = restTemplate.postForEntity(urlOf("/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());
        testUserToken1 = response.getBody().getToken();
        testUser1 = response.getBody().getUser();
    }

    void createUser2() {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPseudo(testUserPseudo2);
        user.setEmail(testMail2);
        user.setPassword(testPassword);
        user.setPurchaseToken(testPurchaseToken2);
        user.setSubscriptionExpiryAt(4133980799000L);
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setFailedAuthentication(new User.FailedAuthentication());

        ResponseEntity<UserToken> response = restTemplate.postForEntity(urlOf("/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());
        testUserToken2 = response.getBody().getToken();
        testUser2 = response.getBody().getUser();
    }

    String urlOf(String apiUrl) {
        return String.format("http://localhost:%d%s%s", port, contextPath, apiUrl);
    }

    private HttpHeaders headersWithAuth(String testUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", String.format("Bearer %s", testUser));
        return headers;
    }

    private HttpHeaders headersWithoutAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    HttpEntity<String> emptyPayloadWithAuth(String testUser) {
        return new HttpEntity<>(headersWithAuth(testUser));
    }

    HttpEntity<String> emptyPayloadWithoutAuth() {
        return new HttpEntity<>(headersWithoutAuth());
    }

    HttpEntity<?> payloadWithAuth(String testUser, Object body) {
        return new HttpEntity<>(body, headersWithAuth(testUser));
    }

    HttpEntity<?> payloadWithoutAuth(Object body) {
        return new HttpEntity<>(body, headersWithoutAuth());
    }

}
