package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class UserTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        User user = new User();

        ResponseEntity<User> getUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), User.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getUserResponse.getStatusCode());

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf("/api/v3/public/users/123"), HttpMethod.POST, payloadWithAuth(testUserInvalidAuth, user), String.class);
        assertEquals(HttpStatus.FORBIDDEN, postUserResponse.getStatusCode());

        ResponseEntity<String> deleteUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, deleteUserResponse.getStatusCode());

        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<FriendRequest>> getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getFriendsResponse.getStatusCode());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getFriendsResponse.getStatusCode());

        ResponseEntity<Count> getFriendsCountResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getFriendsCountResponse.getStatusCode());

        ResponseEntity<String> friendResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/request/anyPseudo"), HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendResponse.getStatusCode());

        friendResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/accept/anyId"), HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendResponse.getStatusCode());

        friendResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/reject/anyId"), HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendResponse.getStatusCode());

        friendResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/remove/anyId"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, friendResponse.getStatusCode());
    }

    @Test
    public void testManageUsers() {
        String pseudo = "VBR1";

        User user = new User();
        user.setId(testUser1Id);
        user.setPseudo(pseudo);
        user.setFriends(new ArrayList<>());

        // User does not exist yet

        ResponseEntity<User> getUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), User.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getUserResponse.getStatusCode());

        // Create user

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf("/api/v3/public/users/54Hhfht"), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.FORBIDDEN, postUserResponse.getStatusCode());

        postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        // User already exists

        postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.CONFLICT, postUserResponse.getStatusCode());

        // User pseudo is taken

        user.setId(testUser2Id);
        user.setPseudo(pseudo);

        postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.CONFLICT, postUserResponse.getStatusCode());

        // Get user

        getUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), User.class);
        assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());

        // Delete user

        ResponseEntity<String> deleteUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteUserResponse.getStatusCode());
    }

    @Test
    public void testManageFriends() {
        String pseudo1 = "VBR1";
        String pseudo2 = "VBR2";

        User user1 = new User();
        user1.setId(testUser1Id);
        user1.setPseudo(pseudo1);
        user1.setFriends(new ArrayList<>());

        User user2 = new User();
        user2.setId(testUser2Id);
        user2.setPseudo(pseudo2);
        user2.setFriends(new ArrayList<>());

        // Create users

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user1), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user2), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        // Can't request friend with self

        ResponseEntity<String> friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/request/%s", pseudo1)), HttpMethod.POST, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CONFLICT, friendResponse.getStatusCode());

        // Request friend

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/request/%s", pseudo2)), HttpMethod.POST, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        // Can't have 2+ same friend request at a time

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/request/%s", pseudo2)), HttpMethod.POST, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CONFLICT, friendResponse.getStatusCode());

        // Count received friend requests

        ResponseEntity<Count> getFriendsCountResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received/count"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Count.class);
        assertEquals(HttpStatus.OK, getFriendsCountResponse.getStatusCode());
        assertEquals(0L, getFriendsCountResponse.getBody().getCount());

        getFriendsCountResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received/count"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), Count.class);
        assertEquals(HttpStatus.OK, getFriendsCountResponse.getStatusCode());
        assertEquals(1L, getFriendsCountResponse.getBody().getCount());

        // List received friend requests

        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<FriendRequest>> getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        // List sent friend requests

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        // Reject friend request

        UUID friendRequestId =  getFriendsResponse.getBody().get(0).getId();
        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/reject/%s", friendRequestId)), HttpMethod.POST, emptyPayloadWithAuth(testUser2Auth), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        // Request friend

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/request/%s", pseudo2)), HttpMethod.POST, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        // Accept friend request

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(1, getFriendsResponse.getBody().size());

        friendRequestId =  getFriendsResponse.getBody().get(0).getId();

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/accept/%s", friendRequestId)), HttpMethod.POST, emptyPayloadWithAuth(testUser2Auth), String.class);
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/received"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        getFriendsResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/requested"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getFriendsResponse.getStatusCode());
        assertEquals(0, getFriendsResponse.getBody().size());

        // Can't request when already friend

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/request/%s", pseudo2)), HttpMethod.POST, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.CONFLICT, friendResponse.getStatusCode());

        // Get users

        ResponseEntity<User> getUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), User.class);
        assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        assertEquals(1, getUserResponse.getBody().getFriends().size());

        getUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), User.class);
        assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        assertEquals(1, getUserResponse.getBody().getFriends().size());

        String userId = getUserResponse.getBody().getId();

        // Remove friend

        friendResponse = restTemplate.exchange(urlOf("/api/v3/users/friends/remove/anyId"), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NOT_FOUND, friendResponse.getStatusCode());

        friendResponse = restTemplate.exchange(urlOf(String.format("/api/v3/users/friends/remove/%s", userId)), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());

        getUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), User.class);
        assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        assertEquals(0, getUserResponse.getBody().getFriends().size());

        getUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.GET, emptyPayloadWithAuth(testUser2Auth), User.class);
        assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        assertEquals(0, getUserResponse.getBody().getFriends().size());

        // Delete users

        ResponseEntity<String> deleteUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteUserResponse.getStatusCode());

        deleteUserResponse = restTemplate.exchange(urlOf("/api/v3/users"), HttpMethod.DELETE, emptyPayloadWithAuth(testUser2Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteUserResponse.getStatusCode());
    }

}
