package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.UserToken;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class StatisticsTests extends VbrMockedTests {

    @Test
    void test_statistics_unauthorized() {
        final var invalidToken = "invalid";

        webTestClient
                .get()
                .uri("/statistics")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_statistics_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/statistics")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void test_statistics_public_get() {
        // WHEN / THEN
        webTestClient.get().uri("/public/statistics").exchange().expectStatus().isOk();
    }
}
