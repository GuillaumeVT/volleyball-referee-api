package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.FriendsAndRequests;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.service.FriendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FriendTests extends VbrMockedTests {

    @Test
    public void test_friends_unauthorized() {
        final var invalidToken = "invalid";

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/users/friends", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/received/count", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/request/anyPseudo", HttpMethod.POST, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/accept/anyId", HttpMethod.POST, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/reject/anyId", HttpMethod.POST, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/users/friends/remove/anyId", HttpMethod.DELETE, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void test_friends_request() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", userToken2.getUser().getPseudo()), HttpMethod.POST, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CREATED, errorResponse.getStatusCode());
    }

    @Test
    public void test_friends_request_conflict() {
        // GIVEN
        UserToken userToken = createUser();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", userToken.getUser().getPseudo()), HttpMethod.POST, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    public void test_friends_request_conflict2() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", userToken2.getUser().getPseudo()), HttpMethod.POST, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    public void test_friends_request_received_count(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken2.getUser().getPseudo());

        // WHEN
        ResponseEntity<Count> friendResponse = restTemplate.exchange("/users/friends/received/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertEquals(0L, Objects.requireNonNull(friendResponse.getBody()).getCount());
    }

    @Test
    public void test_friends_request_received_count2(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken2.getUser().getPseudo());

        // WHEN
        ResponseEntity<Count> friendResponse = restTemplate.exchange("/users/friends/received/count", HttpMethod.GET, emptyPayloadWithAuth(userToken2.getToken()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertEquals(1L, Objects.requireNonNull(friendResponse.getBody()).getCount());
    }

    @Test
    public void test_friends_request_received_list(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken2.getUser().getPseudo());
        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<FriendRequest>> friendResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), typeReference);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertTrue(Objects.requireNonNull(friendResponse.getBody()).isEmpty());
    }

    @Test
    public void test_friends_request_received_list2(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken2.getUser().getPseudo());
        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<FriendRequest>> friendResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(userToken2.getToken()), typeReference);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(friendResponse.getBody()).size());
    }

    @Test
    public void test_friends_request_sent_list(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken2.getUser().getPseudo());
        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<FriendRequest>> friendResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(userToken2.getToken()), typeReference);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertTrue(Objects.requireNonNull(friendResponse.getBody()).isEmpty());
    }

    @Test
    public void test_friends_request_sent_list2(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken2.getUser().getPseudo());
        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<FriendRequest>> friendResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), typeReference);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(friendResponse.getBody()).size());
    }

    @Test
    public void test_friends_and_requests() {
        // GIVEN
        UserToken userToken = createUser();

        // WHEN
        ResponseEntity<FriendsAndRequests> friendResponse = restTemplate.exchange("/users/friends", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), FriendsAndRequests.class);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
    }

    @Test
    public void test_friends_reject(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        UUID friendRequestId = friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken2.getUser().getPseudo());

        // WHEN
        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/reject/%s", friendRequestId), HttpMethod.POST, emptyPayloadWithAuth(userToken2.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());
        assertTrue(friendService.listFriendRequestsSentBy(getUser(userToken.getUser().getId())).isEmpty());
        assertTrue(friendService.listFriendRequestsReceivedBy(getUser(userToken2.getUser().getId())).isEmpty());
    }

    @Test
    public void test_friends_accept(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        UUID friendRequestId = friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken2.getUser().getPseudo());

        // WHEN
        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/accept/%s", friendRequestId), HttpMethod.POST, emptyPayloadWithAuth(userToken2.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());
        assertTrue(getUser(userToken.getUser().getId()).getFriends().stream().anyMatch(friend -> friend.getId().equals(userToken2.getUser().getId())));
        assertTrue(getUser(userToken2.getUser().getId()).getFriends().stream().anyMatch(friend -> friend.getId().equals(userToken.getUser().getId())));
        assertTrue(friendService.listFriendRequestsSentBy(getUser(userToken.getUser().getId())).isEmpty());
        assertTrue(friendService.listFriendRequestsReceivedBy(getUser(userToken2.getUser().getId())).isEmpty());
    }

    @Test
    public void test_friends_remove() {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));

        // WHEN
        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/remove/%s", userToken2.getUser().getId()), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());
        assertTrue(getUser(userToken.getUser().getId()).getFriends().stream().noneMatch(friend -> friend.getId().equals(userToken2.getUser().getId())));
        assertTrue(getUser(userToken2.getUser().getId()).getFriends().stream().noneMatch(friend -> friend.getId().equals(userToken.getUser().getId())));
    }

    @Test
    public void test_friends_remove_notFound() {
        // GIVEN
        UserToken userToken = createUser();
        String anyId = UUID.randomUUID().toString();

        // WHEN
        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/remove/%s", anyId), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.getToken()), Void.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, friendResponse.getStatusCode());
    }
}
