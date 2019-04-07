package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.security.User;
import com.tonkar.volleyballreferee.service.UserAuthenticationService;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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

        Optional<User> validTestUser1 = Optional.of(new User(testUser1Auth, User.AuthenticationProvider.GOOGLE));
        Optional<User> validTestUser2 = Optional.of(new User(testUser2Auth, User.AuthenticationProvider.GOOGLE));
        Optional<User> invalidTestUser = Optional.empty();

        Mockito.when(userAuthenticationService.getUser(User.AuthenticationProvider.GOOGLE, testUser1Auth)).thenReturn(validTestUser1);
        Mockito.when(userAuthenticationService.getUser(User.AuthenticationProvider.GOOGLE, testUser2Auth)).thenReturn(validTestUser2);
        Mockito.when(userAuthenticationService.getUser(User.AuthenticationProvider.GOOGLE, testUserInvalidAuth)).thenReturn(invalidTestUser);
    }

    String testUser1Auth       = "user1";
    String testUser2Auth       = "user2";
    String testUserInvalidAuth = "invalid";

    String testUser1Id = "user1@google";
    String testUser2Id = "user2@google";

    String urlOf(String apiUrl) {
        return "http://localhost:" + port + apiUrl;
    }

    HttpHeaders headersWithAuth(String testUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", String.format("Bearer %s", testUser));
        headers.add("AuthenticationProvider", "google");
        return headers;
    }

    HttpHeaders headersWithoutAuth() {
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

}
