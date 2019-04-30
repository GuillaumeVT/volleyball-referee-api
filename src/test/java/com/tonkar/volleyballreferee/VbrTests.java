package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.AuthenticationProvider;
import com.tonkar.volleyballreferee.service.UserAuthenticationService;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@ActiveProfiles("test")
public class VbrTests {

    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();

    @MockBean
    private UserAuthenticationService userAuthenticationService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void setUp() {
        mongoTemplate.dropCollection(com.tonkar.volleyballreferee.entity.User.class);
        mongoTemplate.dropCollection(FriendRequest.class);
        mongoTemplate.dropCollection(Rules.class);
        mongoTemplate.dropCollection(Team.class);
        mongoTemplate.dropCollection(League.class);
        mongoTemplate.dropCollection(Game.class);

        Mockito
                .when(userAuthenticationService.getUserId(AuthenticationProvider.GOOGLE, testUser1Auth))
                .thenReturn(Optional.of(testUser1Id));
        Mockito
                .when(userAuthenticationService.getUserId(AuthenticationProvider.GOOGLE, testUser2Auth))
                .thenReturn(Optional.of(testUser2Id));
        Mockito
                .when(userAuthenticationService.getUserId(AuthenticationProvider.GOOGLE, testUserInvalidAuth))
                .thenReturn(Optional.empty());
    }

    String testUser1Auth       = "user1";
    String testUser2Auth       = "user2";
    String testUserInvalidAuth = "invalid";

    String testUser1Id = "user1@google";
    String testUser2Id = "user2@google";

    @Value("${vbr.auth.signUpKey}")
    String vbrSignUpKey;

    String urlOf(String apiUrl) {
        return "http://localhost:" + port + apiUrl;
    }

    private HttpHeaders headersWithAuth(String testUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", String.format("Bearer %s", testUser));
        headers.add("AuthenticationProvider", "google");
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
