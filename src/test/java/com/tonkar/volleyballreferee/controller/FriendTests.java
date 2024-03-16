package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.service.FriendService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FriendTests extends VbrMockedTests {

    private final FriendService friendService;

    public FriendTests(@Autowired FriendService friendService) {
        super();
        this.friendService = friendService;
    }

    @ParameterizedTest
    @CsvSource(value = { "GET, /users/friends",
                         "GET, /users/friends/requested",
                         "GET, /users/friends/received",
                         "GET, /users/friends/received/count",
                         "POST, /users/friends/request/anyPseudo",
                         "POST, /users/friends/accept/anyId",
                         "POST, /users/friends/reject/anyId",
                         "DELETE, /users/friends/remove/anyId"
    })
    void test_friends_unauthorized(HttpMethod method, String path) {
        final var invalidToken = "invalid";

        webTestClient
                .method(method)
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_friends_request() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .post()
                .uri(String.format("/users/friends/request/%s", userToken2.user().pseudo()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void test_friends_request_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .post()
                .uri(String.format("/users/friends/request/%s", userToken.user().pseudo()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_friends_request_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));

        // WHEN / THEN
        webTestClient
                .post()
                .uri(String.format("/users/friends/request/%s", userToken2.user().pseudo()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_friends_request_received_count() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/users/friends/received/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Count.class)
                .value(count -> Assertions.assertEquals(0L, count.count()));
    }

    @Test
    void test_friends_request_received_count2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/users/friends/received/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken2.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Count.class)
                .value(count -> Assertions.assertEquals(1L, count.count()));
    }

    @Test
    void test_friends_request_received_list() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/users/friends/received")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(FriendRequest.class)
                .hasSize(0);
    }

    @Test
    void test_friends_request_received_list2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/users/friends/received")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken2.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(FriendRequest.class)
                .hasSize(1);
    }

    @Test
    void test_friends_request_sent_list() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/users/friends/requested")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken2.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(FriendRequest.class)
                .hasSize(0);
    }

    @Test
    void test_friends_request_sent_list2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/users/friends/requested")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(FriendRequest.class)
                .hasSize(1);
    }

    @Test
    void test_friends_and_requests() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/users/friends")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_friends_reject() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        UUID friendRequestId = friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN / THEN
        webTestClient
                .post()
                .uri(String.format("/users/friends/reject/%s", friendRequestId))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken2.token()))
                .exchange()
                .expectStatus()
                .isNoContent();

        // THEN
        assertTrue(friendService.listFriendRequestsSentBy(sandbox.getUser(userToken.user().id())).isEmpty());
        assertTrue(friendService.listFriendRequestsReceivedBy(sandbox.getUser(userToken2.user().id())).isEmpty());
    }

    @Test
    void test_friends_accept() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        UUID friendRequestId = friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken2.user().pseudo());

        // WHEN / THEN
        webTestClient
                .post()
                .uri(String.format("/users/friends/accept/%s", friendRequestId))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken2.token()))
                .exchange()
                .expectStatus()
                .isCreated();

        assertTrue(sandbox
                           .getUser(userToken.user().id())
                           .getFriends()
                           .stream()
                           .anyMatch(friend -> friend.getId().equals(userToken2.user().id())));
        assertTrue(sandbox
                           .getUser(userToken2.user().id())
                           .getFriends()
                           .stream()
                           .anyMatch(friend -> friend.getId().equals(userToken.user().id())));
        assertTrue(friendService.listFriendRequestsSentBy(sandbox.getUser(userToken.user().id())).isEmpty());
        assertTrue(friendService.listFriendRequestsReceivedBy(sandbox.getUser(userToken2.user().id())).isEmpty());
    }

    @Test
    void test_friends_remove() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));

        // WHEN / THEN
        webTestClient
                .delete()
                .uri(String.format("/users/friends/remove/%s", userToken2.user().id()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(sandbox
                           .getUser(userToken.user().id())
                           .getFriends()
                           .stream()
                           .noneMatch(friend -> friend.getId().equals(userToken2.user().id())));
        assertTrue(sandbox
                           .getUser(userToken2.user().id())
                           .getFriends()
                           .stream()
                           .noneMatch(friend -> friend.getId().equals(userToken.user().id())));
    }

    @Test
    void test_friends_remove_notFound() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        String anyId = UUID.randomUUID().toString();

        // WHEN / THEN
        webTestClient
                .delete()
                .uri(String.format("/users/friends/remove/%s", anyId))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
