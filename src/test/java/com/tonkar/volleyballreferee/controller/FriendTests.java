package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.service.FriendService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

@ContextConfiguration(classes = UserController.class)
class FriendTests extends VbrControllerTests {

    @MockBean
    private FriendService friendService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_friends_listFriendRequestsSentBy(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/users/friends/requested")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_friends_listFriendRequestsReceivedBy(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/users/friends/received")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_friends_getNumberOfFriendRequestsReceivedBy(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/users/friends/received/count")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_friends_listFriendsAndRequests(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/users/friends")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, CREATED", "adminToken, CREATED", "invalidToken, UNAUTHORIZED" })
    void test_friends_sendFriendRequest(String token, HttpStatus responseCode) {
        webTestClient
                .post()
                .uri("/users/friends/request/anyPseudo")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, CREATED", "adminToken, CREATED", "invalidToken, UNAUTHORIZED" })
    void test_friends_acceptFriendRequest(String token, HttpStatus responseCode) {
        webTestClient
                .post()
                .uri("/users/friends/accept/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_friends_rejectFriendRequest(String token, HttpStatus responseCode) {
        webTestClient
                .post()
                .uri("/users/friends/reject/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, NO_CONTENT", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_friends_removeFriend(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/users/friends/remove/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
