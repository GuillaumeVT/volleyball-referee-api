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

class FriendTests extends VbrMockedTests {

    @Test
    void test_friends_unauthorized() {
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
    void test_friends_request() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", userToken2.user().pseudo()), HttpMethod.POST, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CREATED, errorResponse.getStatusCode());
    }

    @Test
    void test_friends_request_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", userToken.user().pseudo()), HttpMethod.POST, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    void test_friends_request_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/users/friends/request/%s", userToken2.user().pseudo()), HttpMethod.POST, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    void test_friends_request_received_count(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN
        ResponseEntity<Count> friendResponse = restTemplate.exchange("/users/friends/received/count", HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertEquals(0L, Objects.requireNonNull(friendResponse.getBody()).count());
    }

    @Test
    void test_friends_request_received_count2(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN
        ResponseEntity<Count> friendResponse = restTemplate.exchange("/users/friends/received/count", HttpMethod.GET, emptyPayloadWithAuth(userToken2.token()), Count.class);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertEquals(1L, Objects.requireNonNull(friendResponse.getBody()).count());
    }

    @Test
    void test_friends_request_received_list(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());
        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<FriendRequest>> friendResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), typeReference);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertTrue(Objects.requireNonNull(friendResponse.getBody()).isEmpty());
    }

    @Test
    void test_friends_request_received_list2(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());
        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<FriendRequest>> friendResponse = restTemplate.exchange("/users/friends/received", HttpMethod.GET, emptyPayloadWithAuth(userToken2.token()), typeReference);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(friendResponse.getBody()).size());
    }

    @Test
    void test_friends_request_sent_list(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());
        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<FriendRequest>> friendResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(userToken2.token()), typeReference);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertTrue(Objects.requireNonNull(friendResponse.getBody()).isEmpty());
    }

    @Test
    void test_friends_request_sent_list2(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());
        ParameterizedTypeReference<List<FriendRequest>> typeReference = new ParameterizedTypeReference<>() {};

        // WHEN
        ResponseEntity<List<FriendRequest>> friendResponse = restTemplate.exchange("/users/friends/requested", HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), typeReference);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
        assertEquals(1, Objects.requireNonNull(friendResponse.getBody()).size());
    }

    @Test
    void test_friends_and_requests() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN
        ResponseEntity<FriendsAndRequests> friendResponse = restTemplate.exchange("/users/friends", HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), FriendsAndRequests.class);

        // THEN
        assertEquals(HttpStatus.OK, friendResponse.getStatusCode());
    }

    @Test
    void test_friends_reject(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        UUID friendRequestId = friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN
        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/reject/%s", friendRequestId), HttpMethod.POST, emptyPayloadWithAuth(userToken2.token()), Void.class);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());
        assertTrue(friendService.listFriendRequestsSentBy(sandbox.getUser(userToken.user().id())).isEmpty());
        assertTrue(friendService.listFriendRequestsReceivedBy(sandbox.getUser(userToken2.user().id())).isEmpty());
    }

    @Test
    void test_friends_accept(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        UUID friendRequestId = friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN
        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/accept/%s", friendRequestId), HttpMethod.POST, emptyPayloadWithAuth(userToken2.token()), Void.class);

        // THEN
        assertEquals(HttpStatus.CREATED, friendResponse.getStatusCode());
        assertTrue(sandbox.getUser(userToken.user().id()).getFriends().stream().anyMatch(friend -> friend.getId().equals(userToken2.user().id())));
        assertTrue(sandbox.getUser(userToken2.user().id()).getFriends().stream().anyMatch(friend -> friend.getId().equals(userToken.user().id())));
        assertTrue(friendService.listFriendRequestsSentBy(sandbox.getUser(userToken.user().id())).isEmpty());
        assertTrue(friendService.listFriendRequestsReceivedBy(sandbox.getUser(userToken2.user().id())).isEmpty());
    }

    @Test
    void test_friends_remove() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));

        // WHEN
        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/remove/%s", userToken2.user().id()), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.token()), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, friendResponse.getStatusCode());
        assertTrue(sandbox.getUser(userToken.user().id()).getFriends().stream().noneMatch(friend -> friend.getId().equals(userToken2.user().id())));
        assertTrue(sandbox.getUser(userToken2.user().id()).getFriends().stream().noneMatch(friend -> friend.getId().equals(userToken.user().id())));
    }

    @Test
    void test_friends_remove_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        String anyId = UUID.randomUUID().toString();

        // WHEN
        ResponseEntity<Void> friendResponse = restTemplate.exchange(String.format("/users/friends/remove/%s", anyId), HttpMethod.DELETE, emptyPayloadWithAuth(userToken.token()), Void.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, friendResponse.getStatusCode());
    }
}
