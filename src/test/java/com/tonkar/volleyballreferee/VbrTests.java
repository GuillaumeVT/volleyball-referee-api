package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.UserSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.Before;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@ActiveProfiles("test")
public class VbrTests {

    static {
        System.setProperty("jasypt.encryptor.password", System.getenv("JASYPT_KEY"));
    }

    @LocalServerPort
    private int port;

    final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${vbr.android.app.purchase.mail1}")
    String testMail1;

    @Value("${vbr.android.app.purchase.mail2}")
    String testMail2;

    @Value("${vbr.android.app.purchase.pseudo1}")
    String testUserPseudo1;

    @Value("${vbr.android.app.purchase.pseudo2}")
    String testUserPseudo2;

    @Value("${vbr.android.app.purchase.token1}")
    String testPurchaseToken1;

    @Value("${vbr.android.app.purchase.token2}")
    String testPurchaseToken2;

    String testUserToken1;
    String testUserToken2;
    String testUserInvalidToken = "invalid";

    UserSummary testUser1;
    UserSummary testUser2;

    String testPassword = "Password1234+";

    @Before
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
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setFailedAuthentication(new User.FailedAuthentication());

        ResponseEntity<UserToken> response = restTemplate.postForEntity(urlOf("/api/v3.1/public/users"), payloadWithoutAuth(user), UserToken.class);
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
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setFailedAuthentication(new User.FailedAuthentication());

        ResponseEntity<UserToken> response = restTemplate.postForEntity(urlOf("/api/v3.1/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());
        testUserToken2 = response.getBody().getToken();
        testUser2 = response.getBody().getUser();
    }

    String urlOf(String apiUrl) {
        return "http://localhost:" + port + apiUrl;
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
