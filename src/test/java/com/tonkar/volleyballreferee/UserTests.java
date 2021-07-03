package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dao.PasswordResetDao;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.PasswordReset;
import com.tonkar.volleyballreferee.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class UserTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/users/friends", HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/received/count", HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/request/anyPseudo", HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/accept/anyId", HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/reject/anyId", HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/remove/anyId", HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void testManageUsers(@Autowired PasswordResetDao passwordResetDao) {
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/public/users/%s", "Invalid purchase token"), HttpMethod.GET, null, ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/public/users/%s", testPurchaseToken1), HttpMethod.GET, null, ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());

        // Invalid purchase token

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPseudo("Some pseudo");
        user.setEmail("user@email.com");
        user.setPassword(testPassword);
        user.setPurchaseToken("Invalid purchase token");
        user.setSubscriptionExpiryAt(4133980799000L);
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setFailedAuthentication(new User.FailedAuthentication());

        errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        // Invalid email address

        user.setPseudo(testUserPseudo1);
        user.setEmail("invalidemail.com");
        user.setPassword(testPassword);
        user.setPurchaseToken(testPurchaseToken1);

        ResponseEntity<UserToken> response = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), UserToken.class);
        assertNotEquals(HttpStatus.CREATED, response.getStatusCode());

        // Create user

        user.setEmail(testMail1);

        response = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), UserToken.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());
        testUserToken1 = response.getBody().getToken();
        testUser1 = response.getBody().getUser();

        // Get user from purchase token

        ResponseEntity<UserSummary> userResponse = restTemplate.exchange(String.format("/public/users/%s", testPurchaseToken1), HttpMethod.GET, null, UserSummary.class);
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        assertEquals(testUserPseudo1, userResponse.getBody().getPseudo());

        // User already exists

        errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        // User pseudo is taken

        user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPseudo(testUserPseudo1);
        user.setEmail(testMail2);
        user.setPassword(testPassword);
        user.setPurchaseToken(testPurchaseToken2);
        user.setSubscriptionExpiryAt(4133980799000L);
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.setFailedAuthentication(new User.FailedAuthentication());

        errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        // User email is taken

        user.setPseudo(testUserPseudo2);
        user.setEmail(testMail1);

        errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        // Sign in user

        errorResponse = restTemplate.postForEntity("/public/users/token", payloadWithoutAuth(new EmailCredentials(testMail1, "Invalid password")), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        response = restTemplate.postForEntity("/public/users/token", payloadWithoutAuth(new EmailCredentials(testMail1, testPassword)), UserToken.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());

        // Update user password

        errorResponse = restTemplate.exchange("/users/password", HttpMethod.PATCH, payloadWithAuth(testUserToken1, new UserPasswordUpdate(testPassword, "Invalid password")), ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/password", HttpMethod.PATCH, payloadWithAuth(testUserToken1, new UserPasswordUpdate("Invalid password", "NewPassword5678-")), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        response = restTemplate.exchange("/users/password", HttpMethod.PATCH, payloadWithAuth(testUserToken1, new UserPasswordUpdate(testPassword, "NewPassword5678-")), UserToken.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());

        // Recover user password

        errorResponse = restTemplate.postForEntity(String.format("/public/users/password/recover/%s", testUser1.getEmail()), emptyPayloadWithoutAuth(), ErrorResponse.class);
        assertEquals(HttpStatus.OK, errorResponse.getStatusCode());

        PasswordReset passwordReset = passwordResetDao
                .findByUserId(testUser1.getId())
                .orElseThrow(() -> new RuntimeException("This must not fail"));

        errorResponse = restTemplate.exchange(String.format("/public/users/password/follow/%s", UUID.randomUUID()), HttpMethod.GET, null, ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/public/users/password/follow/%s", passwordReset.getId()), HttpMethod.GET, null, ErrorResponse.class);
        assertEquals(HttpStatus.FOUND, errorResponse.getStatusCode());

        List<String> location = errorResponse.getHeaders().get("Location");
        assertNotNull(location);
        assertEquals(1, location.size());

        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(location.get(0)).build().getQueryParams();
        assertEquals(1, parameters.get("passwordResetId").size());
        assertEquals(passwordReset.getId(), UUID.fromString(parameters.get("passwordResetId").get(0)));

        errorResponse = restTemplate.postForEntity(String.format("/public/users/password/reset/%s", passwordReset.getId()), payloadWithoutAuth(new UserPassword("notStrongEnough")), ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());

        response = restTemplate.postForEntity(String.format("/public/users/password/reset/%s", passwordReset.getId()), payloadWithoutAuth(new UserPassword(testPassword)), UserToken.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testManageFriends() {
        createUser1();
        createUser2();

        // Can't request friend with self

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", testUser1.getPseudo()), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        // Request friend

        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/request/%s", testUser2.getPseudo()), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), Void.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        // Can't have 2+ same friend request at a time

        errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", testUser2.getPseudo()), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        // Count received friend requests

        ResponseEntity<Count> getFriendsCountResponse = restTemplate.exchange("/users/friends/received/count", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getFriendsCountResponse.getStatusCode());
        assertEquals(0L, getFriendsCountResponse.getBody().getCount());

        getFriendsCountResponse = restTemplate.exchange("/users/friends/received/count", HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), Count.class);
        assertEquals(HttpStatus.OK, getFriendsCountResponse.getStatusCode());
        assertEquals(1L, getFriendsCountResponse.getBody().getCount());

        // List received friend requests

        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<FriendRequest>> getFriendsResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        // List sent friend requests

        getFriendsResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        // List all friends and requests

        ResponseEntity<FriendsAndRequests> friendsAndRequestsResponse = restTemplate.exchange("/users/friends", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), FriendsAndRequests.class);
        assertEquals(HttpStatus.OK, friendsAndRequestsResponse.getStatusCode());

        // Reject friend request

        UUID friendRequestId =  getFriendsResponse.getBody().get(0).getId();
        friendResponse = restTemplate.exchange(String.format("/users/friends/reject/%s", friendRequestId), HttpMethod.POST, emptyPayloadWithAuth(testUserToken2), Void.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        getFriendsResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        // Request friend

        friendResponse = restTemplate.exchange(String.format("/users/friends/request/%s", testUser2.getPseudo()), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), Void.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        // Accept friend request

        getFriendsResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        friendRequestId =  getFriendsResponse.getBody().get(0).getId();

        friendResponse = restTemplate.exchange(String.format("/users/friends/accept/%s", friendRequestId), HttpMethod.POST, emptyPayloadWithAuth(testUserToken2), Void.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        getFriendsResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        // Can't request when already friend

        errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", testUser2.getPseudo()), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        // Remove friend

        errorResponse = restTemplate.exchange("/users/friends/remove/anyId", HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());

        friendResponse = restTemplate.exchange(String.format("/users/friends/remove/%s", testUser2.getId()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());

        friendsAndRequestsResponse = restTemplate.exchange("/users/friends", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), FriendsAndRequests.class);
        assertEquals(HttpStatus.OK, friendsAndRequestsResponse.getStatusCode());
        assertEquals(0, friendsAndRequestsResponse.getBody().getFriends().size());

        friendsAndRequestsResponse = restTemplate.exchange("/users/friends", HttpMethod.GET, emptyPayloadWithAuth(testUserToken2), FriendsAndRequests.class);
        assertEquals(HttpStatus.OK, friendsAndRequestsResponse.getStatusCode());
        assertEquals(0, friendsAndRequestsResponse.getBody().getFriends().size());
    }

}
