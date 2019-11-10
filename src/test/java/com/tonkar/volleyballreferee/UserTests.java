package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.PasswordReset;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.repository.PasswordResetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class UserTests extends VbrTests {

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Test
    public void testNotAuthenticated() {
        User user = new User();

        ResponseEntity<String> postUserResponse = restTemplate.postForEntity(urlOf("/api/v3.2/public/users"), payloadWithoutAuth(user), String.class);
        assertNotEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        ResponseEntity<FriendsAndRequests> friendsAndRequestsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), FriendsAndRequests.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendsAndRequestsResponse.getStatusCode());

        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<FriendRequest>> getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getFriendsResponse.getStatusCode());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getFriendsResponse.getStatusCode());

        ResponseEntity<Count> getFriendsCountResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getFriendsCountResponse.getStatusCode());

        ResponseEntity<String> friendResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/request/anyPseudo"), HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendResponse.getStatusCode());

        friendResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/accept/anyId"), HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendResponse.getStatusCode());

        friendResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/reject/anyId"), HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendResponse.getStatusCode());

        friendResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/remove/anyId"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendResponse.getStatusCode());
    }

    @Test
    public void testManageUsers() {
        ResponseEntity<String> responseStr = restTemplate.exchange(urlOf(String.format("/api/v3.2/public/users/%s", "Invalid purchase token")), HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseStr.getStatusCode());

        responseStr = restTemplate.exchange(urlOf(String.format("/api/v3.2/public/users/%s", testPurchaseToken1)), HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseStr.getStatusCode());

        // Invalid purchase token

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPseudo("Some pseudo");
        user.setEmail("user@email.com");
        user.setPassword(testPassword);
        user.setPurchaseToken("Invalid purchase token");
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setFailedAuthentication(new User.FailedAuthentication());

        ResponseEntity<UserToken> response = restTemplate.postForEntity(urlOf("/api/v3.2/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Invalid email address

        user.setPseudo(testUserPseudo1);
        user.setEmail("invalidemail.com");
        user.setPassword(testPassword);
        user.setPurchaseToken(testPurchaseToken1);

        response = restTemplate.postForEntity(urlOf("/api/v3.2/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertNotEquals(HttpStatus.CREATED, response.getStatusCode());

        // Create user

        user.setEmail(testMail1);

        response = restTemplate.postForEntity(urlOf("/api/v3.2/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());
        testUserToken1 = response.getBody().getToken();
        testUser1 = response.getBody().getUser();

        // Get user from purchase token

        ResponseEntity<UserSummary> userResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/public/users/%s", testPurchaseToken1)), HttpMethod.GET, null, UserSummary.class);
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        assertEquals(testUserPseudo1, userResponse.getBody().getPseudo());

        // User already exists

        response = restTemplate.postForEntity(urlOf("/api/v3.2/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        // User pseudo is taken

        user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPseudo(testUserPseudo1);
        user.setEmail(testMail2);
        user.setPassword(testPassword);
        user.setPurchaseToken(testPurchaseToken2);
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setFailedAuthentication(new User.FailedAuthentication());

        response = restTemplate.postForEntity(urlOf("/api/v3.2/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        // User email is taken

        user.setPseudo(testUserPseudo2);
        user.setEmail(testMail1);

        response = restTemplate.postForEntity(urlOf("/api/v3.2/public/users"), payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        // Sign in user

        response = restTemplate.postForEntity(urlOf("/api/v3.2/public/users/token"), payloadWithoutAuth(new EmailCredentials(testMail1, "Invalid password")), UserToken.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        response = restTemplate.postForEntity(urlOf("/api/v3.2/public/users/token"), payloadWithoutAuth(new EmailCredentials(testMail1, testPassword)), UserToken.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());

        // Update user password

        response = restTemplate.exchange(urlOf("/api/v3.2/users/password"), HttpMethod.PATCH, payloadWithAuth(testUserToken1, new UserPasswordUpdate(testPassword, "Invalid password")), UserToken.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(urlOf("/api/v3.2/users/password"), HttpMethod.PATCH, payloadWithAuth(testUserToken1, new UserPasswordUpdate("Invalid password", "NewPassword5678-")), UserToken.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        response = restTemplate.exchange(urlOf("/api/v3.2/users/password"), HttpMethod.PATCH, payloadWithAuth(testUserToken1, new UserPasswordUpdate(testPassword, "NewPassword5678-")), UserToken.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());

        // Recover user password

        responseStr = restTemplate.postForEntity(urlOf(String.format("/api/v3.2/public/users/password/recover/%s", testUser1.getEmail())), emptyPayloadWithoutAuth(), String.class);
        assertEquals(HttpStatus.OK, responseStr.getStatusCode());

        PasswordReset passwordReset = passwordResetRepository
                .findByUserId(testUser1.getId())
                .orElseThrow(() -> new RuntimeException("This must not fail"));

        responseStr = restTemplate.exchange(urlOf(String.format("/api/v3.2/public/users/password/follow/%s", UUID.randomUUID())), HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseStr.getStatusCode());

        responseStr = restTemplate.exchange(urlOf(String.format("/api/v3.2/public/users/password/follow/%s", passwordReset.getId())), HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.FOUND, responseStr.getStatusCode());

        List<String> location = responseStr.getHeaders().get("Location");
        assertNotNull(location);
        assertEquals(1, location.size());

        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(location.get(0)).build().getQueryParams();
        assertEquals(1, parameters.get("passwordResetId").size());
        assertEquals(passwordReset.getId(), UUID.fromString(parameters.get("passwordResetId").get(0)));

        response = restTemplate.postForEntity(urlOf(String.format("/api/v3.2/public/users/password/reset/%s", passwordReset.getId())), payloadWithoutAuth(new UserPassword("notStrongEnough")), UserToken.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.postForEntity(urlOf(String.format("/api/v3.2/public/users/password/reset/%s", passwordReset.getId())), payloadWithoutAuth(new UserPassword(testPassword)), UserToken.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testManageFriends() {
        createUser1();
        createUser2();

        // Can't request friend with self

        ResponseEntity<String> friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/users/friends/request/%s", testUser1.getPseudo())), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CONFLICT, friendResponse.getStatusCode());

        // Request friend

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/users/friends/request/%s", testUser2.getPseudo())), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        // Can't have 2+ same friend request at a time

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/users/friends/request/%s", testUser2.getPseudo())), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CONFLICT, friendResponse.getStatusCode());

        // Count received friend requests

        ResponseEntity<Count> getFriendsCountResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getFriendsCountResponse.getStatusCode());
        assertEquals(0L, getFriendsCountResponse.getBody().getCount());

        getFriendsCountResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), Count.class);
        assertEquals(HttpStatus.OK, getFriendsCountResponse.getStatusCode());
        assertEquals(1L, getFriendsCountResponse.getBody().getCount());

        // List received friend requests

        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<FriendRequest>> getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        // List sent friend requests

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        // List all friends and requests

        ResponseEntity<FriendsAndRequests> friendsAndRequestsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), FriendsAndRequests.class);
        assertEquals(HttpStatus.OK, friendsAndRequestsResponse.getStatusCode());

        // Reject friend request

        UUID friendRequestId =  getFriendsResponse.getBody().get(0).getId();
        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/users/friends/reject/%s", friendRequestId)), HttpMethod.POST, emptyPayloadWithAuth(testUserToken2), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        // Request friend

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/users/friends/request/%s", testUser2.getPseudo())), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        // Accept friend request

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        friendRequestId =  getFriendsResponse.getBody().get(0).getId();

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/users/friends/accept/%s", friendRequestId)), HttpMethod.POST, emptyPayloadWithAuth(testUserToken2), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        // Can't request when already friend

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/users/friends/request/%s", testUser2.getPseudo())), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.CONFLICT, friendResponse.getStatusCode());

        // Remove friend

        friendResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends/remove/anyId"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NOT_FOUND, friendResponse.getStatusCode());

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3.2/users/friends/remove/%s", testUser2.getId())), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());

        friendsAndRequestsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), FriendsAndRequests.class);
        assertEquals(HttpStatus.OK, friendsAndRequestsResponse.getStatusCode());
        assertEquals(0, friendsAndRequestsResponse.getBody().getFriends().size());

        friendsAndRequestsResponse = restTemplate.exchange(urlOf("/api/v3.2/users/friends"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), FriendsAndRequests.class);
        assertEquals(HttpStatus.OK, friendsAndRequestsResponse.getStatusCode());
        assertEquals(0, friendsAndRequestsResponse.getBody().getFriends().size());
    }

}
